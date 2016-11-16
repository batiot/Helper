package com.cgutman.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.devconn.DeviceConnection;
import com.cgutman.devconn.DeviceConnectionListener;

import java.util.HashMap;

public class ShellService extends Service implements DeviceConnectionListener {
    private static final int CONN_BASE = 12131;
    private static final int FAILED_BASE = 12111;
    private ShellServiceBinder binder;
    private HashMap<String, DeviceConnection> currentConnectionMap;
    private int foregroundId;
    private ShellListener listener;
    private WakeLock wakeLock;
    private WifiLock wlanLock;

    public class ShellServiceBinder extends Binder {
        public DeviceConnection createConnection(String host, int port) {
            DeviceConnection conn = new DeviceConnection(ShellService.this.listener, host, port);
            ShellService.this.listener.addListener(conn, ShellService.this);
            return conn;
        }

        public DeviceConnection findConnection(String host, int port) {
            return (DeviceConnection) ShellService.this.currentConnectionMap.get(host + ":" + port);
        }

        public void notifyPausingActivity(DeviceConnection devConn) {
        }

        public void notifyResumingActivity(DeviceConnection devConn) {
        }

        public void notifyDestroyingActivity(DeviceConnection devConn) {
            if (devConn.isClosed()) {
                ((NotificationManager) ShellService.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ShellService.this.getFailedNotificationId(devConn));
            }
            if (ShellService.this.currentConnectionMap.isEmpty()) {
                ShellService.this.stopSelf();
            }
        }

        public void addListener(DeviceConnection conn, DeviceConnectionListener listener) {
            ShellService.this.listener.addListener(conn, listener);
        }

        public void removeListener(DeviceConnection conn, DeviceConnectionListener listener) {
            ShellService.this.listener.removeListener(conn, listener);
        }
    }

    public ShellService() {
        this.binder = new ShellServiceBinder();
        this.listener = new ShellListener(this);
        this.currentConnectionMap = new HashMap();
    }

    private synchronized void acquireWakeLocks() {
        if (this.wlanLock == null) {
            this.wlanLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(1, "Remote ADB Shell");
        }
        if (this.wakeLock == null) {
            this.wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1, "Remote ADB Shell");
        }
        this.wakeLock.acquire();
        this.wlanLock.acquire();
    }

    private synchronized void releaseWakeLocks() {
        this.wlanLock.release();
        this.wakeLock.release();
    }

    public IBinder onBind(Intent arg0) {
        return this.binder;
    }

    private int getFailedNotificationId(DeviceConnection devConn) {
        return getConnectionString(devConn).hashCode() + FAILED_BASE;
    }

    private int getConnectedNotificationId(DeviceConnection devConn) {
        return getConnectionString(devConn).hashCode() + CONN_BASE;
    }

    /*
    private PendingIntent createPendingIntentForConnection(DeviceConnection devConn) {

        Context appContext = getApplicationContext();
        Intent i = new Intent(appContext, AdbShell.class);
        i.putExtra("IP", devConn.getHost());
        i.putExtra("Port", devConn.getPort());
        i.setAction(getConnectionString(devConn));
        return PendingIntent.getActivity(appContext, 0, i, 134217728);
    }


    private Notification createNotification(DeviceConnection devConn, boolean connected) {
        String ticker;
        String message;
        boolean z = true;
        if (connected) {
            ticker = "Connection Established";
            message = "Connected to " + getConnectionString(devConn);
        } else {
            ticker = "Connection Terminated";
            message = "Connection to " + getConnectionString(devConn) + " failed";
        }
        Builder ongoing = new Builder(getApplicationContext()).setTicker("Remote ADB Shell - " + ticker).setSmallIcon(C2392R.drawable.notificationicon).setOnlyAlertOnce(true).setOngoing(connected);
        if (connected) {
            z = false;
        }
        return ongoing.setAutoCancel(z).setContentTitle("Remote ADB Shell").setContentText(message).setContentIntent(createPendingIntentForConnection(devConn)).build();
    }


    private void updateNotification(DeviceConnection devConn, boolean connected) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        removeNotification(devConn);
        if (!connected) {
            nm.notify(getFailedNotificationId(devConn), createNotification(devConn, connected));
        } else if (this.foregroundId != 0) {
            nm.notify(getConnectedNotificationId(devConn), createNotification(devConn, connected));
        } else {
            this.foregroundId = getConnectedNotificationId(devConn);
            startForeground(this.foregroundId, createNotification(devConn, connected));
        }
    }

    private void removeNotification(DeviceConnection devConn) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(getFailedNotificationId(devConn));
        if (getConnectedNotificationId(devConn) == this.foregroundId) {
            DeviceConnection newConn = null;
            for (DeviceConnection conn : this.currentConnectionMap.values()) {
                if (devConn != conn) {
                    newConn = conn;
                    break;
                }
            }
            if (newConn == null) {
                stopForeground(true);
                this.foregroundId = 0;
                return;
            }
            this.foregroundId = getConnectedNotificationId(newConn);
            nm.cancel(this.foregroundId);
            startForeground(this.foregroundId, createNotification(newConn, true));
            return;
        }
        nm.cancel(getConnectedNotificationId(devConn));
    }
    */

    private String getConnectionString(DeviceConnection devConn) {
        return devConn.getHost() + ":" + devConn.getPort();
    }

    private void addNewConnection(DeviceConnection devConn) {
        this.currentConnectionMap.put(getConnectionString(devConn), devConn);
        acquireWakeLocks();
    }

    private void removeConnection(DeviceConnection devConn) {
        this.currentConnectionMap.remove(getConnectionString(devConn));
        releaseWakeLocks();
        if (this.currentConnectionMap.isEmpty()) {
            stopSelf();
        }
    }

    public void notifyConnectionEstablished(DeviceConnection devConn) {
        addNewConnection(devConn);
        //updateNotification(devConn, true);
    }

    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
    }

    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
        //updateNotification(devConn, false);
        removeConnection(devConn);
    }

    public void notifyStreamClosed(DeviceConnection devConn) {
        //removeNotification(devConn);
        removeConnection(devConn);
    }

    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return null;
    }

    public void receivedData(DeviceConnection devConn, byte[] data, int offset, int length) {
    }

    public boolean canReceiveData() {
        return false;
    }

    public boolean isConsole() {
        return false;
    }

}
