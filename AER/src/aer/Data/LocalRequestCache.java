/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author pedro
 */
public class LocalRequestCache {

    //Value Class
    class Info {
        InetAddress             nodeAddr_hop;
        LinkedList<InetAddress> usedPeers;
        byte[]                  req_num;
        long                    timestamp;
        
        Info(LinkedList<InetAddress> usedPeers, InetAddress nodeId_hop, byte[] nodeId_dst,  byte[] req_num) {
            this.req_num         = req_num;
            this.nodeAddr_hop    = nodeId_hop;
            this.usedPeers       = usedPeers;
            this.timestamp       = System.currentTimeMillis();
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    //NODE ID DST X info list
    HashMap <byte[], LinkedList<Info>> hmap;
    Config config;
    
    public LocalRequestCache(Config config) {
       this.config  = config;
       this.hmap    = new HashMap<byte[], LinkedList<Info>>();
    }

    void addRequest(LinkedList<InetAddress> usedPeers, InetAddress nodeHopAddr, byte[] nodeHopId, byte[] nodeIdDst, int hop_count, byte[] req_num) {
    
        if(this.hmap.containsKey(nodeIdDst)) {
            LinkedList<Info> tmpArray = this.hmap.get(nodeIdDst);
            
            if(tmpArray.size() >= config.getReqArraySize()) {
                System.out.println("DEMASIADOS PEDIDOS AO PEER ESPECIFICO!");
                return;
            }else {
                tmpArray.add(new Info(usedPeers, nodeHopAddr, nodeIdDst, req_num));
                this.hmap.put(nodeIdDst, tmpArray);
            }
            
        }else{
            if(this.hmap.size() < config.getRequestCacheSize()) {
                LinkedList<Info> tmpArray = new LinkedList<>();
                
                tmpArray.add(new Info(usedPeers, nodeHopAddr, nodeIdDst, req_num));
                
                this.hmap.put(nodeIdDst, tmpArray);
            }else {
                System.out.println("DEMASIADOS PEDIDOS DE ROTA!");
            }
        }
    }

    //ESTOU A SUPOR QUE NAO HA PEDIDO COM MESMO SEQ ID, como resolver??
    byte[] getReqTarget(byte[] req_num) {
        
        byte[] nodeId = null;
        
        for(Map.Entry<byte[], LinkedList<Info>> pair : this.hmap.entrySet()) {
            for(Info info : pair.getValue()) {
                if(Crypto.cmpByteArray(info.req_num, req_num)) nodeId = pair.getKey();
            }
        }
        
        return nodeId;
    }

    LinkedList<InetAddress> getIncludedNodes(byte[] nodeIdDst, byte[] req_num) {
        LinkedList<InetAddress> usedPeers = null;
        
        LinkedList<Info> tmpArray = this.hmap.get(nodeIdDst);
        
        for(Info info : tmpArray) {
            if(Crypto.cmpByteArray(info.req_num, req_num)) {
                usedPeers = info.usedPeers;
            }
        }    
        
        return usedPeers;
    }
    
}
