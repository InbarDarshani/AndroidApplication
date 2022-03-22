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
import android.widget.TextView;

import com.example.mixtape.MyApplication;
import com.example.mixtape.R;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.EditSongViewModel;
import com.example.mixtape.viewmodels.EditSongViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;

public class EditSongFragment extends Fragment {
    EditSongViewModel viewModel;
    MaterialAlertDialogBuilder alert;
    ImageView song_image_iv;
    ImageButton song_gallery_btn, song_cam_btn;
    EditText song_name_et, song_artist_et, song_caption_et;
    ArrayAdapter<String> adapter;
    AutoCompleteTextView song_mixtape_name_actv;
    TextInputLayout song_mixtape_name_til, song_mixtape_description_til;
    Button song_post_btn;
    ProgressBar progressBar;
    Bitmap inputImage;
    String inputSongName, inputArtist, inputCaption, inputNewMixtapeName, inputNewMixtapeDescription, currentUserId;
    Mixtape inputChosenMixtape;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String songId = EditSongFragmentArgs.fromBundle(getArguments()).getSongId();
        viewModel = new ViewModelProvider(this, new EditSongViewModelFactory(songId)).get(EditSongViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment using add song fragment layout
        View view = inflater.inflate(R.layout.fragment_add_edit_song, container, false);
        //Edit page view
        ((TextView) view.findViewById(R.id.page_title_tv)).setText("Edit Song");
        ((Button) view.findViewById(R.id.song_post_btn)).setText("Save");

        //Get views
        song_image_iv = view.findViewById(R.id.song_image_iv);
        song_gallery_btn = view.findViewById(R.id.song_gallery_btn);
        song_cam_btn = view.findViewById(R.id.song_cam_btn);
        song_name_et = view.findViewById(R.id.song_name_et);
        song_artist_et = view.findViewById(R.id.song_artist_et);
        song_caption_et = view.findViewById(R.id.song_caption_et);
        song_mixtape_name_til = view.findViewById(R.id.song_mixtape_name_til);
        song_mixtape_name_actv = view.findViewById(R.id.song_mixtape_name_actv);
        song_mixtape_description_til = view.findViewById(R.id.song_mixtape_description_til);
        song_post_btn = view.findViewById(R.id.song_post_btn);
        progressBar = view.findViewById(R.id.add_song_progressbar);

        viewModel.getSongItem().observe(getViewLifecycleOwner(), songItem -> {
            bind();
            setup();
        });

        return view;
    }

    private void bind() {
        //Bind Mixtape data of this song post
        song_mixtape_name_actv.setText(viewModel.getMixtape().getName());

        //Bind song's data
        if (!viewModel.getSong().getImage().isEmpty())
            Picasso.get().load(viewModel.getSong().getImage()).into(song_image_iv);
        song_name_et.setText(viewModel.getSong().getName());
        song_artist_et.setText(viewModel.getSong().getArtist());
        song_caption_et.setText(viewModel.getSong().getCaption());
    }

    private void setup() {
        //Setup buttons listeners
        song_post_btn.setOnClickListener(v -> validateAndSave());
        song_cam_btn.setOnClickListener(v -> openCam());
        song_gallery_btn.setOnClickListener(v -> openGallery());

        //Setup alert dialog
        alert = new MaterialAlertDialogBuilder(this.getContext());
        alert.setTitle("Missing Fields");

        //Setup user's mixtapes list
        adapter = new ArrayAdapter<>(MyApplication.getContext(), android.R.layout.simple_dropdown_item_1line);
        song_mixtape_name_actv.setOnDismissListener(() -> { //onDismiss fires anytime the dropdown disappears by choosing an item or by dismissing it or by typing not found item
            //Get autocomplete text input
            inputNewMixtapeName = song_mixtape_name_actv.getText().toString();
            //If the text is one of the mixtape options
            if (Arrays.stream(viewModel.getMixtapesOptions()).anyMatch(s -> s.equals(inputNewMixtapeName))) {
                int position = adapter.getPosition(inputNewMixtapeName);
                inputChosenMixtape = viewModel.getMixtapes().get(position);
                song_mixtape_description_til.setVisibility(View.GONE);
            }
            //If the text is not one of the mixtape options
            else {
                inputChosenMixtape = null;
                song_mixtape_description_til.setVisibility(View.VISIBLE);
            }
        });

        //Observe user's mixtapes live data
        viewModel.getMixtapeItems().observe(getViewLifecycleOwner(), mixtapeItems -> {
            //Setup dropdown list with user's mixtapes
            adapter.clear();
            adapter.addAll(viewModel.getMixtapesOptions());
            song_mixtape_name_actv.setAdapter(adapter);
            song_mixtape_name_actv.setEnabled(true);
        });
    }

