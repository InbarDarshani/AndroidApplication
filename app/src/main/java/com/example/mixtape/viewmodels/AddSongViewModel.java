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
    private LiveData<List<MixtapeItem>> userMixtapeItems;

    public AddSongViewModel() {
        currentUser = Model.instance.getCurrentUser();
        userMixtapeItems = Model.instance.getUserMixtapeItems(currentUser.getUserId());
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<MixtapeItem>> getUserMixtapeItems() {
        return userMixtapeItems;
    }

    public List<Mixtape> getUserMixtapes() {
        return userMixtapeItems.getValue().stream().map(MixtapeItem::getMixtape).collect(Collectors.toList());
    }

    public String[] getMixtapesOptions() {
        if (userMixtapeItems.getValue() == null)
            return new String[0];
        return userMixtapeItems.getValue().stream().map(m -> m.getMixtape().getName()).toArray(String[]::new);
    }

    public boolean existingMixtapeName(String mixtapeName) {
        return (userMixtapeItems.getValue().stream().map(MixtapeItem::getMixtape)
                .anyMatch(m -> (m.getName().equals(mixtapeName))));
    }
}
