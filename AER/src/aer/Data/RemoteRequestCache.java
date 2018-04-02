/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request Activos.

import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;



public class RemoteRequestCache {
    
    //Value Class
    class Info {
        byte[]                  nodeId_dst;
        InetAddress             nodeAddr_hop;
        LinkedList<InetAddress> usedPeers;
        byte[]                  req_num;
        long                    timestamp;
        
        Info(LinkedList<InetAddress> usedPeers, InetAddress nodeId_hop, byte[] nodeId_dst,  byte[] req_num) {
            this.req_num         = req_num;
            this.nodeId_dst      = nodeId_dst;
            this.nodeAddr_hop    = nodeId_hop;
            this.usedPeers      = usedPeers;
            this.timestamp       = System.currentTimeMillis();
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    //NODEID OF NODE THAT REQUESTED X INFO LIST
    HashMap <ByteArray, LinkedList<Info>> hmap;
    Config config;
    
    public RemoteRequestCache(Config config) {
       this.config  = config;
       this.hmap    = new HashMap<ByteArray, LinkedList<Info>>();
    }
    
    //limiting arraylist size
    public void addRequest(LinkedList<InetAddress> usedPeers, InetAddress peer_hop, byte[] nodeIdSrc_old, byte[] nodeIdDst, byte[] req_num) {
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            LinkedList<Info> tmpArray = this.hmap.get(nodeIdSrc);
            
            if(tmpArray.size() >= config.getReqArraySize()) return;
            else {
                tmpArray.add(new Info(usedPeers, peer_hop, nodeIdDst, req_num));
                this.hmap.put(nodeIdSrc, tmpArray);
            }
            
        }else{
            if(this.hmap.size() < config.getRequestCacheSize()) {
                LinkedList<Info> tmpArray = new LinkedList<>();
                Info info = new Info(usedPeers, peer_hop, nodeIdDst, req_num);
                
                tmpArray.add(info);
                
                this.hmap.put(nodeIdSrc, tmpArray);
                return;
            }
        }
    }
    
    public Object removeRequest(ByteArray nodeId) {
        return this.hmap.remove(nodeId);
    }
    
    public void gcReq() {
        long now  = System.currentTimeMillis();
        
        Iterator<Map.Entry<ByteArray, LinkedList<Info>>> iter1 = this.hmap.entrySet().iterator();
        
        while (iter1.hasNext()) {
            Map.Entry<ByteArray, LinkedList<Info>>entry1 = iter1.next();
            
            if(entry1.getValue().size() > 0) {
                
                entry1.getValue().removeIf(entry2 -> (now - entry2.getTimeStamp() > config.getReqTimeDelta()));
                
            } else iter1.remove();
            
        }
        
        
        //ConcurrentModificationException
        /*
        
        // HashMap <ByteArray, LinkedList<Info>> hmap;
        for(Map.Entry<ByteArray, LinkedList<Info>> pair1 : this.hmap.entrySet()) {
            for(Info i : pair1.getValue()) {
                if(now - i.getTimeStamp()>config.getReqTimeDelta()) removeRequest(pair1.getKey());
            }
        }
        
        this.hmap.forEach((k, v) -> {
            for(Info i : v) {
                if(now - i.getTimeStamp()>config.getReqTimeDelta()) removeRequest(k);
            }
        });*/
    }

    InetAddress rmReq(byte[] nodeIdDst, byte[] nodeIdSrc_old, byte[] req_num) {
        InetAddress hopAddr = null;
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        LinkedList<Info> tmpArray = this.hmap.get(nodeIdSrc);
        
        for(Info info : tmpArray) {
            if(Crypto.cmpByteArray(info.nodeId_dst, nodeIdDst) && Crypto.cmpByteArray(info.req_num, req_num)) {
                hopAddr = info.nodeAddr_hop;
                tmpArray.remove(info);
            }
        }
        
        this.hmap.put(nodeIdSrc, tmpArray);
        
        return hopAddr;
    }

    byte[] getReqTarget(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        byte[] nodeId = null;
        
        LinkedList<Info> tmpArray = this.hmap.get(nodeIdSrc);
        
        for(Info info : tmpArray) {
            if(Crypto.cmpByteArray(info.req_num, req_num)) {
                nodeId = info.nodeId_dst;
            }
        }
        
        return nodeId;
    }
    
    InetAddress getReqAddr(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
        InetAddress nodeAddr = null;
        
        LinkedList<Info> tmpArray = this.hmap.get(nodeIdSrc);
        
        for(Info info : tmpArray) {
            if(Crypto.cmpByteArray(info.req_num, req_num)) {
                nodeAddr = info.nodeAddr_hop;
            }
        }
        
        return nodeAddr;
    }

    LinkedList<InetAddress> getIncludedNodes(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        LinkedList<InetAddress> usedPeers = null;
        
        LinkedList<Info> tmpArray = this.hmap.get(nodeIdSrc);
        
        for(Info info : tmpArray) {
            if(Crypto.cmpByteArray(info.req_num, req_num) && Crypto.cmpByteArray(info.nodeId_dst, nodeIdDst)) {
                usedPeers = info.usedPeers;
            }
        }    
        
        return usedPeers;
    }
}
