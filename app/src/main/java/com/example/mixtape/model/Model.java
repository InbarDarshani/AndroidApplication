package com.example.mixtape.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mixtape.MyApplication;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Holds the data

public class Model {
    public static final Model instance = new Model();
    private final ModelFirebase modelFirebase = new ModelFirebase();
    public ExecutorService executor = Executors.newFixedThreadPool(1);
    public Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
    public String dbError = "";

//    Future<String> f = executor.submit(()-> "dkbfgvkhdfvb");
//    public void kdhbsdkhv(){
//        //executor
//    }



    private Model() {
        //Set data loading states
        feedLoadingState.setValue(FeedState.loaded);
        profileLoadingState.setValue(ProfileState.loaded);
        userLoginState.setValue(LoginState.none);
    }

    /*____________________________________________________________________________________________*/
    /*______________________________________ AUTHENTICATION ______________________________________*/
    /*____________________________________________________________________________________________*/

    //_________________________ User Login States _________________________
    public enum LoginState {
        none,
        inprogress,
        signedin,
        signedout,
        error
    }

    MutableLiveData<LoginState> userLoginState = new MutableLiveData<>();

    public MutableLiveData<LoginState> getUserLoginState() {
        return userLoginState;
    }

    //_________________________ User Functions _________________________

    public boolean isSignedIn() {
        return (modelFirebase.getCurrentUser() != null);
    }

