/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.TCP;

import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author pedro
 */
public class ComsTCP {
    private ArrayBlockingQueue<Object> coms;
    private byte[] peer_pubk;
    private byte[] pubk;
    private byte[] shared_key;
    private byte[] data;
    private InetAddress addr;

    public ComsTCP(byte[] pubk) {
        this.coms       = new ArrayBlockingQueue(1, true);
        this.pubk       = pubk;
        this.peer_pubk  = null;
        this.addr       = null;
        this.shared_key = null;
    }

    public byte[] getPeer_pubk() {
        return peer_pubk;
    }

    public void setPeer_pubk(byte[] peer_pubk) {
        this.peer_pubk = peer_pubk;
    }

    public byte[] getShared_key() {
        return shared_key;
    }

    public void setShared_key(byte[] shared_key) {
        this.shared_key = shared_key;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public ArrayBlockingQueue<Object> getComs() {
        return coms;
    }
    
    public byte[] getData() {
        return this.data;
    }
                
    public void setData(byte[] data) {
        this.data = data;
    }
    
}
