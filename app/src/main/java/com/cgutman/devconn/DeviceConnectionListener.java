package com.cgutman.devconn;

import com.cgutman.adblib.AdbCrypto;

public interface DeviceConnectionListener {

    AdbCrypto loadAdbCrypto(DeviceConnection deviceConnection);

    void notifyConnectionEstablished(DeviceConnection deviceConnection);

    void notifyConnectionFailed(DeviceConnection deviceConnection, Exception exception);

    void notifyStreamClosed(DeviceConnection deviceConnection);

    void notifyStreamFailed(DeviceConnection deviceConnection, Exception exception);

    void receivedData(DeviceConnection deviceConnection, byte[] bArr, int i, int i2);
}
