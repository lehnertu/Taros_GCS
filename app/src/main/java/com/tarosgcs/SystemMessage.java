package com.tarosgcs;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SystemMessage {
    public String sender;
    public long time;
    public int level;
    public String text;

    public SystemMessage() {
        sender = "";
        time = 0;
        level = 100;
        text = "";
    }

    public SystemMessage(String s, long t, int lv, String tx) {
        sender = s;
        time = t;
        level = lv;
        text = tx;
    }

    public void fromBuffer(byte[] buffer) {
        // fill in the message data from the buffer
        text = "invalid message " + buffer.length + " bytes " + buffer[0] + buffer[1] + buffer[2];
        if (buffer.length >= 3) {
            if ((buffer[0] == (byte)0xcc) && (buffer[1] == (byte)0x81)) {
                int len = buffer[2] & 0xFF;
                if (buffer.length >= len) {
                    text = "valid message " + len + " bytes";
                    // buffer[3...10] sender
                    if (len>=11) {
                        byte[] slice = Arrays.copyOfRange(buffer, 3, 11);
                        sender = new String(slice, StandardCharsets.UTF_8);
                    }
                    // buffer[11] level
                    // buffer[12...15] time
                    if (len>=16) {
                        level = buffer[11] & 0xFF;
                        // byte is actually a signed 8-bit integer -> wrong interpretation
                        // byte & 0xFF converts a byte to an integer with the correct value
                        long i1 = buffer[12] & 0xFF;
                        long i2 = buffer[13] & 0xFF;
                        long i3 = buffer[14] & 0xFF;
                        long i4 = buffer[15] & 0xFF;
                        time = (i1 << 24) + (i2 << 16) + (i3 << 8) + i4;
                    }
                    // the rest is the text
                    if (len>16) {
                        byte[] slice = Arrays.copyOfRange(buffer, 16, len);
                        text = new String(slice, StandardCharsets.UTF_8);
                    }
                }
            }
        }
    }
}
