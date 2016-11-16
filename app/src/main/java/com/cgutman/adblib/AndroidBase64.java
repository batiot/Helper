package com.cgutman.adblib;

import android.util.Base64;

public class AndroidBase64 implements AdbBase64 {
    public String encodeToString(byte[] data) {
        return Base64.encodeToString(data, 2);
    }
}
