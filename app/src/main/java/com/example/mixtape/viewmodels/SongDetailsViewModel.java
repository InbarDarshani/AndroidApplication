package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.User;

public class SongDetailsViewModel extends ViewModel {
    private String songId = "";

    private MutableLiveData<SongItem> songItem = new MutableLiveData<>();
    private Song song = new Song();
    private Mixtape mixtape = new Mixtape();
    private User user = new User();

    public SongDetailsViewModel(String songId) {
        this.songId = songId;

        //Setup observer that will be triggered on refresh
        songItem.observeForever(songItem -> {
            song = songItem.getSong();
            mixtape = songItem.getMixtape();
            user = songItem.getUser();
        });
    }

    public void refresh() {
        Model.instance.getSongItem(songId, dbSongItem -> songItem.postValue(dbSongItem));
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

    public User getUser() {
        return user;
    }

}
