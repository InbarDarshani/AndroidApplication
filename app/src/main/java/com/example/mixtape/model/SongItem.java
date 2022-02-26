package com.example.mixtape.model;

import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Song;
import com.example.mixtape.model.User;

//Object containing all required objects to feed song post
public class SongItem {
    public Song song;
    public Mixtape mixtape;
    public User user;

    public SongItem() {
    }

    public SongItem(Song song, Mixtape mixtape, User user) {
        this.song = song;
        this.mixtape = mixtape;
        this.user = user;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Mixtape getMixtape() {
        return mixtape;
    }

    public void setMixtape(Mixtape mixtape) {
        this.mixtape = mixtape;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
