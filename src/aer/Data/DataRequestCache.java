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
public class DataRequestCache {

    //Value Class
    class Info {
        
        byte[]                  raw;
        byte[]                  nodeId_dst;
        
        LinkedList<InetAddress> usedPeers;
        
        long                    timestamp;
        
        Info(LinkedList<InetAddress> usedPeers, byte[] nodeId_dst, long timestamp, byte[] raw) {
            
            this.raw                = raw;
            this.nodeId_dst         = nodeId_dst;
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
    
    
    DataRequestCache(Config config) {
        this.config     = config;
        this.gem        = new HashMap<ByteArray, HashMap<ByteArray, Info>>();
    }

    void addReq(byte[] src_old, byte[] dst_old, byte[] req_num_old, long ttl, LinkedList<InetAddress> usedPeers, byte[] raw) {
        
        ByteArray src       = new ByteArray(src_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.gem.containsKey(src)) {//SE JA TEM
            
            HashMap<ByteArray, Info> tmpMap = this.gem.get(src);
            
            if(tmpMap.containsKey(req_num)){
                
                Info info = tmpMap.get(req_num);
                
                if(usedPeers != null)
                    for(InetAddress addr : usedPeers) info.usedPeers.push(addr);
            }else if(tmpMap.size() < config.getReqMapSize()) {
                
                tmpMap.put(req_num, new Info(usedPeers, dst_old, ttl, raw));
                this.gem.put(src, tmpMap);
            }else{
                
                return;
            }
        }else{
            if(this.gem.size() < config.getDataCacheSize()) {//SE TEM ESPACO
                
                HashMap<ByteArray, Info> tmpMap = new HashMap<>();
                Info info = new Info(usedPeers, dst_old, ttl, raw);
                
                tmpMap.put(req_num, info);
                this.gem.put(src, tmpMap);
                
            }else {
                
                //se nao tem tamanho devolver error sem tamanho IMPORTANTE MAIS UMA FLAG NO ERRO
                return;
            }
        }
        
        return;
        
    }
    
}
