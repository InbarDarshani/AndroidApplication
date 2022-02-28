package com.example.mixtape.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mixtape.R;
import com.example.mixtape.model.Model;
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String songId = SongDetailsFragmentArgs.fromBundle(getArguments()).getSongId();
        viewModel = new ViewModelProvider(this, new SongDetailsViewModelFactory(songId)).get(SongDetailsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        return view;
    }
}