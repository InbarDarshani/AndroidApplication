package com.example.mixtape.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

//Factory in order to create ViewModel with arguments
public class SongDetailsViewModelFactory implements ViewModelProvider.Factory {
    private String songId;

    public SongDetailsViewModelFactory(String mixtapeId) {
        this.songId = mixtapeId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SongDetailsViewModel(songId);
    }
}
