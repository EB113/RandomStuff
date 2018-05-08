/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author pedro
 */
public class DataReplyCache {
    
    //Value Class
    class Info {
        
        byte[]                  raw;
        byte[]                  nodeId_dst;
        
        LinkedList<InetAddress> usedPeers;
        
        long                    timestamp;
        
        Info(LinkedList<InetAddress> usedPeers, byte[] nodeId_dst, long timestamp, byte[] raw) {
            
            this.nodeId_dst         = nodeId_dst;
            this.raw                = raw;
            if(usedPeers != null)
                this.usedPeers      = usedPeers;
            else
                this.usedPeers      = new LinkedList<InetAddress>();
            this.timestamp          = timestamp;
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    //NODEID OF NODE THAT REQUESTED X INFO LIST
    HashMap <ByteArray, HashMap<ByteArray, Info>> gem;
    Config config;

    DataReplyCache(Config config) {
        this.config     = config;
        this.gem        = new HashMap<ByteArray, HashMap<ByteArray, Info>>();
    }
    
}
