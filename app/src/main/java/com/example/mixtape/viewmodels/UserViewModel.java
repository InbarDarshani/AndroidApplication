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

public class UserViewModel extends ViewModel {
    private User user;
    private MutableLiveData<List<MixtapeItem>> mixtapeItems = new MutableLiveData<>();

    public UserViewModel(String userId) {
        Model.instance.getUser(userId, dbUser -> {
            user = dbUser;
            mixtapeItems = Model.instance.getUserProfile(user.getUserId());
        });
    }

    public void refresh() {
        Model.instance.getUserProfile(user.getUserId(), localMixtapeItems -> {
            mixtapeItems.postValue(localMixtapeItems);
        });
    }

    public User getUser() {
        return user;
    }

    public LiveData<List<MixtapeItem>> getMixtapeItems() {
        return mixtapeItems;
    }

}
