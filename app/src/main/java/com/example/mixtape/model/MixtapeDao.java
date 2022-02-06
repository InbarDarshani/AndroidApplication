package com.example.mixtape.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MixtapeDao {

    @Query("select * from Mixtape")
    List<Mixtape> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMany(Mixtape... mixtapes);

    @Delete
    void delete(Mixtape mixtape);


}
