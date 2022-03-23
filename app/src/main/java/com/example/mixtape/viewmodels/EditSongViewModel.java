package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class EditSongViewModel extends ViewModel {
    private LiveData<SongItem> songItem;
    private Song song = new Song();
    private Mixtape mixtape = new Mixtape();

    private User currentUser;
    private LiveData<List<MixtapeItem>> userMixtapeItems;

    public EditSongViewModel(String songId) {
        songItem = Model.instance.getSongItem(songId);
        songItem.observeForever(songItem -> {
            song = songItem.getSong();
            mixtape = songItem.getMixtape();
        });

        currentUser = Model.instance.getCurrentUser();
        userMixtapeItems = Model.instance.getUserMixtapeItems(currentUser.getUserId());
    }

    public LiveData<SongItem> getSongItem() {
        return songItem;
    }

    public Song getSong() {
        return song;
    }

    public Mixtape getMixtape() {
        return mixtape;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<MixtapeItem>> getUserMixtapeItems() {
        return userMixtapeItems;
    }

    public List<Mixtape> getUserMixtapes() {
        return userMixtapeItems.getValue().stream().map(MixtapeItem::getMixtape).collect(Collectors.toList());
    }

    public String[] getMixtapesOptions() {
        if (userMixtapeItems.getValue() == null)
            return new String[0];
        return userMixtapeItems.getValue().stream().map(m -> m.getMixtape().getName()).toArray(String[]::new);
    }

    public boolean existingMixtapeName(String mixtapeName) {
        return (userMixtapeItems.getValue().stream().map(MixtapeItem::getMixtape)
                .anyMatch(m -> (m.getName().equals(mixtapeName))));
    }

}
