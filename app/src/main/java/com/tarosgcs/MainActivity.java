package com.tarosgcs;

import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.tarosgcs.ui.main.SectionsPagerAdapter;
import com.tarosgcs.databinding.ActivityMainBinding;
import com.tarosgcs.LoRaTransceiver;
import com.tarosgcs.MessageHandler;

public class MainActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private LoRaTransceiver modem;
    private MessageHandler messageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        MessageHandler messageHandler = new MessageHandler();
        modem = new LoRaTransceiver(usbManager, messageHandler);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // LoRaTransceiver is the communication port which is available
        // to all fragments via the sectionsPagerAdapter
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
                this, getSupportFragmentManager(), modem, messageHandler);
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
    }

}
