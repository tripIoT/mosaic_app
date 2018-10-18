package org.cbase.blinkendroid.server;

import java.net.InetAddress;

import org.cbase.blinkendroid.network.BlinkendroidServerProtocol;
import org.cbase.blinkendroid.network.ConnectionListener;

//import android.util.Log;

public class PlayerClient implements ConnectionListener {

    // position
    int x, y;
    // clipping
    float startX, endX, startY, endY;
    // protocol
    BlinkendroidServerProtocol blinkendroidProtocol;
    long startTime;
    PlayerManager playerManager;

    public PlayerClient(PlayerManager playerManager,
                        BlinkendroidServerProtocol blinkendroidProtocol, long startTime) {
        this.playerManager = playerManager;
        this.blinkendroidProtocol = blinkendroidProtocol;
        this.startTime = startTime;
        blinkendroidProtocol.addConnectionClosedListener(this);
    }

    public void shutdown() {
        blinkendroidProtocol.shutdown();
    }

    public void clip() {
        System.out.println("PlayerClient clip " + x + ":" + y);
        blinkendroidProtocol.clip(startX, startY, endX, endY);
    }

    public void play(String filename) {
        System.out.println("PlayerClient play  " + x + ":" + y + " filename "
                + filename);
        blinkendroidProtocol.play(x, y, System.currentTimeMillis(), startTime,
                filename);
    }

    public void arrow(int degrees, int color) {
        System.out.println("PlayerClient arrow  " + x + ":" + y + " degrees "
                + degrees + " color " + color);
        blinkendroidProtocol.arrow(degrees, color);
    }

    public void connectionClosed(InetAddress inetAddress) {
        shutdown();
        playerManager.removeClient(this);
        System.out.println("PlayerClient connectionClosed  " + x + ":" + y);
    }

    public void connectionOpened(InetAddress inetAddress) {
        System.out.println("PlayerClient connectionOpened  " + x + ":" + y);
    }
}
