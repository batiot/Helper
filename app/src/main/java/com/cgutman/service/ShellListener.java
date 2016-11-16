package com.cgutman.service;

import android.app.Service;

import com.cgutman.AdbUtils;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.devconn.DeviceConnection;
import com.cgutman.devconn.DeviceConnectionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class ShellListener implements DeviceConnectionListener {
    private static final int TERM_LENGTH = 25000;
    //private HashMap<DeviceConnection, ConsoleBuffer> consoleMap;
    private HashMap<DeviceConnection, LinkedList<DeviceConnectionListener>> listenerMap;
    private Service service;

    public ShellListener(Service service) {
        this.listenerMap = new HashMap();
        this.service = service;
    }

    public synchronized void addListener(DeviceConnection conn, DeviceConnectionListener listener) {
        LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(conn);
        if (listeners != null) {
            listeners.add(listener);
        } else {
            listeners = new LinkedList();
            listeners.add(listener);
            this.listenerMap.put(conn, listeners);
        }
    }

    public synchronized void removeListener(DeviceConnection conn, DeviceConnectionListener listener) {
        LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(conn);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void notifyConnectionEstablished(DeviceConnection devConn) {
//        this.consoleMap.put(devConn, new ConsoleBuffer(TERM_LENGTH));
        LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(devConn);
        if (listeners != null) {
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((DeviceConnectionListener) it.next()).notifyConnectionEstablished(devConn);
            }
        }
    }

    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
        LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(devConn);
        if (listeners != null) {
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((DeviceConnectionListener) it.next()).notifyConnectionFailed(devConn, e);
            }
        }
    }

    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
/*        if (this.consoleMap.remove(devConn) != null) {
            LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(devConn);
            if (listeners != null) {
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    ((DeviceConnectionListener) it.next()).notifyStreamFailed(devConn, e);
                }
            }
        }*/
    }

    public void notifyStreamClosed(DeviceConnection devConn) {
 /*       if (this.consoleMap.remove(devConn) != null) {
            LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(devConn);
            if (listeners != null) {
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    ((DeviceConnectionListener) it.next()).notifyStreamClosed(devConn);
                }
            }
        }*/
    }

    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return AdbUtils.readCryptoConfig(this.service.getFilesDir());
    }

    public void receivedData(DeviceConnection devConn, byte[] data, int offset, int length) {
       /*
        ConsoleBuffer console = (ConsoleBuffer) this.consoleMap.get(devConn);
        if (console != null) {
            if (data[(offset + length) - 1] == 7) {
                length--;
            }
            console.append(data, offset, length);
            LinkedList<DeviceConnectionListener> listeners = (LinkedList) this.listenerMap.get(devConn);
            if (listeners != null) {
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    DeviceConnectionListener listener = (DeviceConnectionListener) it.next();
                    if (listener.isConsole()) {
                        listener.consoleUpdated(devConn, console);
                    }
                }
            }
        }*/
    }

    public boolean canReceiveData() {
        return true;
    }

    public boolean isConsole() {
        return false;
    }
}