    //Activity launcher for result
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        //Create initial bitmap
                        Bitmap bitmap = null;

                        //If its a Camera activity get bitmap data directly from extras (Not Full Size Image)
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
                        if (bitmap != null) {
                            int wScale = (bitmap.getWidth() > song_image_iv.getWidth()) ? bitmap.getWidth() / song_image_iv.getWidth() : 1;
                            int hScale = (bitmap.getHeight() > song_image_iv.getHeight()) ? bitmap.getHeight() / song_image_iv.getHeight() : 1;
                            int w = bitmap.getWidth() / wScale;
                            int h = bitmap.getHeight() / hScale;
                            inputImage = Bitmap.createScaledBitmap(bitmap, w, h, false);
                            song_image_iv.setImageBitmap(inputImage);
                        }
                    }
                }
            });

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        activityResultLauncher.launch(galleryIntent);
    }

    private void openCam() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(cameraIntent);
    }

    private void validateAndSave() {
        //Get user's inputs
        inputSongName = song_name_et.getText().toString();
        inputArtist = song_artist_et.getText().toString();
        inputCaption = song_caption_et.getText().toString();
        inputNewMixtapeName = song_mixtape_name_actv.getText().toString();
        inputNewMixtapeDescription = song_mixtape_description_til.getEditText().getText().toString();
        currentUserId = viewModel.getCurrentUser().getUserId();

        //If the mixtape text is one of the mixtape options
        if (Arrays.stream(viewModel.getMixtapesOptions()).anyMatch(s -> s.equals(inputNewMixtapeName))) {
            int position = adapter.getPosition(inputNewMixtapeName);
            inputChosenMixtape = viewModel.getMixtapes().get(position);
        }

        //Validate Input
        if (inputSongName.isEmpty())
            alert.setMessage("Please enter song's name").show();
        else if (inputNewMixtapeName.isEmpty())
            alert.setMessage("Please choose or create mixtape").show();
        else
            saveToDb();
    }

    private void saveToDb() {
        progressBar.setVisibility(View.VISIBLE);
        song_post_btn.setEnabled(false);
        song_cam_btn.setEnabled(false);
        song_gallery_btn.setEnabled(false);
        song_mixtape_name_actv.setEnabled(false);

        Song song = viewModel.getSong();
        song.setName(inputSongName);
        song.setArtist(inputArtist);
        song.setCaption(inputCaption);

        //Update Song with image and new mixtape
        if (inputImage != null && inputChosenMixtape == null) {
            Model.instance.updateSong(song, new Mixtape(inputNewMixtapeName, inputNewMixtapeDescription, currentUserId), inputImage, dbSong -> back(dbSong.getSongId()));
        }
        //Update Song with image and existing mixtape
        if (inputImage != null && inputChosenMixtape != null) {
            song.setMixtapeId(inputChosenMixtape.getMixtapeId());
            Model.instance.updateSong(song, inputImage, dbSong -> back(dbSong.getSongId()));
        }
        //Update Song with no image and new mixtape
        if (inputImage == null && inputChosenMixtape == null) {
            Model.instance.updateSong(song, new Mixtape(inputNewMixtapeName, inputNewMixtapeDescription, currentUserId), dbSong -> back(dbSong.getSongId()));
        }
        //Update Song with no image and existing mixtape
        if (inputImage == null && inputChosenMixtape != null) {
            song.setMixtapeId(inputChosenMixtape.getMixtapeId());
            Model.instance.updateSong(song, dbSong -> back(dbSong.getSongId()));
        }
    }

    private void back(String songId) {
        //TODO:
        Navigation.findNavController(song_name_et).navigateUp();
    }
}