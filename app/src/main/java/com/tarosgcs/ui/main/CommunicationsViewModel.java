package com.tarosgcs.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tarosgcs.SystemMessage;

import java.util.ArrayList;

public class CommunicationsViewModel extends ViewModel {

    // TODO
    // this class should not only hold the strings of the message and info display
    // but also the whole list of messages
    private MutableLiveData<String> infoText;
    private MutableLiveData<ArrayList<SystemMessage>> allSystemMessages;

    public CommunicationsViewModel() {
        infoText = new MutableLiveData<>();
        allSystemMessages = new MutableLiveData<>();
        ArrayList<SystemMessage> startList = new ArrayList<SystemMessage>();
        startList.add(new SystemMessage("GCS", 0, 100, "startup."));
        allSystemMessages.setValue(startList);
    }

    public void setInfo(String text) {
        infoText.postValue(text);
    }
    public void addSystemMessage(SystemMessage msg) {
        ArrayList<SystemMessage> list = allSystemMessages.getValue();
        list.add(0,msg);
        allSystemMessages.postValue(list);
    }

    public LiveData<String> getInfo() {
        return infoText;
    }
    public LiveData<ArrayList<SystemMessage>> getMessages() { return allSystemMessages; }
}
