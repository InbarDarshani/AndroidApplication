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
    private String mixtapeId = "";
    private String name = "";
    private String description = "";
    private Long timeModified = new Long(0);
    private Long timeCreated = new Long(0);
    private Boolean deleted = false;

    //Relations
    public String userId = "";      //The User created this mixtape

    //_________________________ Functions _________________________
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("mixtapeId", mixtapeId);
        json.put("name", name);
        json.put("description", description);
        json.put("userId", userId);
        json.put("deleted", deleted);
        json.put("timeModified", FieldValue.serverTimestamp());
        json.put("timeCreated", FieldValue.serverTimestamp());
        return json;
    }

    public static Mixtape create(Map<String, Object> json) {
        String mixtapeId = (String) json.get("mixtapeId");
        String name = (String) json.get("name");
        String description = (String) json.get("description");
        String userId = (String) json.get("userId");
        Boolean deleted = (Boolean) json.get("deleted");
        Long timeModified = ((Timestamp) json.get("timeModified")).getSeconds();
        Long timeCreated = ((Timestamp) json.get("timeCreated")).getSeconds();

        Mixtape mixtape = new Mixtape(name, description, userId);
        mixtape.setMixtapeId(mixtapeId);
        mixtape.setTimeModified(timeModified);
        mixtape.setTimeCreated(timeCreated);
        mixtape.setDeleted(deleted);
        return mixtape;
    }

    //_________________________ Constructors _________________________
    public Mixtape() {
    }

    @Ignore
    public Mixtape(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Ignore
    public Mixtape(String name, String description, String userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
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

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
