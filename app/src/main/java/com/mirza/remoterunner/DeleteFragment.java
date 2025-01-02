package com.mirza.remoterunner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mirza.remoterunner.data.AppDatabase;
import com.mirza.remoterunner.data.Host;
import com.mirza.remoterunner.data.HostDAO;
import com.mirza.remoterunner.data.RemoteRunnerDAO;
import com.mirza.remoterunner.data.SSHCommands;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteFragment extends Fragment {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private LinearLayout hostsContainer, commandsContainer;
    private HostDAO hostDao;
    private RemoteRunnerDAO remoteRunnerDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete, container, false);

        hostsContainer = view.findViewById(R.id.hosts_container);
        commandsContainer = view.findViewById(R.id.commands_container);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        hostDao = db.hostDAO();
        remoteRunnerDAO = db.remoteRunnerDAO();

        loadData();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        executor.execute(() -> {
            List<Host> hosts = hostDao.getAll();
            List<SSHCommands> commands = remoteRunnerDAO.getAll();

            requireActivity().runOnUiThread(() -> {
                hostsContainer.removeAllViews();
                commandsContainer.removeAllViews();

                for (Host host : hosts) {
                    Button hostButton = new Button(requireContext());
                    hostButton.setText(host.hostname + " (" + host.username + ")");
                    hostButton.setOnClickListener(v -> executor.execute(() -> {
                        hostDao.deleteHost(host);
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Host Deleted", Toast.LENGTH_SHORT).show());
                        loadData();
                    }));
                    hostsContainer.addView(hostButton);
                }

                for (SSHCommands command : commands) {
                    Button commandButton = new Button(requireContext());
                    commandButton.setText(command.commandName + ": " + command.command + " (" + command.hostname + ")");
                    commandButton.setOnClickListener(v -> executor.execute(() -> {
                        remoteRunnerDAO.delete(command);
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Command Deleted", Toast.LENGTH_SHORT).show());
                        loadData();
                    }));
                    commandsContainer.addView(commandButton);
                }
            });
        });
    }
}