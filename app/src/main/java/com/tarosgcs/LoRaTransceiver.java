package com.tarosgcs;

import java.util.List;
import java.util.Arrays;
import java.io.IOException;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDeviceConnection;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialPort;

// import com.tarosgcs.MessageHandler;
import com.tarosgcs.MainActivity;

public class LoRaTransceiver {

    private static final int READ_WAIT_MILLIS = 200;

    private static MessageHandler msgHandler;
    private static UsbManager usbManager;
    private static UsbDevice device;
    private static UsbSerialPort port;
    private static UsbSerialDriver driver;
    private Receiver rec;
    private Thread recThread;
    private Dummy dummy;
    private Thread dummyThread;
    private boolean connected = false;
    private String info;

    // create a separate thread that continuously queries the
    // modem (if connected) and handles the incoming messages
    private class Receiver implements Runnable {
        private int count;
        private boolean running = true;
        public Receiver() {
            count=0;
            System.out.println("receiver thread started");
        }
        @Override
        public void run() {
            byte[] buffer = new byte[256];
            while (running) {
                count += 1;
                // get a message or nothing (timeout)
                int numchars = read(buffer);
                if (numchars>0) {
                    msgHandler.receive(buffer, numchars);
                }
            }
        }
    }

    public void start_receiver() {
        // TODO: shere one should check no thread is already running
        rec = new Receiver();
        recThread = new Thread(rec);
        recThread.start();
    }

    public void stop_receiver() {
        rec.running = false;
        // wait until finished
        try {
            recThread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    public LoRaTransceiver(UsbManager manager, MessageHandler handler) {
        usbManager = manager;
        msgHandler = handler;
        info = "LoRa Transceiver created.";
        refresh();
        if (device != null) {
            connect();
        } else {
            info += "\nno connection.";
        }
    }

    public boolean isAvailable() {
        return (port != null);
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
            System.out.println("modem connected.");
            // start a background thread that reads messages from the modem
            start_receiver();
        }
    }

    // TODO: disconnect when a connection is open leads to crash
    // attempted fix by explicit join() to wait for thread finished
    public void disconnect() {
        // this terminates the receiver thread
        stop_receiver();
        connected = false;
        try {
            port.close();
        } catch (IOException ignored) {}
        port = null;
        info = "not connected.";
        System.out.println("modem disconnected.");
    }

    public int read(byte[] buffer) {
        int len = 0;
        if(connected) {
            try {
                len = port.read(buffer, READ_WAIT_MILLIS);
            } catch (IOException e) {
                // when using read with timeout, USB bulkTransfer returns -1 on timeout
                // so there is typically no exception thrown here on error
                disconnect();
                info = "connection lost: " + e.getMessage();
            }
            // TODO: check for errors if len==-1
        }
        return len;
    }

    // create a separate thread that continuously generates
    // dummy messages
    private class Dummy implements Runnable {
        private int count;
        private boolean running = true;
        public Dummy() {
            count=0;
            System.out.println("dummy thread started");
        }
        @Override
        public void run() {
            while (running) {
                count += 1;
                String message = "message " +  count;
                System.out.println("dummy thread "+message);
                byte[] stringBytes = message.getBytes();
                msgHandler.receive(stringBytes, stringBytes.length);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start_dummy() {
        // TODO: here one should check no thread is already running
        dummy = new Dummy();
        dummyThread = new Thread(dummy);
        dummyThread.start();
    }

    public void stop_dummy() {
        dummy.running = false;
        // wait until finished
        try {
            dummyThread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

}
