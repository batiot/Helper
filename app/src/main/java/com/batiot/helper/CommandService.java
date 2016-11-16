package com.batiot.helper;


import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cgutman.AdbUtils;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.devconn.DeviceConnection;
import com.cgutman.devconn.DeviceConnectionListener;

import java.nio.charset.StandardCharsets;

/**
 * Created by bat on 05/09/16.
 */
public class CommandService extends Service implements DeviceConnectionListener {


    private static final String LOG_TAG = "CommandService";

    //final int CONN_BASE = 12131;
    //final int FAILED_BASE = 12111;
    DeviceConnection deviceConnection;
    PowerManager.WakeLock wakeLock;
    WifiManager.WifiLock wlanLock;

    public CommandService() {
        Log.d("CommandService","instanciate");
    }


    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;

    @Override
    // Fires when a service is first initialized
    public void onCreate() {
        Log.d(LOG_TAG,"onCreate");
        super.onCreate();
        // An Android handler thread internally operates on a looper.
        mHandlerThread = new HandlerThread("CommandService.HandlerThread");
        mHandlerThread.start();
        // An Android service handler is a handler running on a specific background thread.
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());
    }


    //https://guides.codepath.com/android/Managing-Threads-and-Custom-Services

    // Define how the handler will process messages
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Define how to handle any incoming messages here
        @Override
        public void handleMessage(Message message) {
            // ...
            // When needed, stop the service with
            // stopSelf();
        }
    }





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Send empty message to background thread
        mServiceHandler.sendEmptyMessageDelayed(0, 500);
        // or run code in background
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                // Do something here in background!
                // ...
                // If desired, stop the service
                stopSelf();
            }
        });




        if (intent.getAction().equals(Constants.ACTION.STARTCOMMAND_ACTION)) {
            //AdbUtils.writeNewCryptoConfig(this.getFilesDir());
            deviceConnection = createConnection("localhost",5555);
            deviceConnection.startConnect();
        } else if (intent.getAction().equals(Constants.ACTION.SHELL_ACTION)) {
            String command = intent.getStringExtra("command");
            Log.i(LOG_TAG, "Shell command "+command);
            if (command != null) {
                    deviceConnection.queueCommand(command + '\n');
            }
        } else if (intent.getAction().equals(Constants.ACTION.STOPCOMMAND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop command Intent");
            stopSelf();
        }
        return START_STICKY;
    }

    public DeviceConnection createConnection(String host, int port) {
        DeviceConnection conn = new DeviceConnection(this, host, port);
        return conn;
    }


    public void notifyConnectionEstablished(DeviceConnection devConn) {
        Log.d("CommandService","notifyConnectionEstablished");
        //acquireWakeLocks();
    }

    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
        Log.d("CommandService","notifyConnectionFailed "+e);
    }

    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
        Log.d("CommandService","notifyStreamFailed "+e);
        //releaseWakeLocks();
        stopSelf();
    }

    public void notifyStreamClosed(DeviceConnection devConn) {
        Log.d("CommandService","notifyStreamClosed");
        //releaseWakeLocks();
        stopSelf();
    }

    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return AdbUtils.readCryptoConfig(this.getFilesDir());

    }

    public void receivedData(DeviceConnection devConn, byte[] data, int offset, int length) {
        if (data[(offset + length) - 1] == 7) {
            length--;
        }
        String str = new String(data, StandardCharsets.UTF_8);
        Log.d("CommandService","receivedData "+str);
    }



    // Defines the shutdown sequence
    @Override
    public void onDestroy() {
        // Cleanup service before destruction
        mHandlerThread.quit();
    }

    // Binding is another way to communicate between service and activity
    // Not needed here, local broadcasts will be used instead
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

}
