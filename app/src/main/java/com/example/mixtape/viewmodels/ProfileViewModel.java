package com.example.mixtape.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.MyApplication;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.User;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private User user;
    private LiveData<List<MixtapeItem>> mixtapeItems = new MutableLiveData<>();

    public ProfileViewModel(String userId) {
        Model.instance.getUser(userId, dbUser -> {
            user = dbUser;
            refresh();
        });
    }

    public void refresh() {
        mixtapeItems = Model.instance.getUserProfile(user.getUserId());
    }

    public User getUser() {
        return user;
    }

    public LiveData<List<MixtapeItem>> getMixtapeItems() {
        return mixtapeItems;
    }

}
