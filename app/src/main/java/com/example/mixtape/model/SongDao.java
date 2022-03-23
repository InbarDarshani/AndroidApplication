package com.example.mixtape.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {

    @Query("SELECT * FROM Song")
    List<Song> getAll();

    @Query("SELECT * FROM Song WHERE userId = :userId")
    List<Song> getAllByUserId(String userId);

    @Query("SELECT * FROM Song WHERE mixtapeId = :mixtapeId")
    List<Song> getAllByMixtapeId(String mixtapeId);

    @Query("SELECT songId FROM Song")
    List<String> getAllIds();

    @Query("SELECT DISTINCT mixtapeId FROM Song")
    List<String> getMixtapesIds();

    @Query("SELECT DISTINCT userId FROM Song")
    List<String> getUsersIds();

    @Query("SELECT * FROM Song WHERE songId = :songId")
    Song getOneById(String songId);

    @Query("SELECT * FROM Song WHERE  userId = :userId")
    List<Song> getManyByUserId(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMultiple(Song... songs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMany(List<Song> songs);

    @Delete
    void delete(Song song);

}
