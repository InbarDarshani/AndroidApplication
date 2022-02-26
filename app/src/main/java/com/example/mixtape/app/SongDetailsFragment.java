package com.example.mixtape.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mixtape.R;
import com.example.mixtape.model.Model;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

public class SongDetailsFragment extends Fragment {
    ImageView song_details_user_iv, song_details_image_iv;
    TextView song_details_user_tv, song_name_tv, song_artist_tv, song_caption_tv, song_mixtape_tv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        String songId = SongDetailsFragmentArgs.fromBundle(getArguments()).getSongId();

        Model.instance.getSongItem(songId, songItem -> {
            Model.instance.mainThread.post(() -> {
                //Bind Mixtape data of this song post
                song_mixtape_tv.setText(songItem.mixtape.getName());

                //Bind User data of this song post
                if (!songItem.user.getImage().isEmpty())
                    Picasso.get().load(songItem.user.getImage()).into(song_details_user_iv);
                song_details_user_tv.setText(songItem.user.getDisplayName());

                //Bind song's data
                if (!songItem.song.getImage().isEmpty())
                    Picasso.get().load(songItem.song.getImage()).into(song_details_image_iv);

                song_name_tv.setText(songItem.song.getName());
                song_artist_tv.setText(songItem.song.getArtist());
                song_caption_tv.setText(songItem.song.getCaption());
            });
        });

        return view;
    }
}