package com.mirza.remoterunner.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ssh_commands")
public class SSHCommands {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    @ColumnInfo(name = "command_name")
    public String commandName;

    @NonNull
    @ColumnInfo
    public String hostname;

    @ColumnInfo
    public int port;

    @NonNull
    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "encrypted_password")
    public String encryptedPassword;

    @NonNull
    @ColumnInfo(name = "command")
    public String command;

    public SSHCommands(String commandName, String hostname, int port, String username, String encryptedPassword, String command) {
        this.commandName = commandName;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.command = command;
    }
}
