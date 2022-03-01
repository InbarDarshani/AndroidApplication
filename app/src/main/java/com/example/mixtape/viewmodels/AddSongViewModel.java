package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class AddSongViewModel extends ViewModel {
    private User currentUser;
    private LiveData<List<MixtapeItem>> mixtapeItems;

    public AddSongViewModel() {
        currentUser = Model.instance.getCurrentUser();
        mixtapeItems = Model.instance.getUserProfile(currentUser.getUserId());
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<MixtapeItem>> getMixtapeItems() {
        return mixtapeItems;
    }

    public List<Mixtape> getMixtapes() {
        return mixtapeItems.getValue().stream().map(MixtapeItem::getMixtape).collect(Collectors.toList());
    }

    public String[] getMixtapesOptions() {
        if (mixtapeItems.getValue() == null)
            return new String[0];
        return mixtapeItems.getValue().stream().map(m -> m.getMixtape().getName()).toArray(String[]::new);
    }
}
