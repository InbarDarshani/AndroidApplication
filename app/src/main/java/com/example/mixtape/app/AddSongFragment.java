package com.example.mixtape.app;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.AddSongViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

public class AddSongFragment extends Fragment {
    AddSongViewModel viewModel;
    MaterialAlertDialogBuilder alert;
    ImageView song_image_iv;
    ImageButton song_gallery_btn, song_cam_btn;
    EditText song_name_et, song_artist_et, song_caption_et;
    AutoCompleteTextView song_mixtape_et;
    ArrayAdapter<String> adapter;
    TextInputLayout song_mixtape_til;
    Button song_post_btn;
    ProgressBar progressBar;
    Bitmap inputImage;
    String inputSongName, inputArtist, inputCaption, inputMixtapeId, currentUserId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(AddSongViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_song, container, false);

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

        //Setup user's mixtapes list adapter
        adapter = new ArrayAdapter<>(MyApplication.getContext(), android.R.layout.simple_dropdown_item_1line);
        song_mixtape_et.setOnItemClickListener((parent, view1, position, id) -> {
            inputMixtapeId = viewModel.getMixtapes().get(position).getMixtapeId();
        });

        //Observe user's mixtapes live data
        viewModel.getMixtapeItems().observe(getViewLifecycleOwner(), mixtapeItems -> {
            //Treat empty list
            //TODO: Add button (dialog) to create a mixtape and/or create default mixtape
            if (viewModel.getMixtapes().isEmpty()) {
                song_mixtape_til.setHint("No Mixtapes created yet");
                song_mixtape_et.setEnabled(false);
            } else {
                //Setup dropdown list with user's mixtapes
                adapter.clear();
                adapter.addAll(viewModel.getMixtapesOptions());
                song_mixtape_et.setAdapter(adapter);
                song_mixtape_et.setEnabled(true);
            }
        });

        return view;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        activityResultLauncher.launch(galleryIntent);
    }

    private void openCam() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(cameraIntent);
    }

    //Activity launcher for result
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        //Create initial bitmap
                        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

                        //If its a Camera activity get bitmap data directly from extras
                        if (result.getData().hasExtra("data")) {
                            bitmap = (Bitmap) result.getData().getExtras().get("data");
                        }
                        //If its a Gallery activity create bitmap data from image uri
                        else {
                            try {
                                Uri imageUri = result.getData().getData();
                                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //Convert bitmap image to thumbnail and set in view
                        //TODO: set proper size
                        inputImage = Bitmap.createScaledBitmap(bitmap, 189, 252, false);
                        song_image_iv.setImageBitmap(inputImage);
                    }
                }
            });

    private void save() {
        progressBar.setVisibility(View.VISIBLE);
        song_post_btn.setEnabled(false);
        song_cam_btn.setEnabled(false);
        song_gallery_btn.setEnabled(false);
        song_mixtape_et.setEnabled(false);

        inputSongName = song_name_et.getText().toString();
        inputArtist = song_artist_et.getText().toString();
        inputCaption = song_caption_et.getText().toString();
        currentUserId = viewModel.getCurrentUser().getUserId();

        Song song = new Song(inputSongName, inputArtist, inputCaption);
        song.setUserId(currentUserId);
        song.setMixtapeId(inputMixtapeId);

        //Validate Input
        if (inputSongName.isEmpty())
            alert.setMessage("Please enter song's name").show();

        //Save song to dbs
        Model.instance.addSong(song, songId -> {
            if (inputImage != null) {
                Model.instance.saveImage(inputImage, "songs", songId + ".jpg", url -> {
                    song.setImage(url);
                    Model.instance.updateSong(song, () -> Model.instance.mainThread.post(() -> Navigation.findNavController(song_name_et).navigateUp()));
                });
            } else {
                Model.instance.mainThread.post(() -> Navigation.findNavController(song_name_et).navigateUp());
            }
        });
    }
}