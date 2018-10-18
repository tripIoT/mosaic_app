package org.cbase.blinkendroid.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cbase.blinkendroid.Constants;

import android.util.Log;

public class AbstractBlinkendroidProtocol {

    public static final Integer PROTOCOL_PLAYER = 42;
    public static final Integer COMMAND_PLAYER_TIME = 23;
    public static final Integer COMMAND_CLIP = 17;
    public static final Integer COMMAND_PLAY = 11;
    public static final Integer COMMAND_INIT = 77;
    public static final Integer COMMAND_SHUTDOWN = 69;

    protected BufferedOutputStream out;
    protected BufferedInputStream in;
    protected Socket socket;
    protected ReceiverThread receiverThread;
    protected final HashMap<Integer, CommandHandler> handlers = new HashMap<Integer, CommandHandler>();
    private List<ConnectionListener> connectionListener = new ArrayList<ConnectionListener>();
    private boolean server;

    protected AbstractBlinkendroidProtocol(final Socket socket,
                                           ConnectionListener connectionListener, boolean server)
            throws IOException {
        this.socket = socket;
        this.server = server;
        long t = System.currentTimeMillis();
        this.out = new BufferedOutputStream(socket.getOutputStream());
        this.in = new BufferedInputStream(socket.getInputStream());
        this.connectionListener.add(connectionListener);
        receiverThread = new ReceiverThread();
        receiverThread.start();
        System.out.println("AbstractBlinkendroidProtocol constructor "
                + (System.currentTimeMillis() - t));
    }

    public void addConnectionClosedListener(
            ConnectionListener connectionListener) {
        this.connectionListener.add(connectionListener);
    }

    public void registerHandler(Integer proto, CommandHandler handler) {
        handlers.put(proto, handler);
    }

    public void unregisterHandler(CommandHandler handler) {
        handlers.remove(handler);
    }

    protected void connectionClosed(InetAddress inetAddress) {
        for (ConnectionListener listener : connectionListener) {
            listener.connectionClosed(inetAddress);
        }
    }

    protected void connectionOpened(InetAddress inetAddress) {
        for (ConnectionListener listener : connectionListener) {
            listener.connectionOpened(inetAddress);
        }
    }

    public void close() {
        System.out.println(getMyName() + " BlinkendroidProtocol: Start close");
        try {
            out.close();
            System.out.println(getMyName()
                    + " BlinkendroidProtocol: out closed.");
            // if (!server)// ugly hack, server needs to long
            // in.close();// also client needs to long
            System.out.println(getMyName()
                    + " BlinkendroidProtocol: in closed.");
            socket.close();
            System.out.println(getMyName()
                    + " BlinkendroidProtocol: Socket closed.");
        } catch (IOException e) {
            System.out.println(getMyName()
                    + " BlinkendroidProtocol: closed failed ");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        close();
        if (null != receiverThread) {
            receiverThread.shutdown();
        }
        System.out.println(getMyName() + " Protocol shutdown.");
    }

    // Inner classes:

    /**
     * A thread that receives information
     */
    class ReceiverThread extends Thread {

        volatile private boolean running = true;

        @Override
        public void run() {
            running = true;
            System.out.println(getMyName() + " InputThread started");
            int inputLine;
            connectionOpened(socket.getInetAddress());
            try {
                byte[] buffer = new byte[4];
                while (running && in.read(buffer) != -1) {
                    inputLine = ByteBuffer.wrap(buffer).getInt();
                    if (!running) // fast exit
                        break;

                    // System.out.println(getMyName() +
                    // " InputThread received: "
                    // + inputLine);

                    CommandHandler handler = handlers.get(inputLine);
                    if (null != handler)
                        handler.handle(in);
                }
            } catch (SocketException e) {
                System.out.println(getMyName() + " Socket closed.");
            } catch (IOException e) {
                System.out.println(getMyName() + " InputThread fucked ");
                e.printStackTrace();
            }
            System.out.println(getMyName() + " InputThread ended!!!!!!! ");

            connectionClosed(socket.getInetAddress());
            close();
        }

        public void shutdown() {
            System.out.println(getMyName() + " ReceiverThread shutdown start");
            running = false;
            interrupt();
            System.out.println(getMyName()
                    + " ReceiverThread shutdown interrupted");
            // try {
            // join();
            // } catch (InterruptedException e) {
            // System.out.println(getMyName() + " ReceiverThread join failed");
            // e.printStackTrace();
            // }
            System.out.println(getMyName() + " ReceiverThread shutdown end");
        }
    }

    protected String getMyName() {
        if (server)
            return "Server ";// + socket.getRemoteSocketAddress();
        else
            return "Client ";// + socket.getRemoteSocketAddress();
    }

    protected long readLong(BufferedInputStream in) throws IOException {
        byte[] buffer = new byte[8];
        // try {
        in.read(buffer);
        // } catch (IOException e) {
        // Log.e(Constants.LOG_TAG,"readLong failed ",e);
        // }
        return ByteBuffer.wrap(buffer).getLong();
    }

    protected int readInt(BufferedInputStream in) throws IOException {
        byte[] buffer = new byte[4];
        // try {
        in.read(buffer);
        // } catch (IOException e) {
        // Log.e(Constants.LOG_TAG,"readLong failed ",e);
        // }
        return ByteBuffer.wrap(buffer).getInt();
    }

    protected float readFloat(BufferedInputStream in) throws IOException {
        byte[] buffer = new byte[16];
        // try {
        in.read(buffer);
        // } catch (IOException e) {
        // Log.e(Constants.LOG_TAG,"readLong failed ",e);
        // }
        return ByteBuffer.wrap(buffer).getFloat();
    }

    protected void writeInt(BufferedOutputStream out, int i) throws IOException {
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).putInt(i);
        // try {
        out.write(buffer);
        // } catch (IOException e) {
        // Log.e(Constants.LOG_TAG,"writeInt failed ",e);
        // }
    }

    protected void writeFloat(BufferedOutputStream out, float f)
            throws IOException {
        byte[] buffer = new byte[16];
        ByteBuffer.wrap(buffer).putFloat(f);
        // try {
        out.write(buffer);
        // } catch (IOException e) {
        // Log.e(Constants.LOG_TAG,"writeFloat failed ",e);
        // }
    }

    protected void writeLong(BufferedOutputStream out, long l)
            throws IOException {
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).putLong(l);
        // try {
        out.write(buffer);
        // } catch (IOException e) {
        // Log.e(Constants.LOG_TAG,"writeLong failed ",e);
        // }
    }
}