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
import java.util.stream.Collectors;

//All data related actions

public class Model {
    public static final Model instance = new Model();
    private final ModelFirebase modelFirebase = new ModelFirebase();
    public ExecutorService executor = Executors.newFixedThreadPool(1);
    public Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
    public String dbError = "";

    private Model() {
        //Set data loading states
        feedLoadingState.setValue(FeedState.loaded);
        profileLoadingState.setValue(ProfileState.loaded);
        userLoginState.setValue(LoginState.none);
    }

    /*______________________________________ AUTHENTICATION ______________________________________*/

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

    //Save user data to local shared preferences
    private void saveUser(User user) {
        executor.execute(() -> {
            AppLocalDb.db.userDao().insertMultiple(user);

            MyApplication.getContext()
                    .getSharedPreferences("USER", Context.MODE_PRIVATE)
                    .edit()
                    .putString("userId", user.getUserId())
                    .putString("email", user.getEmail())
                    .putString("displayName", user.getDisplayName())
                    .putString("image", user.getImage())
                    .commit();
        });
    }

    //Clear user data from local shared preferences
    private void clearUser() {
        executor.execute(() -> MyApplication.getContext().deleteSharedPreferences("USER"));
    }

    //Synchronous function creating and returning current user as object
    public User getCurrentUser() {
        Map<String, Object> json = (Map<String, Object>) MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getAll();
        return User.create(json);
    }

    /*___________________________________________ DATA ___________________________________________*/

    //_________________________ Data Holders _________________________
    MutableLiveData<List<SongItem>> feed = new MutableLiveData<>();

    //_________________________ Data Loading States _________________________
    //properties for representing the loading state of each LiveData
    public enum FeedState {
        loading,
        empty,
        loaded
    }

    public enum ProfileState {
        loading,
        empty,
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

    //_________ New Objects Fetching _________
    public LiveData<List<SongItem>> getFeed() {
        if (feed.getValue() == null) {
            refreshFeed();
        }
        return feed;
    }

    public MutableLiveData<List<MixtapeItem>> getUserProfile(String userId) {
        MutableLiveData<List<MixtapeItem>> userMixtapeItems = new MutableLiveData<>();

        //First Get and post existing data from local db
        executor.execute(() -> userMixtapeItems.postValue(constructProfileItems(userId)));

        //Start loading
        profileLoadingState.postValue(ProfileState.loading);
        //Fetch all user's mixtapes from firebase db
        modelFirebase.getProfileMixtapes(0L, userId, mixtapes -> {
            Log.d("TAG", "Model - firebase returned " + mixtapes.size() + " mixtapes to profile");

            //If there are no mixtapes finish loading now
            if (mixtapes.isEmpty())
                profileLoadingState.postValue(ProfileState.loaded);

            executor.execute(() -> {
                //Save mixtapes to local db
                AppLocalDb.db.mixtapeDao().insertMany(mixtapes);
                //Construct profile mixtape items objects from local db and post to live data
                userMixtapeItems.postValue(constructProfileItems(userId));

                if (mixtapes.isEmpty()) {
                    profileLoadingState.postValue(ProfileState.empty);
                } else
                    profileLoadingState.postValue(ProfileState.loaded);
            });
        });
        return userMixtapeItems;
    }

    public void getUserProfile(String userId, GetMixtapeItems listener) {
        //Start loading
        profileLoadingState.postValue(ProfileState.loading);
        //Get all user's mixtapes from local db
        executor.execute(() -> {
            List<MixtapeItem> localMixtepItems = constructProfileItems(userId);

            if (localMixtepItems.isEmpty()) {
                profileLoadingState.postValue(ProfileState.empty);
            } else
                profileLoadingState.postValue(ProfileState.loaded);

            listener.onComplete(localMixtepItems);
        });

    }

    //_________ Multiple Objects Fetching _________

    public void getMixtapesOfUser(String userId, GetMixtapes listener) {
        modelFirebase.getMixtapesOfUser(userId, listener);
    }

