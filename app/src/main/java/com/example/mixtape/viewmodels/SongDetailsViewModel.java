package com.example.mixtape.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.User;

import java.util.List;

public class SongDetailsViewModel extends ViewModel {
    private SongItem songItem;

    public SongDetailsViewModel(String songId) {
        Model.instance.getSongItem(songId, dbSongItem -> {
            songItem = dbSongItem;
        });
    }

    public Song getSong() {
        return songItem.getSong();
    }

    public Mixtape getMixtape() {
        return songItem.getMixtape();
    }

    public User getUser() {
        return songItem.getUser();
    }

}
