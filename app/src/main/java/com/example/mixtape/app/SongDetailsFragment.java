package com.example.mixtape.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.mixtape.MyApplication;
import com.example.mixtape.NavGraphDirections;
import com.example.mixtape.R;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.viewmodels.MixtapeDetailsViewModel;
import com.example.mixtape.viewmodels.MixtapeDetailsViewModelFactory;
import com.example.mixtape.viewmodels.SongDetailsViewModel;
import com.example.mixtape.viewmodels.SongDetailsViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

public class SongDetailsFragment extends Fragment {
    SongDetailsViewModel viewModel;
    ImageView song_details_user_iv, song_details_image_iv;
    TextView song_details_user_tv, song_name_tv, song_artist_tv, song_caption_tv, song_mixtape_tv;
    ImageButton song_edit, song_delete;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String songId = SongDetailsFragmentArgs.fromBundle(getArguments()).getSongId();
        viewModel = new ViewModelProvider(this, new SongDetailsViewModelFactory(songId)).get(SongDetailsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_details, container, false);

        //Get views
        song_details_user_iv = view.findViewById(R.id.song_details_user_iv);
        song_details_user_tv = view.findViewById(R.id.song_details_user_tv);
        song_details_image_iv = view.findViewById(R.id.song_details_image_iv);
        song_name_tv = view.findViewById(R.id.song_name_tv);
        song_artist_tv = view.findViewById(R.id.song_artist_tv);
        song_caption_tv = view.findViewById(R.id.song_caption_tv);
        song_mixtape_tv = view.findViewById(R.id.song_mixtape_tv);
        song_edit = view.findViewById(R.id.song_edit);
        song_delete = view.findViewById(R.id.song_delete);

        //Set on click navigations
        song_details_user_iv.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        SongDetailsFragmentDirections.actionSongDetailsFragmentToUserFragment(viewModel.getUser().getUserId())));
        song_mixtape_tv.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        SongDetailsFragmentDirections.actionSongDetailsFragmentToMixtapeDetailsFragment(viewModel.getMixtape().getMixtapeId())));

        //Observe view model's data
        viewModel.getSongItem().observe(getViewLifecycleOwner(), songItem -> {
            bind();

            //Enable clickables
            song_details_user_iv.setClickable(true);
            song_mixtape_tv.setEnabled(true);
            song_edit.setEnabled(true);
            song_delete.setEnabled(true);

            //Setup view if its current user's
            currentUserSetup();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void bind() {
        //Bind Mixtape data of this song post
        song_mixtape_tv.setText(viewModel.getMixtape().getName());

        //Bind User data of this song post
        if (!viewModel.getUser().getImage().isEmpty())
            Picasso.get().load(viewModel.getUser().getImage()).into(song_details_user_iv);
        song_details_user_tv.setText(viewModel.getUser().getDisplayName());

        //Bind song's data
        if (!viewModel.getSong().getImage().isEmpty())
            Picasso.get().load(viewModel.getSong().getImage()).into(song_details_image_iv);
        song_name_tv.setText(viewModel.getSong().getName());
        song_artist_tv.setText(viewModel.getSong().getArtist());
        song_caption_tv.setText(viewModel.getSong().getCaption());
    }

    private void currentUserSetup(){
        //Setup edit and delete buttons
        String currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("userId", "");
        if (viewModel.getUser().getUserId().equals(currentUserId)) {
            song_edit.setVisibility(View.VISIBLE);
            song_delete.setVisibility(View.VISIBLE);

            song_edit.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(
                        SongDetailsFragmentDirections.actionSongDetailsFragmentToEditSongFragment(viewModel.getSong().getSongId()));
            });

            song_delete.setOnClickListener(v -> {
                Model.instance.deleteSong(viewModel.getSong(), () -> {
                    Navigation.findNavController(v).navigateUp();
                });
            });
        }
    }
}