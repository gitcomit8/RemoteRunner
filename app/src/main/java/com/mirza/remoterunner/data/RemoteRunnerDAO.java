package com.mirza.remoterunner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RemoteRunnerDAO {
    @Query("SELECT * FROM SSH_COMMANDS")
    List<SSHCommands> getAll();

    @Insert
    void insertAll(SSHCommands... commands);

    @Delete
    void delete(SSHCommands command);

    @Query("DELETE FROM SSH_COMMANDS")
    void deleteAll();
}
