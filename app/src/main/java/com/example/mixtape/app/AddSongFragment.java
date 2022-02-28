package com.example.mixtape.app;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.mixtape.MyApplication;
import com.example.mixtape.R;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddSongFragment extends Fragment {
    MaterialAlertDialogBuilder alert;
    ImageView song_image_iv;
    ImageButton song_gallery_btn, song_cam_btn;
    EditText song_name_et, song_artist_et, song_caption_et;
    AutoCompleteTextView song_mixtape_et;
    TextInputLayout song_mixtape_til;
    Button song_post_btn;
    ProgressBar progressBar;
    Bitmap imageBitmap;
    String currentUserId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_song, container, false);

        //Get userId
        currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("userId", "");

        //Get views
        song_image_iv = view.findViewById(R.id.song_image_iv);
        song_gallery_btn = view.findViewById(R.id.song_gallery_btn);
        song_cam_btn = view.findViewById(R.id.song_cam_btn);
        song_name_et = view.findViewById(R.id.song_name_et);
        song_artist_et = view.findViewById(R.id.song_artist_et);
        song_caption_et = view.findViewById(R.id.song_caption_et);
        song_mixtape_et = view.findViewById(R.id.song_mixtape_et);
        song_mixtape_til = view.findViewById(R.id.song_mixtape_til);
        song_post_btn = view.findViewById(R.id.song_post_btn);
        progressBar = view.findViewById(R.id.add_song_progressbar);
        //Set buttons listeners
        song_post_btn.setOnClickListener(v -> save());
        song_cam_btn.setOnClickListener(v -> openCam());
        song_gallery_btn.setOnClickListener(v -> openGallery());

        //Setup alert dialog
        alert = new MaterialAlertDialogBuilder(this.getContext());
        alert.setTitle("Error");

        //Setup dropdown list with user's mixtapes
        currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("userId", "");
        Model.instance.getMixtapesOfUser(currentUserId, mixtapes -> {
            //TODO: ViewModel!

            //TODO: default mixtape and createMixtape dialog
            if (mixtapes.isEmpty()) {
                song_mixtape_til.setHint("No Mixtapes created yet");
                song_mixtape_et.setEnabled(false);
            }
            if (!currentUserId.isEmpty() && !mixtapes.isEmpty()) {
                String[] options = mixtapes.stream().map(Mixtape::getName).toArray(String[]::new);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MyApplication.getContext(), android.R.layout.simple_dropdown_item_1line, options);
                song_mixtape_et.setAdapter(adapter);
            }
        });

        return view;
    }

    private void openGallery() {

    }

    private void openCam() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startCamera.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> startCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        song_image_iv.setImageBitmap(imageBitmap);
                    }
                }
            });

    private void save() {
        progressBar.setVisibility(View.VISIBLE);
        song_post_btn.setEnabled(false);
        song_cam_btn.setEnabled(false);
        song_gallery_btn.setEnabled(false);

        String name = song_name_et.getText().toString();
        String artist = song_name_et.getText().toString();
        String caption = song_name_et.getText().toString();
        String mixtapeId = song_mixtape_et.getText().toString();

        Song song = new Song(name, artist, caption);
        //CHECKME: new song with userId from shared preferences and mixtapeID from input
        song.setUserId(currentUserId);
        song.setMixtapeId(mixtapeId);

        Model.instance.addSong(song, songId -> {
            if (imageBitmap != null) {
                Model.instance.saveImage(imageBitmap, "songs", songId + ".jpg", song::setImage);
            }

            //navigate to song details page
            //Navigation.findNavController(song_name_et).navigate(FeedFragmentDirections.actionFeedFragmentToSongDetailsFragment(songId));
            Navigation.findNavController(song_name_et).navigateUp();
        });
    }
}