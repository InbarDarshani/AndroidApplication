package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
            mixtapeItems = Model.instance.getUserMixtapeItems(user.getUserId());
        });
    }

    public void refresh() {
        Model.instance.getUserMixtapeItems(user.getUserId(), localMixtapeItems -> {
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
