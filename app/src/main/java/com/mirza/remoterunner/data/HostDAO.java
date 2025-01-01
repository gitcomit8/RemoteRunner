package com.mirza.remoterunner.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HostDAO {
    @Query("SELECT * FROM hosts")
    List<Host> getAll();

    @Insert
    void insertAll(Host... hosts);
}