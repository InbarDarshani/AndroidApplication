package com.example.mixtape.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.mixtape.model.Model;
import com.example.mixtape.model.Mixtape;

import java.util.List;

public class ProfileViewModel {
    LiveData<List<Mixtape>> mixtapes;

    public ProfileViewModel(){
        mixtapes = Model.instance.getProfile();
    }
    public LiveData<List<Mixtape>> getMixtapes() {
        return mixtapes;
    }
}
