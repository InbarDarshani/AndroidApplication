package com.example.mixtape.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
public class Mixtape {
    public final static String COLLECTION_NAME = "mixtapes";

    //Properties
    @PrimaryKey
    @NonNull
    public String mixtapeId = "";
    public String name = "";
    public String description = "";
    public Long timeModified = new Long( 0);
    public Long timeCreated = new Long( 0);

    //Relations
    public String userId = "";      //The User created this mixtape

    //_________________________ Functions _________________________
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("mixtapeId", mixtapeId);
        json.put("name", name);
        json.put("description", description);
        json.put("timeModified", FieldValue.serverTimestamp());
        json.put("timeCreated", FieldValue.serverTimestamp());
        json.put("userId", userId);
        return json;
    }

    public static Mixtape create(Map<String, Object> json) {
        String mixtapeId = (String) json.get("mixtapeId");
        String name = (String) json.get("name");
        String description = (String) json.get("description");
        Long timeModified = ((Timestamp) json.get("timeModified")).getSeconds();
        Long timeCreated = ((Timestamp) json.get("timeCreated")).getSeconds();
        String userId = (String) json.get("userId");

        Mixtape mixtape = new Mixtape(name, description);
        mixtape.setMixtapeId(mixtapeId);
        mixtape.setTimeModified(timeModified);
        mixtape.setTimeCreated(timeCreated);
        mixtape.setUserId(userId);
        return mixtape;
    }

    //_________________________ Constructors _________________________
    public Mixtape() {}

    @Ignore
    public Mixtape(String name, String description) {
        this.name = name;
        this.description = description;
    }

    //_________________________ Getters & Setters _________________________
    public String getMixtapeId() {
        return mixtapeId;
    }

    public void setMixtapeId(String mixtapeId) {
        this.mixtapeId = mixtapeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
