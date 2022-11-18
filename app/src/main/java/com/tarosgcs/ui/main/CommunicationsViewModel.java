package com.tarosgcs.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class CommunicationsViewModel extends ViewModel {

    private MutableLiveData<String> messageText = new MutableLiveData<>();
    private MutableLiveData<String> infoText = new MutableLiveData<>();

    public void setMessage(String text) {
        messageText.postValue(text);
    }
    public void setInfo(String text) {
        infoText.setValue(text);
    }

    public LiveData<String> getMessage() {
        return messageText;
    }
    public LiveData<String> getInfo() {
        return infoText;
    }
}
