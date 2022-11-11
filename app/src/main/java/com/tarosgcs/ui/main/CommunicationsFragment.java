package com.tarosgcs.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.tarosgcs.LoRaTransceiver;
import com.tarosgcs.databinding.FragmentMainBinding;

/**
 * A fragment containing a view for the serial communication.
 */
public class CommunicationsFragment extends Fragment {

    private CommunicationsViewModel viewModel;
    private FragmentMainBinding binding;
    private LoRaTransceiver modem;

    public static CommunicationsFragment newInstance(LoRaTransceiver tr) {
        CommunicationsFragment fragment = new CommunicationsFragment();
        Bundle bundle = new Bundle();
        // bundle.putString(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        fragment.modem = tr;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunicationsViewModel.class);
        viewModel.setText(modem.getInfo());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.sectionLabel;
        viewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        // final Button buttonRefresh = binding.

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
