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
    final public static String COLLECTION_NAME = "songs";

    //Properties
    @PrimaryKey
    @NonNull
    String songId = "";
    String name = "";
    String artist = "";
    String caption = "";
    String image = "";       //TODO:
    Long timeModified = new Long(0);
    Long timeCreated = new Long(0);

    //Relations
    String userId = "";      //The User created this song post
    String mixtapeId = "";   //The containing mixtape of this song

    //_________________________ Functions _________________________
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("songId", songId);
        json.put("name", name);
        json.put("artist", artist);
        json.put("caption", caption);
        json.put("image", image);
        json.put("timeModified", FieldValue.serverTimestamp());
        json.put("timeCreated", FieldValue.serverTimestamp());
        json.put("userId", userId);
        return json;
    }

    public static Song create(Map<String, Object> json) {
        String songId = (String) json.get("songId");
        String name = (String) json.get("name");
        String artist = (String) json.get("artist");
        String caption = (String) json.get("caption");
        String image = (String) json.get("image");
        Timestamp timeModified = (Timestamp) json.get("timeModified");
        Timestamp timeCreated = (Timestamp) json.get("timeCreated");
        String userId = (String) json.get("userId");
        String mixtapeId = (String) json.get("mixtapeId");
        return new Song(songId, name, artist, caption, image, timeModified.getSeconds(), timeCreated.getSeconds(), userId, mixtapeId);
    }

    //_________________________ Constructors _________________________
    public Song() {}

    @Ignore
    public Song(@NonNull String songId, String name, String artist, String caption, String image, Long timeModified, Long timeCreated, String userId, String mixtapeId) {
        this.songId = songId;
        this.name = name;
        this.artist = artist;
        this.caption = caption;
        this.image = image;
        this.timeModified = timeModified;
        this.timeCreated = timeCreated;
        this.userId = userId;
        this.mixtapeId = mixtapeId;
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

}