    public void getSongsOfMixtapes(String mixtapeId, GetSongs listener) {
        modelFirebase.getSongsOfMixtape(mixtapeId, listener);
    }

    //_________ Single Object Fetching _________
    public MutableLiveData<SongItem> getSongItem(String songId) {
        MutableLiveData<SongItem> songItemLiveData = new MutableLiveData<>();

        //First try to fetch song objects from local db
        executor.execute(() -> {
            Song localSong = AppLocalDb.db.songDao().getOneById(songId);
            if (localSong != null) {
                Mixtape localMixtape = AppLocalDb.db.mixtapeDao().getOneById(localSong.getMixtapeId());
                User localUser = AppLocalDb.db.userDao().getOneById(localSong.getUserId());

                if (localMixtape != null && localUser != null) {
                    SongItem songItem = new SongItem(localSong, localMixtape, localUser);
                    songItemLiveData.postValue(songItem);
                }
            }
        });

        return songItemLiveData;
    }

    public void getSongItem(String songId, GetSongItem listener) {
        executor.execute(() -> {
            Song localSong = AppLocalDb.db.songDao().getOneById(songId);
            if (localSong != null) {
                Mixtape localMixtape = AppLocalDb.db.mixtapeDao().getOneById(localSong.getMixtapeId());
                User localUser = AppLocalDb.db.userDao().getOneById(localSong.getUserId());

                if (localMixtape != null && localUser != null) {
                    listener.onComplete(new SongItem(localSong, localMixtape, localUser));
                }
            }
        });
    }

    public LiveData<MixtapeItem> getMixtapeItem(String mixtapeId) {
        MutableLiveData<MixtapeItem> mixtapeItemLiveData = new MutableLiveData<>();

        //First try to fetch mixtape objects from local db
        executor.execute(() -> {
            Mixtape localMixtape = AppLocalDb.db.mixtapeDao().getOneById(mixtapeId);
            if (localMixtape != null) {
                List<Song> localSongs = AppLocalDb.db.songDao().getAllByMixtapeId(localMixtape.getMixtapeId());
                User localUser = AppLocalDb.db.userDao().getOneById(localMixtape.getUserId());

                if (localSongs != null && localUser != null) {
                    MixtapeItem mixtapeItem = new MixtapeItem(localMixtape, localSongs, localUser);
                    mixtapeItemLiveData.postValue(mixtapeItem);
                }
            }
        });

        return mixtapeItemLiveData;
    }

    public void getMixtapeItem(String mixtapeId, GetMixtapeItem listener) {
        executor.execute(() -> {
            Mixtape localMixtape = AppLocalDb.db.mixtapeDao().getOneById(mixtapeId);
            if (localMixtape != null) {
                List<Song> localSongs = AppLocalDb.db.songDao().getAllByMixtapeId(localMixtape.getMixtapeId());
                User localUser = AppLocalDb.db.userDao().getOneById(localMixtape.getUserId());

                if (localSongs != null && localUser != null) {
                    listener.onComplete(new MixtapeItem(localMixtape, localSongs, localUser));
                }
            }
        });
    }

    public void getSong(String songId, GetSong listener) {
        modelFirebase.getSongById(songId, listener);
    }

    public void getMixtape(String mixtapeId, GetMixtape listener) {
        modelFirebase.getMixtapeById(mixtapeId, listener);
    }

    public void getUser(String userId, GetUser listener) {
        //Fetch user objects from local db
        executor.execute(() -> {
            User localUser = AppLocalDb.db.userDao().getOneById(userId);
            if (localUser != null) {
                listener.onComplete(localUser);
            }
        });
    }

    //_________ Object Creation _________
    //Add Song with no image and existing mixtape
    public void addSong(Song song, GetSong listener) {
        modelFirebase.addSong(song, dbSong -> {
            saveSong(dbSong, listener);
        });
    }

    //Add Song with no image and new mixtape
    public void addSong(Song song, Mixtape mixtape, GetSong listener) {
        Model.instance.addMixtape(mixtape, dbMixtape -> {
            song.setMixtapeId(dbMixtape.getMixtapeId());

            modelFirebase.addSong(song, dbSong -> {
                saveSong(dbSong, listener);
            });
        });
    }

