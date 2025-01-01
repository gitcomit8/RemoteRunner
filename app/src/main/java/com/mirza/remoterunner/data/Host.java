package com.mirza.remoterunner.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hosts")
public class Host {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "hostname")
    public String hostname;

    @NonNull
    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "encrypted_password")
    public String encryptedPassword;

    public Host(@NonNull String hostname, @NonNull String username, String encryptedPassword) {
        this.hostname = hostname;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
    }
}