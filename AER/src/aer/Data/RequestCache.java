/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request Activos.

import aer.miscelaneous.Config;
import java.net.Inet6Address;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;



public class RequestCache {
    
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
    
    public RequestCache(Config config) {
       this.config  = config;
       this.hmap    = new HashMap<byte[], ArrayList<Info>>();
    }
    
    //limiting arraylist size
    public void addRequest(byte[] nodeIdSrc, byte[] nodeIdDst, int hop_count) {
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            ArrayList<Info> tmpArray = this.hmap.get(nodeIdSrc);
            
            if(tmpArray.size() >= config.getReqArraySize()) return;
            else {
                tmpArray.add(new Info(nodeIdDst, hop_count));
                this.hmap.put(nodeIdSrc, tmpArray);
            }
            
        }else{
            if(this.hmap.size() < config.getRequestCacheSize()) {
                ArrayList<Info> tmpArray = new ArrayList<>();
                Info info = new Info(nodeIdDst, hop_count);
                
                tmpArray.add(info);
                
                this.hmap.put(nodeIdSrc, tmpArray);
                return;
            }
        }
    }
    
    public Object removeRequest(byte[] nodeId) {
        return this.hmap.remove(nodeId);
    }
    
    public void gcReq() {
        long now  = System.currentTimeMillis();
        
        this.hmap.forEach((k, v) -> {
            for(Info i : v) {
                if(now - i.getTimeStamp()>config.getReqTimeDelta()) removeRequest(k);
            }
        });
    }
}
