/*
 * Copyright 2010 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbase.blinkendroid.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.cbase.blinkendroid.network.BlinkendroidServerProtocol;
import org.cbase.blinkendroid.network.ConnectionListener;
import org.cbase.blinkendroid.player.bml.BLMHeader;

//import android.util.Log;

public class BlinkendroidServer extends Thread {

    volatile private boolean running = false;
    volatile private ServerSocket serverSocket;
    private int port = -1;
    private PlayerManager playerManager;
    private ConnectionListener connectionListener;

    public BlinkendroidServer(ConnectionListener connectionListener, int port) {
        this.connectionListener = connectionListener;
        this.port = port;
    }

    @Override
    public void run() {

        running = true;
        System.out.println("BlinkendroidServer Thread started");

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            playerManager = new PlayerManager();
            acceptLoop();
            System.out.println("after acceptLoop");
            playerManager.shutdown();
            System.out.println("close serverSocket");
            serverSocket.close();
        } catch (final IOException x) {
            x.printStackTrace();
            System.out.println("Could not create Socket");
            throw new RuntimeException(x);
        }

        System.out.println("BlinkendroidServer Thread ended");
    }

    private void acceptLoop() {

        while (running) {
            try {
                final Socket clientSocket = accept();
                if (!running) // fast exit
                    break;
                System.out.println("BlinkendroidServer got connection "
                        /* + clientSocket.getRemoteSocketAddress().toString() */);
                final BlinkendroidServerProtocol blinkendroidProtocol = new BlinkendroidServerProtocol(
                        clientSocket, connectionListener);
                playerManager.addClient(blinkendroidProtocol);
            } catch (final IOException x) {
                x.printStackTrace();
                System.out.println("BlinkendroidServer could not accept");
            }
        }
    }

    private Socket accept() throws IOException {
        try {
            return serverSocket.accept();
        } catch (final SocketException x) {
            // swallow, this is expected after interruption by closing socket
            x.printStackTrace();
            System.out.println("serverSocket.accept failed");
            return null;
        }
    }

    public void shutdown() {
        System.out.println("BlinkendroidServer.shutdown() initiated");
        running = false;
        try {
            serverSocket.close(); // interrupt thread blocked in accept()
            join();
        } catch (final IOException x) {
            throw new RuntimeException(x);
        } catch (final InterruptedException x) {
            throw new RuntimeException(x);
        }
        System.out.println("BlinkendroidServer.shutdown() ended");
    }

    public boolean isRunning() {
        return running;
    }

    public void switchMovie(BLMHeader blmHeader) {
        playerManager.switchMovie(blmHeader);
    }
}
