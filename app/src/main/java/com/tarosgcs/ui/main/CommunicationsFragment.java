package com.tarosgcs.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.tarosgcs.LoRaTransceiver;
import com.tarosgcs.MessageHandler;
import com.tarosgcs.R;
import com.tarosgcs.databinding.CommunicationsFragmentBinding;
import com.tarosgcs.databinding.CommunicationListviewBinding;

/**
 * A fragment containing a view for the serial communication.
 */
public class CommunicationsFragment extends Fragment {

    private CommunicationsViewModel viewModel;
    private CommunicationsFragmentBinding binding;
    private CommunicationListviewBinding listviewBinding;
    private LoRaTransceiver modem;
    private MessageHandler messageReceiver;

    // Array of strings...
    ListView simpleList;
    String countryList[] = {"India", "China", "australia", "Portugle", "America", "NewZealand"};

    public static CommunicationsFragment newInstance(LoRaTransceiver tr, MessageHandler handler) {
        CommunicationsFragment fragment = new CommunicationsFragment();
        Bundle bundle = new Bundle();
        // bundle.putString(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        fragment.modem = tr;
        fragment.messageReceiver = handler;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunicationsViewModel.class);
        messageReceiver.setViewModel(viewModel);
        viewModel.setInfo(modem.getInfo());
        viewModel.setMessage(messageReceiver.getLastMessage());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = CommunicationsFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        listviewBinding = CommunicationListviewBinding.inflate(inflater, container, false);

        final TextView messageView = binding.receivedMessages;
        viewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                messageView.setText(s);
            }
        });

        final ListView simpleList = binding.simpleListView;
        final TextView listTextView = listviewBinding.listTextView;

        // The second parameter is resource id used to set the layout(xml file)
        //      for list items in which you have a text view.
        // The third parameter is textViewResourceId which is used
        //      to set the id of TextView where you want to display the actual text.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.communication_listview, R.id.listTextView, countryList);
        simpleList.setAdapter(arrayAdapter);

        final TextView textView = binding.deviceInfo;
        viewModel.getInfo().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final Button buttonRefresh = binding.buttonRefresh;
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modem.isConnected()) modem.disconnect();
                modem.refresh();
                viewModel.setInfo(modem.getInfo());
            }
        });

        final Button buttonConnect = binding.buttonConnect;
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modem.isAvailable()) {
                    if (modem.isConnected()) {
                        modem.disconnect();
                        modem.refresh();
                    }
                }
                if (modem.isAvailable()) {
                    modem.connect();
                }
                viewModel.setInfo(modem.getInfo());
            }
        });

        final Button buttonDisconnect = binding.buttonDisconnect;
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modem.isConnected()) modem.disconnect();
                viewModel.setInfo(modem.getInfo());
            }
        });

        final Button buttonStart = binding.buttonStart;
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modem.start_dummy();
            }
        });

        final Button buttonStop = binding.buttonStop;
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modem.stop_dummy();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
