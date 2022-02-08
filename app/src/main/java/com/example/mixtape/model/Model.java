package com.example.mixtape.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mixtape.MyApplication;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

//Holds the data

public class Model {
    public static final Model instance = new Model();
    public Executor executor = Executors.newFixedThreadPool(1);
    public Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
    ModelFirebase modelFirebase = new ModelFirebase();

    public String dbError = "";

    private Model() {
        //Set data loading states
        feedLoadingState.setValue(FeedState.loaded);
        profileLoadingState.setValue(ProfileState.loaded);
        userLoginState.setValue(LoginState.none);
    }

    //_________________________ Data Holders _________________________
    MutableLiveData<List<Song>> feed = new MutableLiveData<>();
    MutableLiveData<List<Mixtape>> profile = new MutableLiveData<>();

    //_________________________ User Login States _________________________
    public enum LoginState {
        none,
        inprogress,
        completed,
        error
    }

    MutableLiveData<LoginState> userLoginState = new MutableLiveData<>();

    public MutableLiveData<LoginState> getUserLoginState() {
        return userLoginState;
    }

    //_________________________ User Functions _________________________

    public boolean isSignedIn() {
        return modelFirebase.isSignedIn();
    }

    public void signInsignUp(String email, String password, boolean newUser) {
        userLoginState.setValue(LoginState.inprogress);

        //Sign up with firebase
        if (newUser) {
            modelFirebase.signUp(email, password, user -> {
                if (user == null) {
                    userLoginState.setValue(LoginState.error);
                } else {
                    saveUser(user);
                    userLoginState.setValue(LoginState.completed);
                }
            });
            return;
        }

        //Sign in with firebase
        modelFirebase.signIn(email, password, user -> {
            if (user == null) {
                userLoginState.setValue(LoginState.error);
            } else {
                saveUser(user);
                userLoginState.setValue(LoginState.completed);
            }
        });
    }

    //TODO: Save all in local db instead?
    //Update user details in device's shared preferences
    private void saveUser(User user) {
        executor.execute(() -> {
            MyApplication.getContext()
                    .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    .edit()
                    .putString("UserId", user.userId)
                    .putString("UserDisplayName", user.displayName)
                    .putString("UserEmail", user.email)
                    .commit();
            //TODO: save user's image in device local db
        });
    }

    //_________________________ Data Loading States _________________________
    //properties for representing the loading state of each LiveData
    public enum FeedState {
        loading,
        loaded
    }

    public enum ProfileState {
        loading,
        loaded
    }

    MutableLiveData<FeedState> feedLoadingState = new MutableLiveData<>();

    public MutableLiveData<FeedState> getFeedLoadingState() {
        return feedLoadingState;
    }

    MutableLiveData<ProfileState> profileLoadingState = new MutableLiveData<>();

    public MutableLiveData<ProfileState> getProfileLoadingState() {
        return profileLoadingState;
    }

    //_________________________ Data Functions _________________________
    //functions for data getters and setters

    public LiveData<List<Song>> getFeed() {
        if (feed.getValue() == null) {
            refreshFeed();
        }
        return feed;
    }

    public Song getSong(String songId, GetSong listener) {
        modelFirebase.getSongById(songId, listener);
        return null;
    }

    public void addSong(Song song, AddSong listener) {
        modelFirebase.addSongPost(song, listener);
    }

    public LiveData<List<Mixtape>> getProfile() {
        if (profile.getValue() == null) {
            refreshProfile();
        }
        return profile;
    }

    public Mixtape getMixtape(String mixtapeId, GetMixtape listener) {
        modelFirebase.getMixtapeById(mixtapeId, listener);
        return null;
    }

    public void addMixtape(Song song, AddMixtape listener) {
        modelFirebase.addMixtape(song, listener);
    }

    //_________________________ Data Refresh Functions _________________________
    //functions for local data refresh on the device
    public void refreshFeed() {
        feedLoadingState.setValue(FeedState.loading);

        // get last local update date from the device
        Long lastUpdateDate = MyApplication.getContext().getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong("FeedLastUpdateDate", 0);

        // firebase get all updates since lastLocalUpdateDate
        modelFirebase.getFeed(lastUpdateDate, (GetSongs) list -> {
            Log.d("TAG", "firebase returned " + list.size() + " songs to feed");

            // add all records to the local db
            executor.execute(() -> {
                Long lastLocalUpdate = new Long(0);

                for (Song song : list) {
                    //add to local db
                    AppLocalDb.db.songDao().insertMany(song);
                    //set the latest update time
                    lastLocalUpdate = (lastLocalUpdate < song.getTimeCreated()) ? song.getTimeCreated() : lastLocalUpdate;
                }

                // update last local update date
                MyApplication.getContext()
                        .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                        .edit()
                        .putLong("FeedLastUpdateDate", lastLocalUpdate)
                        .commit();

                //return all data to caller
                List<Song> newSongs = AppLocalDb.db.songDao().getAll();
                feed.postValue(newSongs);
                feedLoadingState.postValue(FeedState.loaded);
            });
        });
    }

    public void refreshProfile() {
        profileLoadingState.setValue(ProfileState.loading);

        // get last local update date from the device
        Long lastUpdateDate = MyApplication.getContext().getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong("ProfileLastUpdateDate", 0);
        String userId = MyApplication.getContext().getSharedPreferences("TAG", Context.MODE_PRIVATE).getString("UserId", "");

        // firebase get all updates since lastLocalUpdateDate //TODO: get profile by userID
        modelFirebase.getProfile(lastUpdateDate, userId, (GetMixtapes) list -> {
            Log.d("TAG", "firebase returned " + list.size() + " mixtapes to profile");

            // add all records to the local db
            executor.execute(() -> {
                Long lastLocalUpdate = new Long(0);

                for (Mixtape mixtape : list) {
                    //add to local db
                    AppLocalDb.db.mixtapeDao().insertMany(mixtape);
                    //set the latest update time
                    lastLocalUpdate = (lastLocalUpdate < mixtape.getTimeCreated()) ? mixtape.getTimeCreated() : lastLocalUpdate;
                }

                // update last local update date
                MyApplication.getContext()
                        .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                        .edit()
                        .putLong("ProfileLastUpdateDate", lastLocalUpdate)
                        .commit();

                //return all data to caller
                List<Mixtape> newMixtapes = AppLocalDb.db.mixtapeDao().getAll();
                profile.postValue(newMixtapes);
                profileLoadingState.postValue(ProfileState.loaded);
            });
        });
    }

    //_________________________ Listener Interfaces _________________________
    //interface for each remote data fetching\pushing action wrapping its events

    public interface GetSongs {
        void onComplete(List<Song> list);
    }

    public interface GetMixtapes {
        void onComplete(List<Mixtape> list);
    }

    public interface GetSong {
        void onComplete(String songId);
    }

    public interface AddSong {
        void onComplete();
    }

    public interface GetMixtape {
        void onComplete(String mixtapeId);
    }

    public interface AddMixtape {
        void onComplete();
    }

    public interface UserProcess {
        void onComplete(User user);
    }


}
