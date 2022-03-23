package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.User;

import java.util.LinkedList;
import java.util.List;

public class MixtapeDetailsViewModel extends ViewModel {
    private String mixtapeId = "";
    private MutableLiveData<MixtapeItem> mixtapeItem = new MutableLiveData<>();
    private Mixtape mixtape = new Mixtape();
    private List<Song> songs = new LinkedList<>();
    private User user = new User();

    public enum SongsState {
        loading,
        empty,
        loaded
    }

    public MutableLiveData<SongsState> songsLoadingState = new MutableLiveData<>();

    public MixtapeDetailsViewModel(String mixtapeId) {
        this.mixtapeId = mixtapeId;

        songsLoadingState.setValue(SongsState.loading);
        mixtapeItem.observeForever(mixtapeItem -> {
            mixtape = mixtapeItem.getMixtape();
            songs = mixtapeItem.getSongs();
            user = mixtapeItem.getUser();

            if (songs.isEmpty())
                songsLoadingState.setValue(SongsState.empty);
            else
                songsLoadingState.setValue(SongsState.loaded);
        });
    }

    public void refresh() {
        songsLoadingState.setValue(SongsState.loading);

        Model.instance.getMixtapeItem(mixtapeId, dbMixtapeItem -> {
            mixtapeItem.postValue(dbMixtapeItem);
        });
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

    public User getUser() {
        return user;
    }
}
