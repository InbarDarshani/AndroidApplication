package com.example.mixtape.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Object containing all required objects of a mixtape
public class MixtapeItem {
    public Mixtape mixtape;
    public List<Song> songs;
    public User user;

    public MixtapeItem() {
    }

    public MixtapeItem(Mixtape mixtape, List<Song> songs, User user) {
        this.mixtape = mixtape;
        this.songs = songs;
        this.user = user;
    }

    public Mixtape getMixtape() {
        return mixtape;
    }

    public void setMixtape(Mixtape mixtape) {
        this.mixtape = mixtape;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getSongsNames(){
        return songs.stream().map(Song::getName).toArray(String[]::new);
    }
}
