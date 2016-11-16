package com.cgutman;

import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AndroidBase64;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class AdbUtils {
    public static final String PRIVATE_KEY_NAME = "private.key";
    public static final String PUBLIC_KEY_NAME = "public.key";

    public static AdbCrypto readCryptoConfig(File dataDir) {
        File pubKey = new File(dataDir, PUBLIC_KEY_NAME);
        File privKey = new File(dataDir, PRIVATE_KEY_NAME);
        if (!pubKey.exists() || !privKey.exists()) {
            return null;
        }
        try {
            return AdbCrypto.loadAdbKeyPair(new AndroidBase64(), privKey, pubKey);
        } catch (Exception e) {
            return null;
        }
    }

    public static AdbCrypto writeNewCryptoConfig(File dataDir) {
        File pubKey = new File(dataDir, PUBLIC_KEY_NAME);
        File privKey = new File(dataDir, PRIVATE_KEY_NAME);
        try {
            AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AndroidBase64());
            crypto.saveAdbKeyPair(privKey, pubKey);
            return crypto;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean safeClose(Closeable c) {
        if (c == null) {
            return false;
        }
        try {
            c.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
