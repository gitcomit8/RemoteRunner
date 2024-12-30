package com.mirza.remoterunner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RemoteRunnerDAO{
    @Query("SELECT * FROM DRemoteRunner")
    List<DRemoteRunner> getAll();

    @Insert
    void insertAll(DRemoteRunner... commands);

    @Delete
    void delete(DRemoteRunner command);

    @Query("DELETE FROM DRemoteRunner")
    void deleteAll();
}