    //Add Song with image and existing mixtape
    public void addSong(Song song, Bitmap imageBitmap, GetSong listener) {
        modelFirebase.addSong(song, dbSong -> {

            Model.instance.uploadSongImage(imageBitmap, dbSong, dbSongWithImage -> {
                saveSong(dbSongWithImage, listener);
            });
        });
    }

    //Add Song with image and new mixtape
    public void addSong(Song song, Mixtape mixtape, Bitmap imageBitmap, GetSong listener) {
        Model.instance.addMixtape(mixtape, dbMixtape -> {
            song.setMixtapeId(dbMixtape.getMixtapeId());

            modelFirebase.addSong(song, dbSong -> {

                Model.instance.uploadSongImage(imageBitmap, dbSong, dbSongWithImage -> {
                    saveSong(dbSongWithImage, listener);
                });
            });
        });
    }

    public void addMixtape(Mixtape mixtape, GetMixtape listener) {
        modelFirebase.addMixtape(mixtape, dbMixtape -> {
            saveMixtape(dbMixtape, listener);
        });
    }

    //_________ Object Updating _________
    //Update Song
    public void updateSong(Song song, GetSong listener) {
        modelFirebase.updateSong(song, () -> {
            saveSong(song, listener);
        });
    }

    //Update Song with new mixtape
    public void updateSong(Song song, Mixtape mixtape, GetSong listener) {
        Model.instance.addMixtape(mixtape, dbMixtape -> {
            song.setMixtapeId(dbMixtape.getMixtapeId());

            modelFirebase.updateSong(song, () -> {
                saveSong(song, listener);
            });
        });
    }

    //Update Song with new image
    public void updateSong(Song song, Bitmap imageBitmap, GetSong listener) {
        Model.instance.uploadSongImage(imageBitmap, song, dbSongWithImage -> {
            saveSong(dbSongWithImage, listener);
        });
    }

    //Update Song with new image and new mixtape
    public void updateSong(Song song, Mixtape mixtape, Bitmap imageBitmap, GetSong listener) {
        Model.instance.addMixtape(mixtape, dbMixtape -> {
            song.setMixtapeId(dbMixtape.getMixtapeId());

            Model.instance.uploadSongImage(imageBitmap, song, dbSongWithImage -> {
                saveSong(dbSongWithImage, listener);
            });
        });
    }

    public void updateMixtape(Mixtape mixtape, GetMixtape listener) {
        modelFirebase.updateMixtape(mixtape, () -> {
            saveMixtape(mixtape, listener);
        });
    }

    public void updateUser(User user, UpdateUser listener) {
        modelFirebase.updateUser(user, () -> {
            //Save user to local db
            executor.execute(() -> AppLocalDb.db.userDao().insertMultiple(user));
            //Return to listener
            listener.onComplete();
            //Refresh live data
            refreshFeed();
        });
    }

    //_________ Object Deleting _________
    public void deleteSong(Song song, UpdateSong listener) {
        song.setDeleted(true);
        modelFirebase.updateSong(song, () -> {
            executor.execute(() -> AppLocalDb.db.songDao().delete(song));
            //Refresh live data
            refreshFeed();
            //Return to listener
            listener.onComplete();
        });
    }

    public void deleteMixtape(Mixtape mixtape, UpdateMixtape listener) {
        mixtape.setDeleted(true);
        modelFirebase.updateMixtape(mixtape, () -> {
            executor.execute(() -> AppLocalDb.db.mixtapeDao().delete(mixtape));
            //Refresh live data
            refreshFeed();
            //Return to listener
            listener.onComplete();
        });
    }

