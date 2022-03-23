package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.User;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class EditMixtapeViewModel extends ViewModel {
    private LiveData<MixtapeItem> mixtapeItem;
    private Mixtape mixtape = new Mixtape();
    private List<Song> songs = new ArrayList<>();

    private User currentUser;
    private LiveData<List<SongItem>> userSongItems;
    private LiveData<List<MixtapeItem>> userMixtapeItems;

    public EditMixtapeViewModel(String mixtapeId) {
        mixtapeItem = Model.instance.getMixtapeItem(mixtapeId);
        mixtapeItem.observeForever(mixtapeItem ->{
            mixtape = mixtapeItem.getMixtape();
            songs = mixtapeItem.getSongs();
        });

        currentUser = Model.instance.getCurrentUser();
        userMixtapeItems = Model.instance.getUserMixtapeItems(currentUser.getUserId());
        userSongItems = Model.instance.getUserSongItems(currentUser.getUserId());
    }

    public LiveData<MixtapeItem> getMixtapeItem() {
        return mixtapeItem;
    }

    public Mixtape getMixtape() {
        return mixtape;
    }

    public List<Song> getSongs() {
        return songs;
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
        return (userMixtapeItems.getValue().stream().map(MixtapeItem::getMixtape)
                .anyMatch(m -> (m.getName().equals(mixtapeName) && !m.getMixtapeId().equals(mixtape.getMixtapeId()))));
    }

    public boolean mixtapeContainsSong(String songId){
        return songs.stream().anyMatch(s -> s.getSongId().equals(songId));
    }
}
