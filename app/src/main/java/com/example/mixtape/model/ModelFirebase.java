package com.example.mixtape.model;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

//Working with remote service by google - db, userAuth, storage

public class ModelFirebase {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();     //For working with database
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();            //For working with user authentication
    private final FirebaseStorage storage = FirebaseStorage.getInstance();    //For working with images storage

    public ModelFirebase() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
    }

    /*____________________________________________________________________________________________*/
    /*______________________________________ AUTHENTICATION ______________________________________*/
    /*____________________________________________________________________________________________*/
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signIn(String email, String password, Model.UserProcess listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("TAG", "Firebase - sign-in failed");
                        signInsignUpError(task);
                        listener.onComplete(null);
                        return;
                    }
                    Log.d("TAG", "Firebase - sign-in success");
                    //Get User object
                    String userId = mAuth.getCurrentUser().getUid();
                    this.getUserById(userId, listener::onComplete);
                });
    }

    public void signUp(String fullName, String email, String password, Model.UserProcess listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("TAG", "Firebase - sign-up failed");
                        signInsignUpError(task);
                        listener.onComplete(null);
                        return;
                    }
                    Log.d("TAG", "Firebase - sign-up success");
                    //Create User object
                    FirebaseUser fbUser = mAuth.getCurrentUser();
                    String userId = fbUser.getUid();
                    String image = "";
                    User user = new User(userId, email, fullName, image);
                    addUser(user);                             //Add user to user's collection
                    listener.onComplete(user);
                });
    }

    private void signInsignUpError(Task<AuthResult> task) {
        Log.d("TAG", "Firebase - failed to sign-in " + task.getException());

        //Get error
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

    public void signOut() {
        mAuth.signOut();
        Log.d("TAG", "Firebase - sign-out performed");
    }

    /*____________________________________________________________________________________________*/
    /*__________________________________________ STORAGE _________________________________________*/
    /*____________________________________________________________________________________________*/

    public void saveImage(Bitmap imageBitmap, String folder, String imageName, Model.SaveImageListener listener) {
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child(folder + "/" + imageName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imgRef.putBytes(data);
        uploadTask
                .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    listener.onComplete(uri.toString());
                }))
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to upload image " + e.getMessage()));
        //.addOnFailureListener(exception -> listener.onComplete(null))
    }

    /*____________________________________________________________________________________________*/
    /*___________________________________________ DATA ___________________________________________*/
    /*____________________________________________________________________________________________*/

    //_________ New Documents Fetching _________
    public void getFeedSongs(Long lastUpdate, Model.GetSongs listener) {
        db.collection(Song.COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("timeCreated", new Timestamp(lastUpdate, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Song> songs = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Song song = Song.create(documentSnapshot.getData());
                            song.setSongId(documentSnapshot.getId());
                            songs.add(song);
                        }
                    }
                    listener.onComplete(songs);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get feed songs " + e.getMessage()));
    }

    public void getProfileMixtapes(Long lastUpdate, String userId, Model.GetMixtapes listener) {
        db.collection(Mixtape.COLLECTION_NAME)
                .whereEqualTo("userId", userId)  //TODO:Check equal
                .whereGreaterThanOrEqualTo("timeCreated", new Timestamp(lastUpdate, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Mixtape> mixtapes = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Mixtape mixtape = Mixtape.create(documentSnapshot.getData());
                            mixtape.setMixtapeId(documentSnapshot.getId());
                            mixtapes.add(mixtape);
                        }
                    }
                    listener.onComplete(mixtapes);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get profile mixtapes " + "\n\t" + e.getMessage()));
    }

    public void getAllSongs(Model.GetSongs listener) {
        db.collection(Song.COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    List<Song> songs = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Song song = Song.create(documentSnapshot.getData());
                            song.setSongId(documentSnapshot.getId());
                            songs.add(song);
                        }
                    }
                    listener.onComplete(songs);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get all songs " + e.getMessage()));
    }


    //_________ Multiple Documents Fetching _________
    public void getSongsByIds(List<String> songIds, Model.GetSongs listener) {
        db.collection(Song.COLLECTION_NAME).whereIn("songId", songIds).get()
                .addOnCompleteListener(task -> {
                    List<Song> songs = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Song song = Song.create(documentSnapshot.getData());
                            song.setSongId(documentSnapshot.getId());
                            songs.add(song);
                        }
                    }
                    listener.onComplete(songs);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get songs " + "\n\t" + e.getMessage()));
    }

    public void getMixtapesByIds(List<String> mixtapeIds, Model.GetMixtapes listener) {
        db.collection(Mixtape.COLLECTION_NAME).whereIn("mixtapeId", mixtapeIds).get()
                .addOnCompleteListener(task -> {
                    List<Mixtape> mixtapes = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            Mixtape mixtape = Mixtape.create(documentSnapshot.getData());
                            mixtape.setMixtapeId(documentSnapshot.getId());
                            mixtapes.add(mixtape);
                        }
                    }
                    listener.onComplete(mixtapes);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get mixtapes " + "\n\t" + e.getMessage()));
    }

    public void getUsersByIds(List<String> userIds, Model.GetUsers listener) {
        db.collection(User.COLLECTION_NAME).whereIn("userId", userIds).get()
                .addOnCompleteListener(task -> {
                    List<User> users = new LinkedList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            User user = User.create(documentSnapshot.getData());
                            user.setUserId(documentSnapshot.getId());
                            users.add(user);
                        }
                    }
                    listener.onComplete(users);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get users " + "\n\t" + e.getMessage()));
    }

    //_________ Single Document Fetching _________
    public void getSongById(String songId, Model.GetSong listener) {
        db.collection(Song.COLLECTION_NAME)
                .document(songId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Song song = Song.create(documentSnapshot.getData());
                    song.setSongId(documentSnapshot.getId());
                    listener.onComplete(song);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get song " + songId + "\n\t" + e.getMessage()));
    }

    public void getMixtapeById(String mixtapeId, Model.GetMixtape listener) {
        db.collection(Mixtape.COLLECTION_NAME)
                .document(mixtapeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Mixtape mixtape = Mixtape.create(documentSnapshot.getData());
                    mixtape.setMixtapeId(documentSnapshot.getId());
                    listener.onComplete(mixtape);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get mixtape " + mixtapeId + "\n\t" + e.getMessage()));
    }

    public void getUserById(String userId, Model.GetUser listener) {
        db.collection(User.COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = User.create(documentSnapshot.getData());
                    user.setUserId(documentSnapshot.getId());
                    listener.onComplete(user);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get user " + userId + "\n\t" + e.getMessage()));
    }

    //_________ Document Creation _________
    public void addSong(@NonNull Song song, Model.AddSong listener) {
        DocumentReference addedDocRef = db.collection(Song.COLLECTION_NAME).document();
        song.setSongId(addedDocRef.getId());

        Map<String, Object> json = song.toJson();
        addedDocRef
                .set(json)
                .addOnSuccessListener(unused -> listener.onComplete(song.getSongId()))
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to add song " + e.getMessage()));
    }

    public void addMixtape(@NonNull Mixtape mixtape, Model.AddMixtape listener) {
        DocumentReference addedDocRef = db.collection(Mixtape.COLLECTION_NAME).document();
        mixtape.setMixtapeId(addedDocRef.getId());

        Map<String, Object> json = mixtape.toJson();
        addedDocRef
                .set(json)
                .addOnSuccessListener(unused -> listener.onComplete(mixtape.getMixtapeId()))
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to add mixtape " + mixtape.getMixtapeId() + "\n\t" + e.getMessage()));
    }

    private void addUser(@NonNull User user) {
        DocumentReference addedDocRef = db.collection(User.COLLECTION_NAME).document(user.getUserId());
        Map<String, Object> json = user.toJson();
        addedDocRef
                .set(json)
                .addOnSuccessListener(unused -> Log.d("TAG", "Firebase - add user " + user.getUserId()))
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to add user " + user.getUserId() + "\n\t" + e.getMessage()));
    }

    public void getMixtapesOfUser(String userId, Model.GetMixtapes listener) {
        db.collection(Mixtape.COLLECTION_NAME).whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Mixtape> mixtapes = new LinkedList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Mixtape mixtape = Mixtape.create(document.getData());
                        mixtape.setMixtapeId(document.getId());
                        mixtapes.add(mixtape);
                    }
                    listener.onComplete(mixtapes);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get get mixtapes of user " + userId + "\n\t" + e.getMessage()));
    }

    public void getSongsOfMixtape(String mixtapeId, Model.GetSongs listener) {
        db.collection(Song.COLLECTION_NAME).whereEqualTo("mixtapeId", mixtapeId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Song> songs = new LinkedList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Song song = Song.create(document.getData());
                        song.setSongId(document.getId());
                        songs.add(song);
                    }
                    listener.onComplete(songs);
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to get get songs of mixtape " + mixtapeId + "\n\t" + e.getMessage()));
    }

    //_________ Document Updating _________
    public void updateSong(Song song, Model.UpdateSong listener){
        db.collection(Song.COLLECTION_NAME)
                .document(song.getSongId())
                .set(song.toJson())
                .addOnSuccessListener(documentSnapshot -> {
                    listener.onComplete();
                })
                .addOnFailureListener(e -> Log.d("TAG", "Firebase - failed to update song " + song.getSongId() + "\n\t" + e.getMessage()));
    }

}
