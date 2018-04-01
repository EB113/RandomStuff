/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request que obteram resposta com successo com origem no host ou outsourced.

import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
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
    HashMap <byte[], HashMap <byte[], Info>> hmap;
    Config config;
    
    public HitCache(Config config) {
       this.config   = config;
       this.hmap     = new HashMap<byte[], HashMap <byte[], Info>>();
    }
    
    public void removeHitLink(byte[] nodeIdDst, byte[] nodeIdHop) {
        HashMap<byte[], Info> tmp = this.hmap.get(nodeIdDst);
        tmp.remove(nodeIdHop);
        this.hmap.put(nodeIdDst, tmp);
        return;
    }
    
    public void removeHit(byte[] nodeId) {
        this.hmap.remove(nodeId);
        return;
    }
    
    //limiting arraylist size
    public void addHit(InetAddress nodeIHopAddr, byte[] nodeIHopId, byte[] nodeIdDst, int hop_count) {
        long now    = System.currentTimeMillis();
        Info info   = new Info(nodeIHopAddr, hop_count);
        
        if(this.hmap.containsKey(nodeIdDst)) {
            HashMap<byte[], Info> tmpMap        = this.hmap.get(nodeIdDst);
            byte[] index                        = null;
            
            if(tmpMap.size() >= config.getHitArraySize()) {
                
                if(tmpMap.containsKey(nodeIHopId)) {
                    Info tmpInfo = tmpMap.get(nodeIHopId);
                    if(hop_count <= tmpInfo.hop_count) {
                        tmpMap.put(nodeIHopId, info);
                        this.hmap.put(nodeIdDst, tmpMap);
                    }
                }else {
                    
                    for (Map.Entry<byte[], Info> entry : tmpMap.entrySet()) {
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
                HashMap <byte[], Info> newMap = new HashMap <byte[], Info>();
                newMap.put(nodeIHopId, info);
                
                this.hmap.put(nodeIdDst, newMap);
            }
        }
    }
    
    public void gcHit() {
        long now  = System.currentTimeMillis();
        
        this.hmap.forEach((k1, v1) -> {
            if(v1.size() > 0 ){
                v1.forEach((k2, v2) -> {
                    if(now - v2.getTimeStamp()>config.getHitTimeDelta()) removeHitLink(k1, k2);
                });
            } else {
                removeHit(k1);
            }
        });
    }

    Tuple getHit(byte[] nodeIdDst) {
        Tuple tuple         = null;
        InetAddress addr    = null;
        long now            = 0;
        int hopCount        = 0;
        
        if(this.hmap.containsKey(nodeIdDst)) {
            HashMap <byte[], Info> tmpMap = this.hmap.get(nodeIdDst);
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
            HashMap<byte[], Info> tmpArray = this.hmap.get(nodeIdDst);
            if(tmpArray.containsKey(nodeIdSrc) && tmpArray.get(nodeIdSrc).nodeAddr_hop.equals(hopAddr)) {
                tmpArray.remove(nodeIdSrc);
            }
            this.hmap.put(nodeIdDst, tmpArray);
        }
        
    }
}