    public void signInsignUp(String fullName, String email, String password, boolean newUser) {
        userLoginState.setValue(LoginState.inprogress);

        //Sign up with firebase
        if (newUser) {
            modelFirebase.signUp(fullName, email, password, user -> {
                if (user == null) {
                    userLoginState.setValue(LoginState.error);
                } else {
                    saveUser(user);
                    userLoginState.setValue(LoginState.signedin);
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
                userLoginState.setValue(LoginState.signedin);
            }
        });
    }

    public void signOut() {
        userLoginState.setValue(LoginState.inprogress);
        modelFirebase.signOut();
        clearUser();
        userLoginState.setValue(LoginState.signedout);
    }

    //Update user details in device's shared preferences
    private void saveUser(User user) {
        executor.execute(() -> {
            MyApplication.getContext()
                    .getSharedPreferences("USER", Context.MODE_PRIVATE)
                    .edit()
                    .putString("UserId", user.getUserId())
                    .putString("UserDisplayName", user.getDisplayName())
                    .putString("UserEmail", user.getEmail())
                    .commit();
        });
    }

    //Clear user details in device's shared preferences
    private void clearUser() {
        executor.execute(() -> MyApplication.getContext()
                .deleteSharedPreferences("USER"));
    }

    /*____________________________________________________________________________________________*/
    /*__________________________________________ STORAGE _________________________________________*/
    /*____________________________________________________________________________________________*/

    //_________________________ Storage Functions _________________________
    public void saveImage(Bitmap imageBitmap, String folder, String imageName, SaveImageListener listener) {
        modelFirebase.saveImage(imageBitmap, folder, imageName, listener);
    }

    /*____________________________________________________________________________________________*/
    /*___________________________________________ DATA ___________________________________________*/
    /*____________________________________________________________________________________________*/

    //_________________________ Data Holders _________________________
    MutableLiveData<List<SongItem>> feed = new MutableLiveData<>();
    MutableLiveData<List<Mixtape>> profile = new MutableLiveData<>();

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
    public LiveData<List<SongItem>> getFeed() {
        if (feed.getValue() == null)
            refreshFeed();
        return feed;
    }

    public LiveData<List<Mixtape>> getProfile() {
        if (profile.getValue() == null)
            refreshProfile();
        return profile;
    }

    public void getSongItem(String songId, GetSongItem listener) {
        executor.execute(() -> {
            //First try to fetch song objects from local db
            Song localSong = AppLocalDb.db.songDao().getOneById(songId);
            if (localSong != null) {
                Mixtape localMixtape = AppLocalDb.db.mixtapeDao().getOneById(localSong.getMixtapeId());
                User localUser = AppLocalDb.db.userDao().getOneById(localSong.getUserId());

                if (localMixtape != null && localUser != null) {
                    SongItem songItem = new SongItem(localSong, localMixtape, localUser);
                    listener.onComplete(songItem);
                    return;
                }
            }

            //CHECKME:
            //Fetch song objects from firebase if not found in local db
            SongItem songItem = new SongItem();
            modelFirebase.getSongById(songId, dbSong -> {
                songItem.setSong(dbSong);
                modelFirebase.getMixtapeById(dbSong.getMixtapeId(), dbMixtape -> {
                    songItem.setMixtape(dbMixtape);
                    modelFirebase.getUserById(dbSong.getUserId(), dbUser -> {
                        songItem.setUser(dbUser);
                        listener.onComplete(songItem);
                    });
                });
            });
        });
    }

    public void getSong(String songId, GetSong listener) {
        modelFirebase.getSongById(songId, listener);
    }

    public void addSong(Song song, AddSong listener) {
        modelFirebase.addSong(song, songId -> {
            //Save song to local db
            AppLocalDb.db.songDao().insertMultiple(song);
            //Return to listener
            listener.onComplete(songId);
            //Refresh live data
            refreshFeed();
            refreshProfile();
        });
    }

    public void getMixtape(String mixtapeId, GetMixtape listener) {
        modelFirebase.getMixtapeById(mixtapeId, listener);
    }

    public void addMixtape(Mixtape mixtape, AddMixtape listener) {
        modelFirebase.addMixtape(mixtape, mixtapeId -> {
            listener.onComplete(mixtapeId);
            refreshFeed();
            refreshProfile();
        });
    }

    public void getUser(String userId, GetUser listener) {
        modelFirebase.getUserById(userId, listener);
    }

    public void getMixtapesOfUser(String userId, GetMixtapesOfUser listener) {
        //TODO: save to local db
        modelFirebase.getMixtapesOfUser(userId, listener);
    }

    //_________________________ Data Refresh Functions _________________________

    //functions for local data refresh on the device
    public void refreshFeed() {
        feedLoadingState.setValue(FeedState.loading);

        //Get last local update date from the device
        Long lastUpdateDate = MyApplication.getContext().getSharedPreferences("FEED", Context.MODE_PRIVATE).getLong("FeedLastUpdateDate", 0);

        //Firebase get all updates since lastLocalUpdateDate
        modelFirebase.getFeedSongs(lastUpdateDate, songs -> {
            Log.d("TAG", "Model - firebase returned " + songs.size() + " songs to feed");

            executor.execute(() -> {
                //Save feed songs to local db
                AppLocalDb.db.songDao().insertMany(songs);

                //CHECKME: check last update date is working
                //Find and Save the latest update time to device's share preferences
                long lastLocalUpdate = songs.stream().mapToLong(Song::getTimeCreated).max().getAsLong();
                MyApplication.getContext().getSharedPreferences("FEED", Context.MODE_PRIVATE).edit().putLong("FeedLastUpdateDate", lastLocalUpdate).apply();

                //Get required mixtapes and user ids
                List<String> localMixtapesIds = AppLocalDb.db.songDao().getMixtapesIds();
                List<String> localUsersIds = AppLocalDb.db.songDao().getUsersIds();

                //Get and save feed mixtapes and users
                modelFirebase.getMixtapesByIds(localMixtapesIds, mixtapes -> {
                    Log.d("TAG", "Model - firebase returned " + mixtapes.size() + " mixtapes to feed");

                    executor.execute(() -> {
                        //Save feed mixtapes to local db
                        AppLocalDb.db.mixtapeDao().insertMany(mixtapes);

                        modelFirebase.getUsersByIds(localUsersIds, users -> {
                            Log.d("TAG", "Model - firebase returned " + users.size() + " users to feed");

                            executor.execute(() -> {
                                //Save feed users to local db
                                AppLocalDb.db.userDao().insertMany(users);
                                //Construct feed items from local data and post to caller
                                updateFeedItems();
                                //Post loading state to observer
                                feedLoadingState.postValue(FeedState.loaded);
                            });
                        });
                    });
                });
            });
        });
    }

    private void updateFeedItems() {
        List<SongItem> items = new LinkedList<>();
        List<Song> songs = AppLocalDb.db.songDao().getAll();

        for (Song song : songs) {
            Mixtape mixtape = AppLocalDb.db.mixtapeDao().getOneById(song.getMixtapeId());
            User user = AppLocalDb.db.userDao().getOneById(song.getUserId());
            SongItem songItem = new SongItem(song, mixtape, user);
            items.add(songItem);
        }

        //Post data to caller
        feed.postValue(items);
    }

    public void refreshProfile() {
        profileLoadingState.setValue(ProfileState.loading);

        // get last local update date from the device
        Long lastUpdateDate = MyApplication.getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getLong("ProfileLastUpdateDate", 0);
        String userId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("UserId", "");

        // firebase get all updates since lastLocalUpdateDate //TODO: get profile by userID
        modelFirebase.getProfileMixtapes(lastUpdateDate, userId, (GetMixtapes) list -> {
            Log.d("TAG", "Model - firebase returned " + list.size() + " mixtapes to profile");

            // add all records to the local db
            executor.execute(() -> {
                Long lastLocalUpdate = new Long(0);

                for (Mixtape mixtape : list) {
                    //add to local db
                    AppLocalDb.db.mixtapeDao().insertMultiple(mixtape);
                    //set the latest update time
                    lastLocalUpdate = (lastLocalUpdate < mixtape.getTimeCreated()) ? mixtape.getTimeCreated() : lastLocalUpdate;
                }

                // update last local update date
                MyApplication.getContext()
                        .getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit()
                        .putLong("ProfileLastUpdateDate", lastLocalUpdate)
                        .apply();

                //return all data to caller
                List<Mixtape> newMixtapes = AppLocalDb.db.mixtapeDao().getAll();
                profile.postValue(newMixtapes);
                profileLoadingState.postValue(ProfileState.loaded);
            });
        });
    }

    /*____________________________________________________________________________________________*/
    /*________________________________________ LISTENERS _________________________________________*/
    /*____________________________________________________________________________________________*/

    //_________________________ Listener Interfaces _________________________
    //interface for each remote data fetching\pushing action wrapping its events

    public interface GetSongs {
        void onComplete(List<Song> songs);
    }

    public interface GetMixtapes {
        void onComplete(List<Mixtape> mixtapes);
    }

    public interface GetUsers {
        void onComplete(List<User> users);
    }

    public interface GetSong {
        void onComplete(Song song);
    }

    public interface AddSong {
        void onComplete(String songId);
    }

    public interface GetMixtape {
        void onComplete(Mixtape mixtape);
    }

    public interface AddMixtape {
        void onComplete(String mixtapeId);
    }

    public interface UserProcess {
        void onComplete(User user);
    }

    public interface GetUser {
        void onComplete(User user);
    }

    public interface SaveImageListener {
        void onComplete(String url);
    }

    public interface GetMixtapesOfUser {
        void onComplete(Map<String, String> mixtapeIdToName);
    }

    public interface GetSongItem {
        void onComplete(SongItem songItem);
    }
}
