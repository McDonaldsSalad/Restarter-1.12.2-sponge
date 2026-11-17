/*
 * Decompiled with CFR 0.152.
 */
package net.craftersland.restarter.toolkit;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import net.craftersland.restarter.RR;

public class WrapperLink {
    private RR rr;
    private int port = 25561;

    public WrapperLink(RR rr) {
        this.rr = rr;
    }

    public void dispatchLocalUDPPacket(String paramString) {
        this.port = this.rr.getConfigHandler().getInteger("ToolKit", "RemotePort");
        try (DatagramSocket localDatagramSocket = null;){
            try {
                localDatagramSocket = new DatagramSocket();
                DatagramPacket localDatagramPacket = new DatagramPacket(paramString.getBytes(), paramString.getBytes().length, InetAddress.getByName("localhost"), this.port);
                localDatagramSocket.send(localDatagramPacket);
            }
            catch (Exception e) {
                e.printStackTrace();
                localDatagramSocket.close();
            }
        }
    }
}

