package com.app.signalprotocolimplementation.helper;

import android.util.Base64;

public class Helper {
    public static String encodeToBase64(byte[] value) {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }

    public static byte[] decodeToByteArray(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
}
