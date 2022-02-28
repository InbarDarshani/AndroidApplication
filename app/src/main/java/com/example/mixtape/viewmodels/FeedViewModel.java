package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.SongItem;
import com.example.mixtape.model.Model;

import java.util.List;

public class FeedViewModel extends ViewModel {
    private LiveData<List<SongItem>> songItems;

    public FeedViewModel() {
        songItems = Model.instance.getFeed();
    }

    public LiveData<List<SongItem>> getSongItems() {
        return songItems;
    }
}