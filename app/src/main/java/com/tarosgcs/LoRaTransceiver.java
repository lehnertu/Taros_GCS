package com.tarosgcs;

import java.util.List;
import java.util.EnumSet;
import java.io.IOException;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDeviceConnection;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class LoRaTransceiver {

    private static UsbManager usbManager;
    private static UsbDevice device;
    private static UsbSerialPort port;
    private static UsbSerialDriver driver;

    private boolean connected = false;
    private String info;

    public LoRaTransceiver(UsbManager manager) {
        usbManager = manager;
        info = "LoRa Transceiver created.";
        refresh();
        if (device != null) {
            connect();
        } else {
            info += "\nno connection.";
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getInfo() {
        return info;
    }

    public void refresh() {
        device = null;
        driver = null;
        port = null;
        info = "no USB serial port detected.";
        UsbSerialProber prob = UsbSerialProber.getDefaultProber();
        for(UsbDevice dev : usbManager.getDeviceList().values()) {
            device = dev;
            info = device.getDeviceName();
            info += "\n";
            driver = prob.probeDevice(device);
            if (driver != null) {
                if(driver.getPorts().size()>0) {
                    List<UsbSerialPort> ports = driver.getPorts();
                    port = ports.get(0);
                    info += "port : ";
                    info += port.getPortNumber();
                    info += "\n";
                    // abort the loop if we have a working interface
                    break;
                }
            } else {
                info += "no driver";
            }
        }
    }

    public void connect() {
        UsbDeviceConnection usbConnection = usbManager.openDevice(device);
        if(usbConnection == null) {
            if (!usbManager.hasPermission(device))
                info += "connection failed: permission denied";
            else
                info += "connection failed: open failed";
            return;
        } else {
            info += "opening connection\n";
            try {
                port.open(usbConnection);
                port.setParameters(115200, 8, 1, UsbSerialPort.PARITY_NONE);
                // if(withIoManager) {
                //     usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
                //     usbIoManager.start();
                // }
                connected = port.isOpen();
                if (connected) {
                    info += "connected";
                }
                // check if we have some usable control lines
                // try {
                //     EnumSet<UsbSerialPort.ControlLine> controlLines = port.getControlLines();
                //     if (controlLines.contains(UsbSerialPort.ControlLine.CTS)) {
                //         info += "\nhave CTS";
                //     }
                //     if (controlLines.contains(UsbSerialPort.ControlLine.DTR)) {
                //         info += "\nhave DTR";
                //     }
                // } catch (IOException ignored) {
                // }
            } catch (Exception e) {
                info += "connection failed: " + e.getMessage();
                disconnect();
            }
        }
    }

    public void disconnect() {
        connected = false;
        // controlLines.stop();
        // if(usbIoManager != null) {
        //     usbIoManager.setListener(null);
        //     usbIoManager.stop();
        // }
        // usbIoManager = null;
        try {
            port.close();
        } catch (IOException ignored) {}
        port = null;
    }
}
