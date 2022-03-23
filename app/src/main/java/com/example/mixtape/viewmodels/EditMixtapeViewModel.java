package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.User;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class EditMixtapeViewModel extends ViewModel {
    private String mixtapeId = "";
    private User currentUser;
    private LiveData<List<SongItem>> songItems;
    private LiveData<List<MixtapeItem>> mixtapeItems;

    public EditMixtapeViewModel(String mixtapeId) {
        this.mixtapeId = mixtapeId;
        currentUser = Model.instance.getCurrentUser();
        songItems = Model.instance.getUserSongItems(currentUser.getUserId());
        mixtapeItems = Model.instance.getUserMixtapeItems(currentUser.getUserId());
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<SongItem>> getSongItems() {
        return songItems;
    }

    public LiveData<List<MixtapeItem>> getMixtapeItems() {
        return mixtapeItems;
    }

    public List<Song> getUserSongs() {
        return songItems.getValue().stream().map(SongItem::getSong).collect(Collectors.toList());
    }

    public Mixtape getMixtape() {
        return mixtapeItems.getValue().stream().map(m -> m.getMixtape())
                .filter(m -> m.getMixtapeId().equals(mixtapeId)).findAny().get();
    }

    public List<Song> getMixtapeSongs() {
        return songItems.getValue().stream().map(SongItem::getSong)
                .filter(s -> s.getMixtapeId().equals(mixtapeId)).collect(Collectors.toList());
    }

    public boolean existingMixtapeName(String mixtapeName) {
        return (mixtapeItems.getValue().stream().map(m -> m.getMixtape())
                .anyMatch(m -> (m.getName().equals(mixtapeName) && !m.getMixtapeId().equals(mixtapeId))));
    }
}
