package com.cgutman.devconn;

import com.cgutman.AdbUtils;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import com.cgutman.devconn.DeviceConnectionListener;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class DeviceConnection implements Closeable {
    private static final int CONN_TIMEOUT = 5000;
    private boolean closed;
    private LinkedBlockingQueue<byte[]> commandQueue;
    private AdbConnection connection;
    private String host;
    private DeviceConnectionListener listener;
    private int port;
    private AdbStream shellStream;

    class DevicePlug implements Runnable {
        DevicePlug() {
        }
        public void run() {
            Socket socket = new Socket();
            AdbCrypto crypto = DeviceConnection.this.listener.loadAdbCrypto(DeviceConnection.this);
            if (crypto != null) {
                try {
                    socket.connect(new InetSocketAddress(DeviceConnection.this.host, DeviceConnection.this.port), DeviceConnection.CONN_TIMEOUT);
                    try {
                        DeviceConnection.this.connection = AdbConnection.create(socket, crypto);
                        DeviceConnection.this.connection.connect();
                        DeviceConnection.this.shellStream = DeviceConnection.this.connection.open("shell:");
                        if (!true) {
                            AdbUtils.safeClose(DeviceConnection.this.shellStream);
                            if (!AdbUtils.safeClose(DeviceConnection.this.connection)) {
                                try {
                                    socket.close();
                                    return;
                                } catch (IOException e) {
                                    return;
                                }
                            }
                            return;
                        }
                    } catch (IOException e2) {
                        DeviceConnection.this.listener.notifyConnectionFailed(DeviceConnection.this, e2);
                        //if (null == null) {
                            AdbUtils.safeClose(DeviceConnection.this.shellStream);
                            if (!AdbUtils.safeClose(DeviceConnection.this.connection)) {
                                try {
                                    socket.close();
                                    return;
                                } catch (IOException e3) {
                                    return;
                                }
                            }
                            return;
                        //}
                    } catch (InterruptedException e4) {
                        DeviceConnection.this.listener.notifyConnectionFailed(DeviceConnection.this, e4);
                        //if (null == null) {
                            AdbUtils.safeClose(DeviceConnection.this.shellStream);
                            if (!AdbUtils.safeClose(DeviceConnection.this.connection)) {
                                try {
                                    socket.close();
                                    return;
                                } catch (IOException e5) {
                                    return;
                                }
                            }
                            return;
                        //}
                    } catch (Throwable th) {
                        //if (null == null) {
                            AdbUtils.safeClose(DeviceConnection.this.shellStream);
                            if (!AdbUtils.safeClose(DeviceConnection.this.connection)) {
                                try {
                                    socket.close();
                                    return;
                                } catch (IOException e6) {
                                    return;
                                }
                            }
                            return;
                        //}
                    }
                    DeviceConnection.this.listener.notifyConnectionEstablished(DeviceConnection.this);
                    DeviceConnection.this.startReceiveThread();
                    DeviceConnection.this.sendLoop();
                } catch (IOException e22) {
                    DeviceConnection.this.listener.notifyConnectionFailed(DeviceConnection.this, e22);
                }
            }
        }
    }

    class ShellLoop implements Runnable {
        ShellLoop() {
        }

        public void run() {
            while (!DeviceConnection.this.shellStream.isClosed()) {
                try {
                    byte[] data = DeviceConnection.this.shellStream.read();
                    DeviceConnection.this.listener.receivedData(DeviceConnection.this, data, 0, data.length);
                } catch (IOException e) {
                    DeviceConnection.this.listener.notifyStreamFailed(DeviceConnection.this, e);
                } catch (InterruptedException e2) {
                    DeviceConnection.this.listener.notifyStreamFailed(DeviceConnection.this, e2);
                    AdbUtils.safeClose(DeviceConnection.this);
                }
            }
            DeviceConnection.this.listener.notifyStreamClosed(DeviceConnection.this);
        }
    }

    public DeviceConnection(DeviceConnectionListener listener, String host, int port) {
        this.commandQueue = new LinkedBlockingQueue();
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public boolean queueCommand(String command) {
        try {
            this.commandQueue.add(command.getBytes("UTF-8"));
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    public boolean queueBytes(byte[] buffer) {
        this.commandQueue.add(buffer);
        return true;
    }

    public void startConnect() {
        new Thread(new com.cgutman.devconn.DeviceConnection.DevicePlug()).start();
    }

    private void sendLoop() {
        while (true) {
            try {
                byte[] command = (byte[]) this.commandQueue.take();
                if (this.shellStream.isClosed()) {
                    break;
                }
                this.shellStream.write(command);
            } catch (IOException e) {
                this.listener.notifyStreamFailed(this, e);
            } catch (InterruptedException e2) {
                this.listener.notifyStreamFailed(this, e2);
                AdbUtils.safeClose(this);
            }
        }
        this.listener.notifyStreamClosed(this);
    }

    private void startReceiveThread() {
        new Thread(new ShellLoop()).start();
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void close() throws IOException {
        if (!isClosed()) {
            this.closed = true;
            AdbUtils.safeClose(this.shellStream);
            AdbUtils.safeClose(this.connection);
            this.commandQueue.add(new byte[0]);
        }
    }
}
