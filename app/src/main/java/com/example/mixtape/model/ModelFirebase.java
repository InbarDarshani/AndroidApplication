package com.example.mixtape.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.LinkedList;
import java.util.List;

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

    public void signIn(String email, String password, Model.UserProcess listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    User u = signInsignUpOnComplete(task);
                    listener.onComplete(u);
                });
    }

    public void signUp(String email, String password, Model.UserProcess listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    User u = signInsignUpOnComplete(task);
                    listener.onComplete(u);
                });
    }

    private User signInsignUpOnComplete(Task<AuthResult> task){
        User u = null;
        if (task.isSuccessful()) {
            Log.d("TAG", "signInWithEmail:success ");

            FirebaseUser fbUser = mAuth.getCurrentUser();
            assert fbUser != null: "FirebaseUser current user is null!";
            u = new com.example.mixtape.model.User(fbUser.getUid(), fbUser.getEmail(), "Admin User", "");
            //TODO: set display name and image
        } else {
            Log.d("TAG", "signInWithEmail:failure ", task.getException());

            Exception e = task.getException();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Model.instance.dbError = "Invalid password";
            } else if (e instanceof FirebaseAuthInvalidUserException) {
                String errorCode = ((FirebaseAuthInvalidUserException) e).getErrorCode();
                switch (errorCode) {
                    case "ERROR_USER_NOT_FOUND":
                        Model.instance.dbError = "No matching account found";
                        break;
                    case "ERROR_USER_DISABLED":
                        Model.instance.dbError = "User account has been disabled";
                        break;
                    case "ERROR_EMAIL_ALREADY_IN_USE":
                        Model.instance.dbError = "Email is already in use";
                        break;
                    default:
                        Model.instance.dbError = e.getMessage();
                        break;
                }
            }
        }
        return u;
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
