/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PDU;

import aer.Data.Node;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * @author pedro
 */
public class Hello {
    ArrayList<byte[]> peers;
    byte[] nodeId;
    byte[] seq_num;
    
    public Hello() {
        peers = null;
    }
    
    public static byte[] dump(Node node) {
        
        byte[] id               = node.getId();
        byte[] seq              = node.getSeq();
        ArrayList<byte[]> peers = node.getZonePeersIds(1);
        
        int peerslen = 0, counter=0, limit = 0, it = 0;
        for(byte[] i: peers) peerslen+=i.length;
        int len = 1 + 4 + id.length + 4 + peerslen; //PDUTYPE  + NODEID + SEQNUM + PEERS
        byte[] raw = new byte[len];
        byte[] tmp = null;
        
        //PDU TYPE
        raw[counter++] = 0x00;
        limit++;
        
        //PDU TOTAL SIZE
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //NODEID DATA
        limit+=id.length;
        for(; counter<limit; counter++) {
            raw[counter] = id[it++];
        }
        it = 0;
        
        //SEQUENCE DATA
        limit+=seq.length;
        for(; counter<limit; counter++) {
            raw[counter] = seq[it++];
        }
        it = 0;
        
        //PEERS ID ARRAY
        for(byte[] entry: peers) {
            limit+=entry.length;
            for(; counter<limit; counter++) {
                raw[counter] = entry[it++];
            }
            it = 0;
        }
        
        return raw;
    }
    
    public ArrayList<byte[]> load(byte[] raw) {
        
        int counter = 1,limit = 1, it = 0;
        ArrayList<byte[]> peers  = new ArrayList();
        
        //GET PDU SIZE
        byte[] tmp = new byte[4];
        for(it = 1; it<5; it++) {
            tmp[it-1] = raw[it];
        }
        ByteBuffer wrapped = ByteBuffer.wrap(tmp);
        int num = wrapped.getInt();
        
        limit   += 1 + 4 + 32 + 4 + num; // Integer + NodeId + Integer + Peers
        counter += 1 + 4 + 32 + 4; // Integer + NodeId + Integer
                
        for(; counter<limit;) {
            tmp = new byte[8];
            for(it = 0; it<8; it++)
                tmp[it] = raw[counter++];
            peers.add(tmp);
        }
        
        return this.peers;
    }
}