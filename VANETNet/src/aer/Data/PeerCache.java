/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

import aer.Data.ZoneTopology.Info;
import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author pedro
 */
class PeerCache {
    
    class PeerInfo {
        
        //Identity
        long   timestamp;
        
        //Position
        Tuple  position;
        Tuple  direction;
        Double speed;
        
        PeerInfo(Tuple position, Tuple direction, Double speed, Long timestamp) {
            
            // Identity
            if(timestamp == null) this.timestamp  = System.currentTimeMillis();
            else this.timestamp = timestamp;
                
            //Position
            this.position   = position;
            this.direction  = direction;
            this.speed      = speed;
        }
        
    }
    
    //Manage
    private int count;
    
    //Data
    //private 
    LinkedHashMap<ByteArray, PeerInfo> gemZone;
    LinkedHashMap<ByteArray, PeerInfo> gemCache;
    
    //Config
    Config config;

    PeerCache(Config config) {
        
        this.gemZone    = new LinkedHashMap<ByteArray, PeerInfo>(config.getPeerCacheSize()+1);
        this.gemCache   = new LinkedHashMap<ByteArray, PeerInfo>(config.getPeerCacheSize()+1);
        this.config     = config;
        this.count      = 0;
    }
    
    void addZonePeer(ByteArray nodeID, Info info) {
    
        PeerInfo peerInfo = new PeerInfo(info.position, info.direction, info.speed, null);
        
        if(this.gemZone.containsKey(nodeID)) {
            
            PeerInfo peerInfoAux = this.gemZone.get(nodeID);
            if(peerInfoAux.timestamp < peerInfo.timestamp) this.gemZone.put(nodeID, peerInfo);
        }else {
            
            if(this.count < this.config.getPeerCacheSize()) {

                this.gemZone.put(nodeID, peerInfo);
            }else{//MUDAR ISTO PARA ELIMINAR ENTRADA MAIS ANTIGA

                Iterator<Map.Entry<ByteArray, PeerInfo>> iter = this.gemZone.entrySet().iterator();
                iter.next(); 

                this.gemZone.put(nodeID, peerInfo);
                iter.remove();
            }
        }
    }
    
    void addCachePeer(ByteArray nodeID, Tuple tuple) {
    
        ArrayList<Double> tmp = ((ArrayList<Double>)tuple.x);
        long timestamp = ((long)tuple.y);
        
        Tuple   position  = new Tuple(tmp.get(0), tmp.get(1));
        Tuple   direction = new Tuple(tmp.get(2), tmp.get(3));
        Double  speed     = tmp.get(4);
        
        PeerInfo peerInfo = new PeerInfo(position, direction, speed, timestamp);
        
        if(this.gemCache.containsKey(nodeID)) {
        
            PeerInfo peerInfoAux = this.gemCache.get(nodeID);
            if(peerInfoAux.timestamp < peerInfo.timestamp) this.gemCache.put(nodeID, peerInfo);
        }else {
            
            if(this.count < this.config.getPeerCacheSize()) {

                this.gemCache.put(nodeID, peerInfo);
            }else{//MUDAR ISTO PARA ELIMINAR ENTRADA MAIS ANTIGA

                Iterator<Map.Entry<ByteArray, PeerInfo>> iter = this.gemCache.entrySet().iterator();
                iter.next(); 

                this.gemCache.put(nodeID, peerInfo);
                iter.remove();
            }
        }
    }
    
    
    PeerInfo getPeer(ByteArray nodeID, Info info) {
        PeerInfo out1 = null;
        PeerInfo out2 = null;
        
        out1 = this.gemZone.get(nodeID);
        out2 = this.gemCache.get(nodeID);
        
        if(out1.timestamp > out2.timestamp) return out1;
        else return out2;
    }
    
    void gcPeer() {
    
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<ByteArray, PeerInfo>> iter = this.gemZone.entrySet().iterator();
        
        while (iter.hasNext()) {
            
            Map.Entry<ByteArray, PeerInfo> entry = iter.next();
            
            PeerInfo info = entry.getValue();
            
            if(now - info.timestamp > config.getPeerCacheTimeDelta()){
                iter.remove();
            }
        }
        
        iter = this.gemCache.entrySet().iterator();
        
        while (iter.hasNext()) {
            
            Map.Entry<ByteArray, PeerInfo> entry = iter.next();
            
            PeerInfo info = entry.getValue();
            
            if(now - info.timestamp > config.getPeerCacheTimeDelta()){
                iter.remove();
            }
        }
    }
    
    void rmZonePeer(ByteArray nodeID) {
        
        if(this.gemZone.containsKey(nodeID)) {
        
            this.gemZone.remove(nodeID);
        }
    }
    

    Tuple getPeer(ByteArray nodeIdDst_new) {
        
        Tuple tuple = null;
        ArrayList<Double> out = null;
        
        PeerInfo info  = null;
        PeerInfo info1 = null;
        PeerInfo info2 = null;
        
        if(this.gemZone.containsKey(nodeIdDst_new)) 
            info1 = this.gemZone.get(nodeIdDst_new);
        
        if(this.gemCache.containsKey(nodeIdDst_new)) 
            info2 = this.gemCache.get(nodeIdDst_new);
        
        if(info1 != null && info2 != null){
            if(info1.timestamp > info2.timestamp)
                info = info1;
            else info = info2;
        }else if(info1 != null){
            info = info1;
        }else if(info2 != null){
            info = info2;
        }
        
        if(info != null){
            
            out = new ArrayList<>(4);

            out.add(((Double)info.position.x));
            out.add(((Double)info.position.y));
            out.add(((Double)info.direction.x));
            out.add(((Double)info.direction.y));
            out.add(((Double)info.speed));

            tuple = new Tuple(out, info.timestamp);
        }
        
        return tuple;
    }
}