    //_________________________ Other Functions _________________________
    public void refreshFeed() {
        //Start loading
        feedLoadingState.setValue(FeedState.loading);

        //First Get and post existing data from local db
        executor.execute(() -> feed.postValue(constructFeedItems()));

        //Get last local update date from the device
        Long localLastUpdate = MyApplication.getContext().getSharedPreferences("FEED", Context.MODE_PRIVATE).getLong("FeedLastUpdateDate", 0);

        //Add a task to executor to remove recently deleted song posts from local db
        modelFirebase.getFeedDeletedSongs(localLastUpdate, deletedSongs -> {
            Log.d("TAG", "Model - firebase returned " + deletedSongs.size() + " deleted songs to feed");
            executor.execute(() -> deletedSongs.forEach(s -> AppLocalDb.db.songDao().delete(s)));
        });

        //Firebase get all new songs since lastLocalUpdateDate
        modelFirebase.getFeedSongs(localLastUpdate, newSongs -> {
            Log.d("TAG", "Model - firebase returned " + newSongs.size() + " new songs to feed");

            if (newSongs.isEmpty()) {
                //Post loading state to observer
                feedLoadingState.postValue(FeedState.loaded);
                return;
            }

            executor.execute(() -> {
                //Save new feed songs to local db
                AppLocalDb.db.songDao().insertMany(newSongs);

                //Find and Save the latest update time to device's share preferences
                long lastLocalUpdate = newSongs.stream().mapToLong(Song::getTimeCreated).max().orElse(0);
                MyApplication.getContext().getSharedPreferences("FEED", Context.MODE_PRIVATE).edit().putLong("FeedLastUpdateDate", lastLocalUpdate).apply();

                //Get required mixtapes and user ids
                List<String> newMixtapeIds = newSongs.stream().map(Song::getMixtapeId).collect(Collectors.toList());
                List<String> newUsersIds = newSongs.stream().map(Song::getUserId).collect(Collectors.toList());

                //Get and save feed mixtapes and users
                modelFirebase.getMixtapesByIds(newMixtapeIds, mixtapes -> {
                    Log.d("TAG", "Model - firebase returned " + mixtapes.size() + " mixtapes to feed");

                    executor.execute(() -> {
                        //Save feed mixtapes to local db
                        AppLocalDb.db.mixtapeDao().insertMany(mixtapes);

                        modelFirebase.getUsersByIds(newUsersIds, users -> {
                            Log.d("TAG", "Model - firebase returned " + users.size() + " users to feed");

                            executor.execute(() -> {
                                //Save feed users to local db
                                AppLocalDb.db.userDao().insertMany(users);
                                //Construct feed items from local data and post to caller
                                feed.postValue(constructFeedItems());
                                //Post loading state to observer
                                feedLoadingState.postValue(FeedState.loaded);
                            });
                        });
                    });
                });
            });
        });
    }

    //Construct feed song items objects from local db
    private List<SongItem> constructFeedItems() {
        List<SongItem> items = new LinkedList<>();
        List<Song> songs = AppLocalDb.db.songDao().getAll();

        for (Song song : songs) {
            if (!song.isDeleted()) {
                Mixtape mixtape = AppLocalDb.db.mixtapeDao().getOneById(song.getMixtapeId());
                User user = AppLocalDb.db.userDao().getOneById(song.getUserId());
                SongItem songItem = new SongItem(song, mixtape, user);
                items.add(songItem);
            }
        }

        //Sort by TimeModified
        items.sort((si1, si2) -> si2.getSong().getTimeModified().compareTo(si1.getSong().getTimeModified()));
        return items;
    }

    //Construct profile mixtape items objects from local db
    private List<MixtapeItem> constructProfileItems(String userId) {
        List<MixtapeItem> items = new LinkedList<>();
        List<Mixtape> mixtapes = AppLocalDb.db.mixtapeDao().getManyByUserId(userId);
        User user = AppLocalDb.db.userDao().getOneById(userId);

        for (Mixtape mixtape : mixtapes) {
            if (!mixtape.isDeleted()) {
                List<Song> songs = AppLocalDb.db.songDao().getAllByMixtapeId(mixtape.getMixtapeId());
                MixtapeItem mixtapeItem = new MixtapeItem(mixtape, songs, user);
                items.add(mixtapeItem);
            }
        }

        //Sort by TimeModified
        items.sort((mi1, mi2) -> mi2.getMixtape().getTimeModified().compareTo(mi1.getMixtape().getTimeModified()));
        return items;
    }

