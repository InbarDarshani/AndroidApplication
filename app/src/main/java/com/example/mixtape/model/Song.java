package com.example.mixtape.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Song {
    public final static String COLLECTION_NAME = "songs";

    //Properties
    @PrimaryKey
    @NonNull
    public String songId = "";
    public String name = "";
    public String artist = "";
    public String caption = "";
    public String image = "";
    public Long timeModified = new Long(0);
    public Long timeCreated = new Long(0);
    private Boolean deleted = false;

    //Relations
    public String userId = "";      //The User created this song post
    public String mixtapeId = "";   //The containing mixtape of this song

    //_________________________ Functions _________________________
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("songId", songId);
        json.put("name", name);
        json.put("artist", artist);
        json.put("caption", caption);
        json.put("image", image);
        json.put("userId", userId);
        json.put("mixtapeId", mixtapeId);
        json.put("deleted", deleted);
        json.put("timeModified", FieldValue.serverTimestamp());
        json.put("timeCreated", FieldValue.serverTimestamp());
        return json;
    }

    public static Song create(Map<String, Object> json) {
        String songId = (String) json.get("songId");
        String name = (String) json.get("name");
        String artist = (String) json.get("artist");
        String caption = (String) json.get("caption");
        String image = (String) json.get("image");
        String userId = (String) json.get("userId");
        String mixtapeId = (String) json.get("mixtapeId");
        Boolean deleted = (Boolean) json.get("deleted");
        Long timeModified = ((Timestamp) json.get("timeModified")).getSeconds();
        Long timeCreated = ((Timestamp) json.get("timeCreated")).getSeconds();

        Song song = new Song(name, artist, caption, userId);
        song.setSongId(songId);
        song.setTimeModified(timeModified);
        song.setTimeCreated(timeCreated);
        song.setImage(image);
        song.setMixtapeId(mixtapeId);
        song.setDeleted(deleted);
        return song;
    }

    //_________________________ Constructors _________________________
    public Song() {
    }

    @Ignore
    public Song(String name, String artist, String caption) {
        this.name = name;
        this.artist = artist;
        this.caption = caption;
    }

    @Ignore
    public Song(String name, String artist, String caption, String userId) {
        this.name = name;
        this.artist = artist;
        this.caption = caption;
        this.userId = userId;
    }

    //_________________________ Getters & Setters _________________________
    @NonNull
    public String getSongId() {
        return songId;
    }

    public void setSongId(@NonNull String songId) {
        this.songId = songId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getTimeModified() {
        return timeModified;
    }

    public void setTimeModified(Long timeModified) {
        this.timeModified = timeModified;
    }

    public Long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMixtapeId() {
        return mixtapeId;
    }

    public void setMixtapeId(String mixtapeId) {
        this.mixtapeId = mixtapeId;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}

