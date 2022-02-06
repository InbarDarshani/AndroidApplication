package com.example.mixtape.model;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;


//Working with remote service by google - db, userAuth, storage

public class ModelFirebase {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();     //For working with database
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();            //For working with user authentication
    private FirebaseStorage storage = FirebaseStorage.getInstance();    //For working with images storage

    public ModelFirebase() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
    }

    //_________________________ AUTHENTICATION _____________________________________________________
    public boolean isSignedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("TAG", "isSignedIn check: " + currentUser);
        return (currentUser != null);
    }

    public void signIn(String email, String password, Model.SignIn listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    User u = null;
                    if (task.isSuccessful()) {
                        Log.d("TAG", "signInWithEmail:success " + email);
                        FirebaseUser fbUser = mAuth.getCurrentUser();
                        //TODO: set display name and image
                        u = new com.example.mixtape.model.User(fbUser.getUid(),fbUser.getEmail(),"Admin User", "");
                    } else {
                        Log.d("TAG", "signInWithEmail:failure " + email, task.getException());
                    }
                        listener.onComplete(u);
                });
    }


    //_________________________ STORAGE ____________________________________________________________
    //TODO

    //_________________________ DATABASE ___________________________________________________________

    //TODO: get feed for specific user (Timestamp lastUpdate, User userId, Model.GetFeed listener)
    public void getFeed(Long lastUpdate, Model.GetSongs listener) {
        db.collection(Song.COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("timeCreated", new Timestamp(lastUpdate, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Song> list = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Song song = Song.create(doc.getData());
                            song.setSongId(doc.getId());
                            if (song != null) {
                                list.add(song);
                            }
                        }
                    }
                    listener.onComplete(list);
                });
    }

    public void getProfile(Long lastUpdate, String userId, Model.GetMixtapes listener) {
        db.collection(Mixtape.COLLECTION_NAME)
                .whereEqualTo("userId", userId)  //TODO:Check equal
                .whereGreaterThanOrEqualTo("timeCreated", new Timestamp(lastUpdate, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Mixtape> list = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Mixtape mixtape = Mixtape.create(doc.getData());
                            if (mixtape != null) {
                                list.add(mixtape);
                            }
                        }
                    }
                    listener.onComplete(list);
                });
    }

    public void getSongById(String songId, Model.GetSong listener) {

    }

    public void addSongPost(Song song, Model.AddSong listener) {

    }

    public void getMixtapeById(String mixtapeId, Model.GetMixtape listener) {
    }

    public void addMixtape(Song song, Model.AddMixtape listener) {

    }

    public void getSongsOfMixtape(Long lastUpdate, String mixtapeId, Model.GetSongs listener) {

    }


}
