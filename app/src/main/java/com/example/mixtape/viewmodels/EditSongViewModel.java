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
    private LiveData<List<MixtapeItem>> mixtapeItems;

    public EditSongViewModel(String songId) {
        currentUser = Model.instance.getCurrentUser();
        songItem = Model.instance.getSongItem(songId);
        songItem.observeForever(songItem -> {
            song = songItem.getSong();
            mixtape = songItem.getMixtape();
        });
        mixtapeItems = Model.instance.getUserProfile(currentUser.getUserId());
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

    public LiveData<List<MixtapeItem>> getMixtapeItems() {
        return mixtapeItems;
    }

    public List<Mixtape> getMixtapes() {
        return mixtapeItems.getValue().stream().map(MixtapeItem::getMixtape).collect(Collectors.toList());
    }

    public String[] getMixtapesOptions() {
        if (mixtapeItems.getValue() == null)
            return new String[0];
        return mixtapeItems.getValue().stream().map(m -> m.getMixtape().getName()).toArray(String[]::new);
    }

}
