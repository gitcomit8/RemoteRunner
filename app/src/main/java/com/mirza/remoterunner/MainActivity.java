package com.mirza.remoterunner;

import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.mirza.remoterunner.data.AppDatabase;
import com.mirza.remoterunner.data.RemoteRunnerDAO;
import com.mirza.remoterunner.data.SSHCommands;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private AppDatabase db;
    private DrawerLayout drawer;
    private RemoteRunnerDAO remoteRunnerDAO; // Use the correct DAO
    private LinearLayout buttonContainer;
    private TextView outputText;
    private EditText hostnameInput, portInput, usernameInput, passwordInput, commandInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        buttonContainer = findViewById(R.id.button_container);
        outputText = findViewById(R.id.output_text);
        hostnameInput = findViewById(R.id.hostname_input);
        portInput = findViewById(R.id.port_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        commandInput = findViewById(R.id.command_input);

        db = AppDatabase.getDatabase(getApplicationContext());
        remoteRunnerDAO = db.remoteRunnerDAO(); // Initialize the correct DAO

        executor.execute(() -> {
            List<SSHCommands> commands = remoteRunnerDAO.getAll();
            runOnUiThread(() -> {
                for (SSHCommands command : commands) {
                    try {
                        String decryptedPassword = EncryptionManager.decrypt(getApplicationContext(), command.encryptedPassword);
                        if (decryptedPassword != null) { // Check if decrypted password is not null
                            addButtonFromDB(command.commandName, command.hostname, command.port, command.username, decryptedPassword, command.command);
                        } else {
                            Log.e("Decryption Error", "Decryption failed for command: " + command.commandName);
                            executor.execute(() -> remoteRunnerDAO.delete(command));
                        }
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                        Log.e("Decryption Error", "Exception during decryption for command: " + command.commandName, e);
                        executor.execute(() -> remoteRunnerDAO.delete(command));
                    }
                }
            });
        });

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> addButton());
    }

    private void addButton() {
        String commandName = commandInput.getText().toString();
        String hostname = hostnameInput.getText().toString();
        int port = Integer.parseInt(portInput.getText().toString().isEmpty() ? "22" : portInput.getText().toString());
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String command = commandInput.getText().toString();

        if (hostname.isEmpty() || username.isEmpty() || password.isEmpty() || command.isEmpty() || commandName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String encryptedPassword = EncryptionManager.encrypt(getApplicationContext(), password);
            SSHCommands newCommand = new SSHCommands(commandName, hostname, port, username, encryptedPassword, command);
            executor.execute(() -> remoteRunnerDAO.insertAll(newCommand));
            addButtonFromDB(commandName, hostname, port, username, password, command);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addButtonFromDB(String commandName, String hostname, int port, String username, String password, String command) {
        Button newButton = new Button(this);
        newButton.setText(commandName);
        newButton.setOnClickListener(v -> CompletableFuture.supplyAsync(() -> {
            try {
                return RemoteRunner.executeCommand(hostname, port, username, password, command);
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }, executor).thenAccept(result -> runOnUiThread(() -> outputText.setText(result))));
        buttonContainer.addView(newButton);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}