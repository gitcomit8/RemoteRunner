package com.mirza.remoterunner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mirza.remoterunner.data.AppDatabase;
import com.mirza.remoterunner.data.RemoteRunnerDAO;
import com.mirza.remoterunner.data.SSHCommands;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListCommandsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommandAdapter adapter;
    private RemoteRunnerDAO remoteRunnerDAO;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private TextView outputText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_commands, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        outputText = view.findViewById(R.id.output_text);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        remoteRunnerDAO = db.remoteRunnerDAO();

        executor.execute(() -> {
            List<SSHCommands> commands = remoteRunnerDAO.getAll();
            try {
                for (SSHCommands command : commands) {
                    command.encryptedPassword = EncryptionManager.decrypt(requireContext(), command.encryptedPassword);
                }
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
            requireActivity().runOnUiThread(() -> {
                adapter = new CommandAdapter(commands);
                recyclerView.setAdapter(adapter);
            });
        });

        return view;
    }

    private class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {
        private final List<SSHCommands> commands;

        public CommandAdapter(List<SSHCommands> commands) {
            this.commands = commands;
        }

        @NonNull
        @Override
        public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.command_item, parent, false);
            return new CommandViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
            SSHCommands command = commands.get(position);
            holder.button.setText(command.commandName);

            holder.button.setOnClickListener(v -> CompletableFuture.supplyAsync(() -> {
                try {
                    return RemoteRunner.executeCommand(command.hostname, command.port, command.username, command.encryptedPassword, command.command);
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }, executor).thenAccept(result -> requireActivity().runOnUiThread(() -> outputText.setText(result))));
        }

        @Override
        public int getItemCount() {
            return commands.size();
        }

        public class CommandViewHolder extends RecyclerView.ViewHolder {
            Button button; // Use a Button

            public CommandViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.command_button); // Find the Button in command_item.xml
            }
        }
    }
}