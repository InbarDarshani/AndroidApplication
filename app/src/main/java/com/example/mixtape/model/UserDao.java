package com.example.mixtape.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Map;

@Dao
public interface UserDao {

    @Query("SELECT * FROM Song")
    List<User> getAll();

    @Query("SELECT * FROM User WHERE userId = :userId")
    User getOneById(String userId);

    @Query("SELECT * FROM User WHERE userId IN(:userIds)")
    List<User> getManyByIds(List<String> userIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMultiple(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMany(List<User> users);

    @Delete
    void delete(User user);
}
