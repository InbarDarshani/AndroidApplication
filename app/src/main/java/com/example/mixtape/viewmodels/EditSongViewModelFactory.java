package com.example.mixtape.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

//Factory in order to create ViewModel with arguments
public class EditSongViewModelFactory implements ViewModelProvider.Factory {
    private String songId;

    public EditSongViewModelFactory(String songId) {
        this.songId = songId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new EditSongViewModel(songId);
    }
}
