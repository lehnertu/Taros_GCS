package com.tarosgcs;

public class LoRaTransceiver {

    private static String port_name;

    public LoRaTransceiver() {
        port_name = "none";
    }

    public static String getPortName() {
        return port_name;
    }
}
