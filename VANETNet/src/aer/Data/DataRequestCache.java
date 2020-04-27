/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

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

    boolean addReq(byte[] src_old, byte[] dst_old, byte[] req_num_old, long ttl, LinkedList<InetAddress> usedPeers, byte[] raw) {
        
        ByteArray src       = new ByteArray(src_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.gem.containsKey(src)) {//SE JA TEM
            
            HashMap<ByteArray, Info> tmpMap = this.gem.get(src);
            
            if(tmpMap.containsKey(req_num)){
                
                Info info = tmpMap.get(req_num);
                
                if(usedPeers != null)
                    for(InetAddress addr : usedPeers) info.usedPeers.push(addr);
                
                return true;
            }else if(tmpMap.size() < config.getReqMapSize()) {
                
                tmpMap.put(req_num, new Info(usedPeers, dst_old, ttl, raw));
                this.gem.put(src, tmpMap);
                
                return false;
            }else{
                
                return true;
            }
        }else{
            if(this.gem.size() < config.getDataCacheSize()) {//SE TEM ESPACO
                
                HashMap<ByteArray, Info> tmpMap = new HashMap<>();
                Info info = new Info(usedPeers, dst_old, ttl, raw);
                
                tmpMap.put(req_num, info);
                this.gem.put(src, tmpMap);
                
            }else {
                
                //se nao tem tamanho devolver error sem tamanho IMPORTANTE MAIS UMA FLAG NO ERRO
            }
            
            return false;
        }
    }

    ArrayList<byte[]> getReq(byte[] nodeID, boolean mode) {
        
        Info info               = null;
        ArrayList<byte[]> out   = new ArrayList<>();
        
        Iterator<Map.Entry<ByteArray, HashMap<ByteArray, Info>>> iter1 = this.gem.entrySet().iterator();
        
        while (iter1.hasNext()) {
            Map.Entry<ByteArray, HashMap<ByteArray, Info>> entry1 = iter1.next();
            
            if(entry1.getValue().size() > 0) {
                Iterator<Map.Entry<ByteArray, Info>> iter2 = entry1.getValue().entrySet().iterator();
                
                while (iter2.hasNext()) {
                    Map.Entry<ByteArray, Info> entry2 = iter2.next();
                    info = entry2.getValue();
                    
                    if(Crypto.cmpByteArray(info.nodeId_dst, nodeID)){
                        out.add(info.raw);
                        if(mode) iter2.remove();
                    }
                    
                }
                
            } else iter1.remove();
            
        }
                
        if(out.size() == 0) return null;        
        return out;
    }

    void gcData() {
        long now  = System.currentTimeMillis();
        
        Iterator<Map.Entry<ByteArray, HashMap<ByteArray, Info>>> iter1 = this.gem.entrySet().iterator();
        
        while (iter1.hasNext()) {
            Map.Entry<ByteArray, HashMap<ByteArray, Info>> entry1 = iter1.next();
            
            if(entry1.getValue().size() > 0) {
                
                entry1.getValue().entrySet().removeIf(entry2 -> (now - entry2.getValue().getTimeStamp() > config.getDataReqTimeDelta()));
                
            } else iter1.remove();
        }
    }
    
    Boolean exists(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {

        ByteArray src = new ByteArray(nodeIdSrc);
        ByteArray req = new ByteArray(req_num);
        
        if(this.gem.containsKey(src)){
        
            HashMap<ByteArray, Info> tmpMap = this.gem.get(src);
            
            if(tmpMap.containsKey(req)){
                
                Info info = tmpMap.get(req);
                    
                    if(Crypto.cmpByteArray(nodeIdDst, info.nodeId_dst)) return true;
            }
            
        }
        
        return false;
    }

    void rmReqwithRep(ByteArray src, ByteArray req, byte[] origin){
    
        Iterator<Map.Entry<ByteArray, HashMap<ByteArray, Info>>> iter1 = this.gem.entrySet().iterator();
        
        if(this.gem.containsKey(src)){
        
            HashMap<ByteArray, Info> tmpMap = this.gem.get(src);
            
            if(tmpMap.containsKey(req)){
                
                Info info = tmpMap.get(req);
                    
                    if(Crypto.cmpByteArray(origin, info.nodeId_dst)) tmpMap.remove(req);
            }
            
        }
        
    }
}

    
