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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
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
import com.example.mixtape.NavGraphDirections;
import com.example.mixtape.R;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.AddSongViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Arrays;

public class AddSongFragment extends Fragment {
    AddSongViewModel viewModel;
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
        viewModel = new ViewModelProvider(this).get(AddSongViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit_song, container, false);

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

        return view;
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
        inputSongName = song_name_et.getText().toString();
        inputArtist = song_artist_et.getText().toString();
        inputCaption = song_caption_et.getText().toString();
        inputNewMixtapeName = song_mixtape_name_actv.getText().toString();
        inputNewMixtapeDescription = song_mixtape_description_til.getEditText().getText().toString();
        currentUserId = viewModel.getCurrentUser().getUserId();

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

        Song song = new Song(inputSongName, inputArtist, inputCaption, currentUserId);

        //Add Song with image and new mixtape
        if (inputImage != null && inputChosenMixtape == null) {
            Model.instance.addSong(song, new Mixtape(inputNewMixtapeName, inputNewMixtapeDescription, currentUserId), inputImage, dbSong -> toSongDetails(dbSong.getSongId()));
        }
        //Add Song with image and existing mixtape
        if (inputImage != null && inputChosenMixtape != null) {
            song.setMixtapeId(inputChosenMixtape.getMixtapeId());
            Model.instance.addSong(song, inputImage, dbSong -> toSongDetails(dbSong.getSongId()));
        }
        //Add Song with no image and new mixtape
        if (inputImage == null && inputChosenMixtape == null) {
            Model.instance.addSong(song, new Mixtape(inputNewMixtapeName, inputNewMixtapeDescription, currentUserId), dbSong -> toSongDetails(dbSong.getSongId()));
        }
        //Add Song with no image and existing mixtape
        if (inputImage == null && inputChosenMixtape != null) {
            song.setMixtapeId(inputChosenMixtape.getMixtapeId());
            Model.instance.addSong(song, dbSong -> toSongDetails(dbSong.getSongId()));
        }
    }

    private void toSongDetails(String songId) {
        //TOCHECK:!!
        //Remove this fragment from back stack and navigate to the created song details
        FragmentManager manager = getParentFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(this);
        transaction.commit();
        manager.popBackStack();
        //Navigation.findNavController(song_name_et).navigate(AddSongFragmentDirections.actionGlobalSongDetailsFragment(songId));
        Navigation.findNavController(song_name_et).navigate(NavGraphDirections.actionGlobalSongDetailsFragment(songId));
    }
}