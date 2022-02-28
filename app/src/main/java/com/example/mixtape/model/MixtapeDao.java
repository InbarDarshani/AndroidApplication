package com.example.mixtape.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Map;

@Dao
public interface MixtapeDao {

    @Query("SELECT * FROM Mixtape")
    List<Mixtape> getAll();

    @Query("SELECT * FROM Mixtape WHERE mixtapeId = :mixtapeId")
    Mixtape getOneById(String mixtapeId);

    @Query("SELECT * FROM Mixtape WHERE mixtapeId IN(:mixtapeIds)")
    List<Mixtape> getManyByIds(List<String> mixtapeIds);

    @Query("SELECT * FROM Mixtape WHERE  userId = :userId")
    List<Mixtape> getManyByUserId(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMultiple(Mixtape... mixtapes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMany(List<Mixtape> mixtapes);

    @Delete
    void delete(Mixtape mixtape);

}
