package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;

import java.util.List;

public class FeedViewModel extends ViewModel {
    LiveData<List<Song>> songs;

    public FeedViewModel(){
        songs = Model.instance.getFeed();
    }
    public LiveData<List<Song>> getSongs() {
        return songs;
    }

}
