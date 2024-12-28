package com.mirza.remoterunner;

import android.os.Bundle;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private DrawerLayout drawer;
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

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> addButton());
    }

    private void addButton() {
        String hostname = hostnameInput.getText().toString();
        int port = Integer.parseInt(portInput.getText().toString().isEmpty() ? "22" : portInput.getText().toString());
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String command = commandInput.getText().toString();

        if (hostname.isEmpty() || username.isEmpty() || password.isEmpty() || command.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Button newButton = new Button(this);
        newButton.setText(command);
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

        //TODO
        // Handle navigation item clicks here, like switching fragments
        // (omitted for brevity)

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}