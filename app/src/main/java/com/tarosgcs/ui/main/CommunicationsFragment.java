package com.tarosgcs.ui.main;

import java.util.ArrayList;

import android.os.Bundle;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tarosgcs.LoRaTransceiver;
import com.tarosgcs.MessageHandler;
import com.tarosgcs.R;
import com.tarosgcs.SystemMessage;
import com.tarosgcs.databinding.CommunicationsFragmentBinding;

/**
 * A fragment containing a view for the serial communication.
 */
public class CommunicationsFragment extends Fragment {

    // The ViewModel holds all app data that must stay persistent during run time
    // independent of creation or destruction of UI elements.
    // The UI is implemented in activities and fragments, the data live in the ViewModel.
    // Usually we have a special class with a separate ViewModel for each page.
    private CommunicationsViewModel viewModel;
    private CommunicationsFragmentBinding binding;
    private LoRaTransceiver modem;
    private MessageHandler messageReceiver;
    private RecyclerView mRecyclerView;
    private RVAdapter mRVAdapter;

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
        // associate the ViewModel with the UI controller (the fragment - this)
        viewModel = new ViewModelProvider(this).get(CommunicationsViewModel.class);
        // tell the MessageHandler where to store the message data (in the ViewModel)
        // TODO: that is wrong, the MessageHandler should be part of the ViewModel
        messageReceiver.setViewModel(viewModel);
        // initial update of the ViewModel data content
        viewModel.setInfo(modem.getInfo());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = CommunicationsFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mRecyclerView  = binding.recyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRVAdapter = new RVAdapter();
        mRecyclerView.setAdapter(mRVAdapter);

        viewModel.getMessages().observe(getViewLifecycleOwner(), new Observer<ArrayList<SystemMessage>>() {
            @Override
            public void onChanged(@Nullable ArrayList<SystemMessage> list) {
                mRVAdapter.generateData();
            }
        });

        // The second parameter is resource id used to set the layout(xml file)
        //      for list items in which you have a text view.
        // The third parameter is textViewResourceId which is used
        //      to set the id of TextView where you want to display the actual text.
        // ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.communication_listview, R.id.listTextView, countryList);
        // simpleList.setAdapter(arrayAdapter);

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

    /**
     * Custom adapter that supplies view holders to the RecyclerView. Our view holders
     * contain a simple LinearLayout of TextViews (displaying the messages)
     */
    private class RVAdapter extends RecyclerView.Adapter {

        ArrayList<Integer> mColors;
        ArrayList<String> mSenders;
        ArrayList<String> mTimes;
        ArrayList<String> mStrings;

        public RVAdapter() {
            generateData();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final MyViewHolder myHolder = (MyViewHolder) holder;
            int color = mColors.get(position);
            myHolder.container.setBackgroundColor(color);
            myHolder.senderView.setText(mSenders.get(position));
            myHolder.timeView.setText(mTimes.get(position));
            myHolder.bodyView.setText(mStrings.get(position));
        }

        @Override
        public int getItemCount() {
            return mStrings.size();
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View container = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            return new MyViewHolder(container);
        }

        public void generateData() {
            mColors = new ArrayList<>();
            mSenders = new ArrayList<>();
            mTimes = new ArrayList<>();
            mStrings = new ArrayList<>();
            String levelString;
            ArrayList<SystemMessage> msgList = viewModel.getMessages().getValue();
            for (SystemMessage msg : msgList)
            {
                switch (msg.level) {
                    case 1:     // MSG_LEVEL_FATALERROR
                        levelString = "FATAL";
                        mColors.add(Color.rgb(200, 0, 0));
                        break;
                    case 3:     // MSG_LEVEL_CRITICAL
                        levelString = "CRITICAL";
                        mColors.add(Color.rgb(255, 0, 0));
                        break;
                    case 5:     // MSG_LEVEL_MILESTONE
                        levelString = "MILESTONE";
                        mColors.add(Color.rgb(0, 200, 0));
                        break;
                    case 8:     // MSG_LEVEL_ERROR
                        levelString = "ERROR";
                        mColors.add(Color.rgb(255, 200, 200));
                        break;
                    case 10:    // MSG_LEVEL_STATE_CHANGE
                        levelString = "STATE";
                        mColors.add(Color.rgb(200, 255, 200));
                        break;
                    case 12:    // MSG_LEVEL_WARNING
                        levelString = "WARN";
                        mColors.add(Color.rgb(255, 255, 100));
                        break;
                    default:    // MSG_LEVEL_STATUSREPORT
                        levelString = "REPORT";
                        mColors.add(Color.rgb(210, 210, 210));
                }
                mSenders.add(msg.sender);
                mTimes.add(String.format("%.1f", 0.001*msg.time));
                mStrings.add(levelString + " : " + msg.text);
            }
            notifyDataSetChanged();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView senderView;
        public TextView timeView;
        public TextView bodyView;
        public LinearLayout container;

        public MyViewHolder(View v) {
            super(v);
            container = (LinearLayout) v;
            senderView = (TextView) v.findViewById(R.id.sender);
            timeView = (TextView) v.findViewById(R.id.time);
            bodyView = (TextView) v.findViewById(R.id.body);
        }

        @Override
        public String toString() {
            return super.toString() + " \"" + bodyView.getText() + "\"";
        }
    }

}
