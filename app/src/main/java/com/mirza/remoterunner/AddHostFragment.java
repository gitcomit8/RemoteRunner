package com.mirza.remoterunner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mirza.remoterunner.data.AppDatabase;
import com.mirza.remoterunner.data.Host;
import com.mirza.remoterunner.data.HostDAO;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddHostFragment extends Fragment {

    private EditText hostnameInput, usernameInput, passwordInput;
    private Button saveButton;
    private HostDAO hostDao;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_host, container, false);

        hostnameInput = view.findViewById(R.id.hostname_input);
        usernameInput = view.findViewById(R.id.username_input);
        passwordInput = view.findViewById(R.id.password_input);
        saveButton = view.findViewById(R.id.save_button);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        hostDao = db.hostDAO();

        saveButton.setOnClickListener(v -> saveHost());

        return view;
    }

    private void saveHost() {
        String hostname = hostnameInput.getText().toString();
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (hostname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String encryptedPassword = EncryptionManager.encrypt(requireContext(), password);
            Host newHost = new Host(hostname, username, encryptedPassword);
            executor.execute(() -> hostDao.insertAll(newHost));
            Toast.makeText(requireContext(), "Host saved", Toast.LENGTH_SHORT).show();
            hostnameInput.setText("");
            usernameInput.setText("");
            passwordInput.setText("");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving host", Toast.LENGTH_SHORT).show();
        }
    }
}