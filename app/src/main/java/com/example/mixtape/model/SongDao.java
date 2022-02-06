package com.example.mixtape.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {

    @Query("select * from Song")
    List<Song> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMany(Song... songs);

    @Delete
    void delete(Song song);


}
