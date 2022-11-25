package com.tarosgcs;

import java.util.Arrays;

import com.tarosgcs.ui.main.CommunicationsViewModel;

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
        byte[] slice = Arrays.copyOfRange(buffer, 0, num);
        SystemMessage lastSysMsg = new SystemMessage();
        lastSysMsg.fromBuffer(slice);
        // System.out.println(HexDump.dumpHexString(slice));
        lastMessage = lastSysMsg.text;
        if (viewModel != null) {
            viewModel.setMessage(lastMessage);
            viewModel.addSystemMessage(lastSysMsg);
        }
    }

    public String getLastMessage() {
        return lastMessage;
    }

}