    private void saveSong(Song song, GetSong listener) {
        //Save song to local db
        executor.execute(() -> AppLocalDb.db.songDao().insertMultiple(song));
        //Refresh live data
        refreshFeed();
        //Return to listener
        listener.onComplete(song);
    }

    private void saveMixtape(Mixtape mixtape, GetMixtape listener) {
        //Save mixtape to local db
        executor.execute(() -> AppLocalDb.db.mixtapeDao().insertMultiple(mixtape));
        //Refresh live data
        refreshFeed();
        //Return to listener
        listener.onComplete(mixtape);
    }

    private SongItem constructSongItem(String songId, String mixtapeId, String userId) {
        Song song = AppLocalDb.db.songDao().getOneById(songId);
        Mixtape mixtape = AppLocalDb.db.mixtapeDao().getOneById(mixtapeId);
        User user = AppLocalDb.db.userDao().getOneById(userId);
        return new SongItem(song, mixtape, user);
    }

    private MixtapeItem constructMixtapeItem(String mixtapeId, String userId) {
        Mixtape mixtape = AppLocalDb.db.mixtapeDao().getOneById(mixtapeId);
        List<Song> songs = AppLocalDb.db.songDao().getAllByMixtapeId(mixtape.getMixtapeId());
        User user = AppLocalDb.db.userDao().getOneById(userId);
        return new MixtapeItem(mixtape, songs, user);
    }

    /*__________________________________________ STORAGE _________________________________________*/

    //_________________________ Storage Functions _________________________
    public void uploadImage(Bitmap imageBitmap, String folder, String imageName, SaveImageListener listener) {
        modelFirebase.saveImage(imageBitmap, folder, imageName, listener);
    }

    public void uploadSongImage(Bitmap imageBitmap, Song song, SaveSongImage listener) {
        Model.instance.uploadImage(imageBitmap, "songs", song.getSongId() + ".jpg", url -> {
            song.setImage(url);
            Model.instance.updateSong(song, s -> listener.onComplete(s));
        });
    }

    public void uploadUserImage(Bitmap imageBitmap, User user, SaveUserImage listener) {
        Model.instance.uploadImage(imageBitmap, "users", user.getUserId() + ".jpg", url -> {
            user.setImage(url);
            Model.instance.updateUser(user, () -> listener.onComplete(user));
        });
    }

    /*________________________________________ LISTENERS _________________________________________*/

//_________________________ Listener Interfaces _________________________
//interface for each remote data fetching\pushing action

    //_________ Multiple Objects _________
    public interface GetSongs {
        void onComplete(List<Song> songs);
    }

    public interface GetMixtapes {
        void onComplete(List<Mixtape> mixtapes);
    }

    public interface GetMixtapeItems {
        void onComplete(List<MixtapeItem> mixtapeItems);
    }

    public interface GetUsers {
        void onComplete(List<User> users);
    }

    //_________ Single Object _________
    public interface GetSong {
        void onComplete(Song song);
    }

    public interface GetSongItem {
        void onComplete(SongItem songItem);
    }

    public interface GetMixtape {
        void onComplete(Mixtape mixtape);
    }

    public interface GetMixtapeItem {
        void onComplete(MixtapeItem mixtapeItem);
    }

    public interface GetUser {
        void onComplete(User user);
    }

    //_________ Object Creation _________
    public interface AddSong {
        void onComplete(Song song);
    }

    public interface AddMixtape {
        void onComplete(Mixtape mixtape);
    }

    //_________ Other_________
    public interface SaveImageListener {
        void onComplete(String url);
    }

    public interface SaveSongImage {
        void onComplete(Song song);
    }

    public interface SaveUserImage {
        void onComplete(User user);
    }

    public interface UserProcess {
        void onComplete(User user);
    }

    //_________ Object Updating _________
    public interface UpdateSong {
        void onComplete();
    }

    public interface UpdateMixtape {
        void onComplete();
    }

    public interface UpdateUser {
        void onComplete();
    }
}
