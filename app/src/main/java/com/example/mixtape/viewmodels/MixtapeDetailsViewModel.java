package com.example.mixtape.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.User;

import java.util.List;

public class MixtapeDetailsViewModel extends ViewModel {
    private MixtapeItem mixtapeItem;

    public MixtapeDetailsViewModel(String mixtapeId) {
        Model.instance.getMixtapeItem(mixtapeId, dbMixtapeItem -> {
            mixtapeItem = dbMixtapeItem;
        });
    }

    public Mixtape getMixtape() {
        return mixtapeItem.getMixtape();
    }

    public List<Song> getSongs() {
        return mixtapeItem.getSongs();
    }

    public User getUser() {
        return mixtapeItem.getUser();
    }
}
