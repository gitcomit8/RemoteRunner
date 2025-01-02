package com.mirza.remoterunner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button showButton = view.findViewById(R.id.show_button);
        Button editButton = view.findViewById(R.id.edit_button);
        Button hostsButton = view.findViewById(R.id.hosts_button);
        Button deleteButton = view.findViewById(R.id.delete_button);

        MainActivity mainActivity = (MainActivity) getActivity();

        showButton.setOnClickListener(v -> {
            assert mainActivity != null;
            mainActivity.displayFragment(new ListCommandsFragment(), "Show Commands");
        });
        editButton.setOnClickListener(v -> {
            assert mainActivity != null;
            mainActivity.displayFragment(new AddCommandFragment(), "Add Command");
        });
        hostsButton.setOnClickListener(v -> {
            assert mainActivity != null;
            mainActivity.displayFragment(new AddHostFragment(), "Add Host");
        });
        deleteButton.setOnClickListener(v -> {
            assert mainActivity != null;
            mainActivity.displayFragment(new DeleteFragment(), "Delete Entries");
        });

        return view;
    }
}