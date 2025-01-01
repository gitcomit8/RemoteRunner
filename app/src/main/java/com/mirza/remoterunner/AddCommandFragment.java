package com.mirza.remoterunner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mirza.remoterunner.data.AppDatabase;
import com.mirza.remoterunner.data.Host;
import com.mirza.remoterunner.data.HostDAO;
import com.mirza.remoterunner.data.RemoteRunnerDAO; // Assuming this is the DAO for SSHCommands
import com.mirza.remoterunner.data.SSHCommands;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCommandFragment extends Fragment {

    private EditText commandNameInput, commandInput;
    private Spinner hostSpinner;
    private Button saveButton;
    private RemoteRunnerDAO remoteRunnerDAO;
    private HostDAO hostDao;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private List<Host> hostList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_command, container, false);

        commandNameInput = view.findViewById(R.id.command_name_input);
        commandInput = view.findViewById(R.id.command_input);
        hostSpinner = view.findViewById(R.id.host_spinner);
        saveButton = view.findViewById(R.id.save_button);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        remoteRunnerDAO = db.remoteRunnerDAO();
        hostDao = db.hostDAO();
        hostList = new ArrayList<>();

        executor.execute(() -> {
            hostList = hostDao.getAll();
            List<String> hostNames = new ArrayList<>();
            for (Host host : hostList) {
                hostNames.add(host.hostname + " (" + host.username + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, hostNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            requireActivity().runOnUiThread(() -> hostSpinner.setAdapter(adapter));
        });

        saveButton.setOnClickListener(v -> saveCommand());

        return view;
    }

    private void saveCommand() {
        String commandName = commandNameInput.getText().toString();
        String command = commandInput.getText().toString();
        int selectedHostIndex = hostSpinner.getSelectedItemPosition();

        if (commandName.isEmpty() || command.isEmpty() || selectedHostIndex == -1) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Host selectedHost = hostList.get(selectedHostIndex);

        executor.execute(() -> {
            List<SSHCommands> existingCommands = remoteRunnerDAO.getAll();
            for (SSHCommands existingCommand : existingCommands) {
                if (existingCommand.hostname.equals(selectedHost.hostname) && existingCommand.command.equals(command)) {
                    remoteRunnerDAO.delete(existingCommand);
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show());
                    return;
                }
            }
            try {
                String encryptedPassword = EncryptionManager.encrypt(requireContext(), selectedHost.encryptedPassword);
                SSHCommands newCommand = new SSHCommands(commandName, selectedHost.hostname, 22, selectedHost.username, encryptedPassword, command);
                remoteRunnerDAO.insertAll(newCommand);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Command Saved Successfully", Toast.LENGTH_SHORT).show());
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error saving command", Toast.LENGTH_SHORT).show());
            }
        });
    }
}