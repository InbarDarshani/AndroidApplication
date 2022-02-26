package com.example.mixtape.model;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.User;

//Object containing all required objects to profile mixtape
public class MixtapeItem {
    public Mixtape mixtape;
    public Song song;
    public User user;

    public MixtapeItem() {
    }

    public MixtapeItem(Mixtape mixtape, Song song, User user) {
        this.mixtape = mixtape;
        this.song = song;
        this.user = user;
    }

    public Mixtape getMixtape() {
        return mixtape;
    }

    public void setMixtape(Mixtape mixtape) {
        this.mixtape = mixtape;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
