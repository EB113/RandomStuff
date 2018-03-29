/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request que obteram resposta com successo com origem no host ou outsourced.

import java.net.Inet6Address;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;


public class HitCache {
    
   //Value Class
    class Info {
        byte[]          nodeId_dst;
        int             hop_count;
        long            timestamp;
        
        Info(byte[] nodeId_dst, int hop_count) {
            this.nodeId_dst      = nodeId_dst;
            this.hop_count       = hop_count;
            this.timestamp       = System.currentTimeMillis();
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    HashMap <byte[], ArrayList<Info>> hmap;
    int hitCacheSize;
    
    public HitCache(int hitCacheSize) {
        this.hitCacheSize   = hitCacheSize;
       this.hmap            = new HashMap<byte[], ArrayList<Info>>();
    }
    
    //limiting arraylist size
    public Object addHit(byte[] nodeIdSrc, byte[] nodeIdDst, int hop_count) {
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            return this.hmap.put(nodeIdSrc, this.hmap.get(nodeIdSrc)).add(new Info(nodeIdDst, hop_count));
        }
        return null;
    }
    
    public Object removeHit(byte[] nodeId) {
        return this.hmap.remove(nodeId);
    }
    
    public void gcHit(long hitTimeDelta) {
        long now  = System.currentTimeMillis();
        
        this.hmap.forEach((k, v) -> {
            for(Info i : v) {
                if(now - i.getTimeStamp()>hitTimeDelta) removeHit(k);
            }
        });
    }
}
