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
        
        PeerInfo(Tuple position, Tuple direction, Double speed) {
            
            // Identity
            this.timestamp  = System.currentTimeMillis();
            
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
    LinkedHashMap<ByteArray, PeerInfo> gem;
    
    //Config
    Config config;

    PeerCache(Config config) {
        
        this.gem    = new LinkedHashMap<ByteArray, PeerInfo>(config.getPeerCacheSize()+1);
        this.config = config;
        this.count  = 0;
    }
    
    void addPeer(ByteArray nodeID, Info info) {
    
        PeerInfo peerInfo = new PeerInfo(info.position, info.direction, info.speed);
        
        if(this.gem.containsKey(nodeID)) {
        
            this.gem.put(nodeID, peerInfo);
        }else {
            
            if(this.count < this.config.getPeerCacheSize()) {

                this.gem.put(nodeID, peerInfo);
            }else{

                Iterator<Map.Entry<ByteArray, PeerInfo>> iter = this.gem.entrySet().iterator();
                iter.next(); 

                this.gem.put(nodeID, peerInfo);
                iter.remove();
            }
        }
    }
    
    PeerInfo getPeer(ByteArray nodeID, Info info) {
        PeerInfo out = null;
        
        out = this.gem.get(nodeID);
        
        return out;
    }
    
    void gcPeer() {
    
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<ByteArray, PeerInfo>> iter = this.gem.entrySet().iterator();
        
        while (iter.hasNext()) {
            
            Map.Entry<ByteArray, PeerInfo> entry = iter.next();
            
            PeerInfo info = entry.getValue();
            
            if(now - info.timestamp > config.getPeerCacheTimeDelta()){
                iter.remove();
            }
        }
    }
    
    void rmPeer(ByteArray nodeID) {
        
        if(this.gem.containsKey(nodeID)) {
        
            this.gem.remove(nodeID);
        }
    }
    

    Tuple getPeer(ByteArray nodeIdDst_new) {
        
        Tuple tuple = null;
        ArrayList<Double> out = null;
        
        if(this.gem.containsKey(nodeIdDst_new)) {
        
            PeerInfo info = this.gem.get(nodeIdDst_new);
            
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
