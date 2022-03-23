package com.example.mixtape.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

//Factory in order to create ViewModel with arguments
public class EditMixtapeViewModelFactory implements ViewModelProvider.Factory {
    private String mixtapeId;

    public EditMixtapeViewModelFactory(String mixtapeId) {
        this.mixtapeId = mixtapeId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new EditMixtapeViewModel(mixtapeId);
    }
}
