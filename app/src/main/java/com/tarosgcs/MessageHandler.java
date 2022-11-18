package com.tarosgcs;

import com.tarosgcs.ui.main.CommunicationsViewModel;
import com.tarosgcs.HexDump;

public class MessageHandler {

    private CommunicationsViewModel viewModel = null;
    private String lastMessage;

    public MessageHandler() {
        lastMessage = "default message.";
    }

    public void setViewModel(CommunicationsViewModel view) {
        viewModel = view;
    }

    public void receive(byte[] buffer, int num) {
        // lastMessage = new String(buffer, num);
        lastMessage = HexDump.dumpHexString(buffer);
        if (viewModel != null) viewModel.setMessage(lastMessage);
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
