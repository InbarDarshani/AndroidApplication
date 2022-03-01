package com.example.mixtape.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mixtape.MyApplication;

//Working with the local database in the device

@Database(entities = {Song.class, Mixtape.class, User.class}, version = 3, exportSchema = false)
abstract class AppLocalDbRepository extends RoomDatabase {

    public abstract SongDao songDao();

    public abstract MixtapeDao mixtapeDao();

    public abstract UserDao userDao();

}

public class AppLocalDb {
    static public AppLocalDbRepository db =
            Room.databaseBuilder(MyApplication.getContext(),
                    AppLocalDbRepository.class,
                    "mixtapeDB.db")
                    .fallbackToDestructiveMigration()
                    .build();
}

