package com.example.mixtape.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

//Factory in order to create ViewModel with arguments
public class MixtapeDetailsViewModelFactory implements ViewModelProvider.Factory {
    private String mixtapeId;

    public MixtapeDetailsViewModelFactory(String mixtapeId) {
        this.mixtapeId = mixtapeId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MixtapeDetailsViewModel(mixtapeId);
    }
}
