package com.example.mixtape.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

@Entity
public class User {
    final public static String COLLECTION_NAME = "users";

    //Properties
    @PrimaryKey
    @NonNull
    String userId = "";
    String email = "";
    String displayName = "";
    //TODO: setup profile picture upload 
    String image = "";

    //_________________________ Functions _________________________
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("userId", userId);
        json.put("email", email);
        json.put("displayName", displayName);
        json.put("image", image);
        return json;
    }

    public static User create(Map<String, Object> json) {
        String userId = (String) json.get("userId");
        String email = (String) json.get("email");
        String displayName = (String) json.get("displayName");
        String image = (String) json.get("image");
        User user = new User(userId, email, displayName, image);
        return user;
    }

    //_________________________ Constructors _________________________
    public User() {}

    @Ignore
    public User(String userId, String email, String displayName, String image) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.image = image;
    }

    //_________________________ Getters & Setters _________________________
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
