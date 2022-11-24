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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tarosgcs.LoRaTransceiver;
import com.tarosgcs.MessageHandler;
import com.tarosgcs.R;
import com.tarosgcs.databinding.CommunicationsFragmentBinding;

/**
 * A fragment containing a view for the serial communication.
 */
public class CommunicationsFragment extends Fragment {

    private CommunicationsViewModel viewModel;
    private CommunicationsFragmentBinding binding;
    private LoRaTransceiver modem;
    private MessageHandler messageReceiver;

    // new for RecyclerView version of the message list
    RecyclerView mRecyclerView;

    /**
     * Custom adapter that supplies view holders to the RecyclerView. Our view holders
     * contain a simple LinearLayout TextViews (displaying the messages)
     */
    private class RVAdapter extends RecyclerView.Adapter {

        ArrayList<Integer> mColors = new ArrayList<>();

        public RVAdapter() {
            generateData();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final MyViewHolder myHolder = (MyViewHolder) holder;
            int color = mColors.get(position);
            myHolder.container.setBackgroundColor(color);
            myHolder.textView.setText("#" + Integer.toHexString(color));
        }

        @Override
        public int getItemCount() {
            return mColors.size();
        }

        private void addItem(View view) {
            int position = mRecyclerView.getChildAdapterPosition(view);
            if (position != RecyclerView.NO_POSITION) {
                int color = generateColor();
                mColors.add(position, color);
                notifyItemInserted(position);
            }
        }

        private void changeItem(View view) {
            int position = mRecyclerView.getChildAdapterPosition(view);
            if (position != RecyclerView.NO_POSITION) {
                int color = generateColor();
                mColors.set(position, color);
                notifyItemChanged(position);
            }
        }

        private View.OnClickListener mItemAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeItem(v);
            }
        };

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View container = getLayoutInflater().inflate(R.layout.item_layout, parent, false);
            container.setOnClickListener(mItemAction);
            return new MyViewHolder(container);
        }

        private int generateColor() {
            int red = ((int) (Math.random() * 200));
            int green = ((int) (Math.random() * 200));
            int blue = ((int) (Math.random() * 200));
            return Color.rgb(red, green, blue);
        }

        private void generateData() {
            for (int i = 0; i < 100; ++i) {
                mColors.add(generateColor());
            }
        }

    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public LinearLayout container;

        public MyViewHolder(View v) {
            super(v);
            container = (LinearLayout) v;
            textView = (TextView) v.findViewById(R.id.textview);
        }

        @Override
        public String toString() {
            return super.toString() + " \"" + textView.getText() + "\"";
        }
    }

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
        // new for RecyclerView
        // this has to change to use with a ViewModel
        // setContentView(R.layout.activity_main);
        // mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        // mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // mRecyclerView.setAdapter(new RVAdapter());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = CommunicationsFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView messageView = binding.receivedMessages;
        viewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                messageView.setText(s);
            }
        });

        // final RecyclerView  recyclerView = binding.recyclerView;
        mRecyclerView  = binding.recyclerView;

        // The second parameter is resource id used to set the layout(xml file)
        //      for list items in which you have a text view.
        // The third parameter is textViewResourceId which is used
        //      to set the id of TextView where you want to display the actual text.
        // ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.communication_listview, R.id.listTextView, countryList);
        // simpleList.setAdapter(arrayAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RVAdapter());

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
