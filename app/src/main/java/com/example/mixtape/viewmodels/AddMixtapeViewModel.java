package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class AddMixtapeViewModel extends ViewModel {
    private User currentUser;
    private LiveData<List<SongItem>> userSongItems;
    private LiveData<List<MixtapeItem>> userMixtapeItems;

    public AddMixtapeViewModel() {
        currentUser = Model.instance.getCurrentUser();
        userSongItems = Model.instance.getUserSongItems(currentUser.getUserId());
        userMixtapeItems = Model.instance.getUserMixtapeItems(currentUser.getUserId());
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<SongItem>> getUserSongItems() {
        return userSongItems;
    }

    public LiveData<List<MixtapeItem>> getUserMixtapeItems() {
        return userMixtapeItems;
    }

    public List<Song> getUserSongs() {
        return userSongItems.getValue().stream().map(SongItem::getSong).collect(Collectors.toList());
    }

    public boolean existingMixtapeName(String mixtapeName) {
        return (userMixtapeItems.getValue().stream().map(m -> m.getMixtape().getName()).anyMatch(s -> (s.equals(mixtapeName))));
    }
}
