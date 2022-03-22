package com.example.mixtape.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

//Factory in order to create ViewModel with arguments
public class UserViewModelFactory implements ViewModelProvider.Factory {
    private String userId;

    public UserViewModelFactory(String userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserViewModel(userId);
    }

}
