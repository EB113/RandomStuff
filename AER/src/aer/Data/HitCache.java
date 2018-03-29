/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request que obteram resposta com successo com origem no host ou outsourced.

import aer.miscelaneous.Config;
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
    Config config;
    
    public HitCache(Config config) {
       this.config   = config;
       this.hmap     = new HashMap<byte[], ArrayList<Info>>();
    }
    
    public void removeHit(byte[] nodeId) {
        this.hmap.remove(nodeId);
        return;
    }
    
    //limiting arraylist size
    public void addHit(byte[] nodeIdSrc, byte[] nodeIdDst, int hop_count) {
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            ArrayList<Info> tmpArray    = this.hmap.get(nodeIdSrc);
            Info info                   = new Info(nodeIdDst, hop_count);
            long minTimeStamp           = tmpArray.get(0).getTimeStamp();
            int counter                 = 0;
            int index                   = 0;
            
            if(tmpArray.size() >= config.getHitArraySize()) {
                for(Info i : tmpArray) {
                    long curTimeStamp = i.getTimeStamp();
                    if(curTimeStamp < minTimeStamp){
                        minTimeStamp = curTimeStamp;
                        index = counter;
                    }
                    counter++;
                }
                
                tmpArray.set(index, info);
                
            }
            else {
                tmpArray.add(info);
                this.hmap.put(nodeIdSrc, tmpArray);
            }
            
        }else{
            if(this.hmap.size() < config.getHitCacheSize()) {
                ArrayList<Info> tmpArray = new ArrayList<>();
                Info info = new Info(nodeIdDst, hop_count);
                
                tmpArray.add(info);
                
                this.hmap.put(nodeIdSrc, tmpArray);
                return;
            }
        }
    }
    
    public void gcHit() {
        long now  = System.currentTimeMillis();
        
        this.hmap.forEach((k, v) -> {
            for(Info i : v) {
                if(now - i.getTimeStamp()>config.getHitTimeDelta()) removeHit(k);
            }
        });
    }
}
