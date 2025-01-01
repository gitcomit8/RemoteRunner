package com.mirza.remoterunner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListCommandsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommandAdapter adapter;
    private RemoteRunnerDAO remoteRunnerDAO;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_commands, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        remoteRunnerDAO = db.remoteRunnerDAO();

        executor.execute(() -> {
            List<SSHCommands> commands = remoteRunnerDAO.getAll();
            try {
                for(SSHCommands command : commands){
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
        private List<SSHCommands> commands;

        public CommandAdapter(List<SSHCommands> commands) {
            this.commands = commands;
        }

        @NonNull
        @Override
        public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new CommandViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
            SSHCommands command = commands.get(position);
            holder.textView.setText(command.commandName + ": " + command.command + " - " + command.hostname);
        }

        @Override
        public int getItemCount() {
            return commands.size();
        }

        public class CommandViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public CommandViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}