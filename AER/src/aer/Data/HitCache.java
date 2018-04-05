/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request que obteram resposta com successo com origem no host ou outsourced.

import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class HitCache {
    
   //Value Class
    class Info {
        InetAddress         nodeAddr_hop;
        int                 hop_count;
        long                timestamp;
        
            Info(InetAddress nodeId_dst, int hop_count) {
            this.nodeAddr_hop      = nodeId_dst;
            this.hop_count       = hop_count;
            this.timestamp       = System.currentTimeMillis();
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    HashMap <ByteArray, HashMap <ByteArray, Info>> hmap;
    Config config;
    
    public HitCache(Config config) {
       this.config   = config;
       this.hmap     = new HashMap<ByteArray, HashMap<ByteArray, Info>>();
    }
    
    public void removeHitLink(ByteArray nodeIdDst, ByteArray nodeIdHop) {
                
        HashMap<ByteArray, Info> tmp = this.hmap.get(nodeIdDst);
        tmp.remove(nodeIdHop);
        this.hmap.put(nodeIdHop, tmp);
        return;
    }
    
    public void removeHit(ByteArray nodeId) {
        this.hmap.remove(nodeId);
        return;
    }
    
    //limiting arraylist size
    public void addHit(InetAddress nodeIHopAddr, byte[] nodeIHopId_old, byte[] nodeIdDst_old, int hop_count) {
        long now    = System.currentTimeMillis();
        Info info   = new Info(nodeIHopAddr, hop_count);
        
        ByteArray nodeIHopId = new ByteArray(nodeIHopId_old);
        ByteArray nodeIdDst = new ByteArray(nodeIdDst_old);
        
        
        if(this.hmap.containsKey(nodeIdDst)) {
            HashMap<ByteArray, Info> tmpMap        = this.hmap.get(nodeIdDst);
            ByteArray index                        = null;
            
            if(tmpMap.size() >= config.getHitMapSize()) {
                
                if(tmpMap.containsKey(nodeIHopId)) {
                    Info tmpInfo = tmpMap.get(nodeIHopId);
                    if(hop_count <= tmpInfo.hop_count) {
                        tmpMap.put(nodeIHopId, info);
                        this.hmap.put(nodeIdDst, tmpMap);
                    }
                }else {
                    
                    for (Map.Entry<ByteArray, Info> entry : tmpMap.entrySet()) {
                        if(hop_count<=entry.getValue().hop_count) {
                            index = entry.getKey();
                        }
                    }
                    if(index != null) {
                        tmpMap.remove(index);
                        tmpMap.put(nodeIHopId, info);
                        this.hmap.put(nodeIdDst, tmpMap);
                    }
                }
                
            } else {
                
                if(tmpMap.containsKey(nodeIHopId)) {
                    Info tmpInfo = tmpMap.get(nodeIHopId);
                    if(hop_count <= tmpInfo.hop_count) {
                        tmpMap.put(nodeIHopId, info);
                        this.hmap.put(nodeIdDst, tmpMap);
                    }
                }else {
                    tmpMap.put(nodeIHopId, info);
                    this.hmap.put(nodeIdDst, tmpMap);
                }
            }
            
        }else{
            if(this.hmap.size() < config.getHitCacheSize()) {
                HashMap <ByteArray, Info> newMap = new HashMap <ByteArray, Info>();
                newMap.put(nodeIHopId, info);
                
                this.hmap.put(nodeIdDst, newMap);
            }
        }
    }
    
    public void gcHit() {
        long now  = System.currentTimeMillis();
        
        Iterator<Map.Entry<ByteArray, HashMap<ByteArray, Info>>> iter1 = this.hmap.entrySet().iterator();
        
        while (iter1.hasNext()) {
            Map.Entry<ByteArray, HashMap<ByteArray, Info>> entry1 = iter1.next();
            
            if(entry1.getValue().size() > 0) {
                
                entry1.getValue().entrySet().removeIf(entry2 -> (now - entry2.getValue().getTimeStamp() > config.getHitTimeDelta()));
                
            } else iter1.remove();
            
        }
        
        //ConcurrentModificationException
        /*
        for(Map.Entry<ByteArray, HashMap<ByteArray, Info>> pair1 : this.hmap.entrySet()) {
            if(pair1.getValue().size() > 0) {
                for(Map.Entry<ByteArray, Info> pair2 : pair1.getValue().entrySet()) {

                    if(now - pair2.getValue().getTimeStamp() > config.getZoneTimeDelta()) removeHitLink(pair1.getKey(),pair2.getKey());
                }
            }else removeHit(pair1.getKey());
        }
        
        this.hmap.forEach((k1, v1) -> {
            if(v1.size() > 0 ){
                v1.forEach((k2, v2) -> {
                    if(now - v2.getTimeStamp()>config.getHitTimeDelta()) removeHitLink(k1, k2);
                });
            } else {
                removeHit(k1);
            }
        });*/
    }

    Tuple getHit(byte[] nodeIdDst) {
        Tuple tuple         = null;
        InetAddress addr    = null;
        long now            = 0;
        int hopCount        = 0;
        
        if(this.hmap.containsKey(nodeIdDst)) {
            HashMap <ByteArray, Info> tmpMap = this.hmap.get(nodeIdDst);
            if(tmpMap.size()>0){
                for(Info info : tmpMap.values()) {
                    if(info.timestamp > now) {
                        now         = info.timestamp;
                        addr        = info.nodeAddr_hop;
                        hopCount    = info.hop_count;
                    }
                }
                tuple = new Tuple(addr, now);
            }
        }
        
        return tuple;
    }

    void rmRoute(byte[] nodeIdSrc, byte[] nodeIdDst, InetAddress hopAddr) {
        
        if(this.hmap.containsKey(nodeIdDst)){
            HashMap<ByteArray, Info> tmpArray = this.hmap.get(nodeIdDst);
            if(tmpArray.containsKey(nodeIdSrc) && tmpArray.get(nodeIdSrc).nodeAddr_hop.equals(hopAddr)) {
                tmpArray.remove(nodeIdSrc);
            }
            this.hmap.put(new ByteArray(nodeIdDst), tmpArray);
        }
        
    }
}
