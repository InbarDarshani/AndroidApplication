package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.User;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private User user;
    private LiveData<List<MixtapeItem>> mixtapeItems;

    public ProfileViewModel() {
        user = Model.instance.getCurrentUser();
        mixtapeItems = Model.instance.getProfile();
    }

    public User getUser() {
        return user;
    }

    public LiveData<List<MixtapeItem>> getMixtapeItems() {
        return mixtapeItems;
    }

}
