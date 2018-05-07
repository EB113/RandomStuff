/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Topologia Local com tamanho N 

import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import static aer.miscelaneous.Crypto.hexStringToByteArray;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ZoneTopology {

    
    //Value Class
    class Info {
        
        //Identity
        InetAddress     hop_addr;
        int             timeRank;
        int             hop_dist;
        long            timestamp;
        
        //Position
        Tuple position;
        Tuple direction;
        Double speed;
        
        //Session
        int helloCounter;
        
        
        Info(InetAddress addr, int hop_dist, Tuple position, Tuple direction, Double speed, int counter) {
            
            // Identity
            this.hop_addr   = addr;
            this.timeRank   = 0;
            this.hop_dist   = hop_dist;
            this.timestamp  = System.currentTimeMillis();
            
            //Position
            this.position   = position;
            this.direction  = direction;
            this.speed      = speed;
            
            //Session
            this.helloCounter = counter;
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }

        public int getHop_dist() {
            return hop_dist;
        }   

        public InetAddress getHop_addr() {
            return hop_addr;
        }
        
    }
    
    //Congestion Parameters
    int         vehicles;
    double      reqTime;
    
    //Data Struct   
    HashMap <ByteArray, Info>   gem;
    PeerCache                   peerCache;
    
    //Configuration
    Config config;
    
    public ZoneTopology(Config config, PeerCache peerCache) {
        
       this.config    = config;
       
       this.gem      = new HashMap<ByteArray, Info>();
       this.peerCache = peerCache;
       vehicles = 0;
       reqTime  = 0.0;
    }
    
    public void addPeerZone(byte[] myid, byte[] nodeId_old, InetAddress addr6, Tuple position, Tuple direction, Double speed) {
        
        Info        info        = null;
        ByteArray   nodeId      = new ByteArray(nodeId_old);
        
        //ADD HELLO OWNER
        if(!(this.gem.containsKey(nodeId)) && this.gem.size() < config.getZoneCacheSize()){
            
            info = new Info(addr6, 1, position, direction, speed, 1);
            
            this.gem.put(nodeId, info);
            this.peerCache.rmPeer(nodeId);
            
        } else if(this.gem.containsKey(nodeId)) {
            
            info = this.gem.get(nodeId);
            
            info = new Info(addr6, 1, position, direction, speed, ++info.helloCounter);
            this.gem.put(nodeId, info);
        }
        
        return;
    }
    
    /*
    public void removePeer(ByteArray nodeId) {
        this.gem.remove(nodeId);
    }*/
    
    public void gcPeer() {
        
        long now  = System.currentTimeMillis();
        
        Iterator<Map.Entry<ByteArray, Info>> iter = this.gem.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<ByteArray, Info> entry = iter.next();
            Info info = entry.getValue();
            
            if((now - info.getTimeStamp() > config.getZoneTimeDelta())) {

                this.peerCache.addPeer(entry.getKey(), info);
                iter.remove();
            }
        }
        
    }
    
    ArrayList<byte[]> printPeers() {
        ArrayList<byte[]> out = new ArrayList<>();
        
        for(ByteArray peer : this.gem.keySet()) {
            System.out.print("|" + Crypto.toHex(peer.getData()) + "|");
        }
        
        System.out.println("");
        return out;
    }
    
    int getCount() {
        return this.vehicles;
    }
    
    
    LinkedList<InetAddress> getPeer(ByteArray nodeIdDst_new) {
        
        LinkedList<InetAddress> out = null;
                
        if(this.gem.containsKey(nodeIdDst_new)){
            out = new LinkedList<>();
            out.add(this.gem.get(nodeIdDst_new).hop_addr);
        }
        
        return out;
    }

    LinkedList<InetAddress> getOptimal(Tuple tuple) {
        
        LinkedList<InetAddress> out = null;
        
        /*
        ArrayList<Double> peerInfo = ((ArrayList<Double>) tuple.x);
        long timestamp             = ((long) tuple.y);
        
        Double positionX    = peerInfo.get(0);
        Double positionY    = peerInfo.get(1);
        Double directionX   = peerInfo.get(2);
        Double directionY   = peerInfo.get(3);
        Double speed        = peerInfo.get(4);
        
        Double newPosX
        
        
        for(Info info: this.gem.values()) {
            
        }*/
        
        return null;
    }

    LinkedList<InetAddress> getOrientUnic(LinkedList<InetAddress> usedPeers) {
        
        LinkedList<InetAddress> out = new LinkedList<>();
        
        for(Info info : this.gem.values()){
            
        }
        
        if(out.size() == 0) return null;
        else return out;
    }
    /*
    //ROUTES COM RANk
    //RETURN TUPLE <ADDR,DIST>
    public LinkedList<Tuple> getRankRoutes(int maxHops) {
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.gem.forEach((k1, v1) -> {
            ((HashMap<ByteArray, Info>)v1.y).forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(v2.hop_addr, v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    //ROUTES SEM RANK
    //RETURN TUPLE <ADDR,DIST>
    public LinkedList<Tuple> getRoutes(int maxHops) {
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.gem.forEach((k1, v1) -> {
            ((HashMap<ByteArray, Info>)v1.y).forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(v2.hop_addr, v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    //REUTRN TUPLE <BYTE[], DIST>
    public LinkedList<Tuple> getPeers(int maxHops) {
        
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.gem.forEach((k1, v1) -> {
            ((HashMap<ByteArray, Info>)v1.y).forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(k1.getData(), v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    public Tuple getPeer(ByteArray nodeId) {
        Tuple tuple = null;
        
        if(this.gem.containsKey(nodeId)){
            InetAddress peer = null;
            int minDist = config.getZoneSize(); //MAX DIST is BORDER
            //HashMap<ByteArray, Info> routes = this.gem.get(nodeId);
            Tuple routes = this.gem.get(nodeId);
            
            for(Map.Entry<ByteArray, Info> pair : ((HashMap<ByteArray, Info>)(routes.y)).entrySet()) {
                if(pair.getValue().getHop_dist() <= minDist) {
                    peer    = pair.getValue().getHop_addr();
                    minDist = pair.getValue().getHop_dist();
                }
            }
            if(peer != null)    tuple = new Tuple(minDist, peer);
        }
        
        return tuple;
    }

    public byte[] getNodeId(InetAddress nodeHopAddr) {
        byte[] peerId = null;
        
        //for(Map.Entry<ByteArray, HashMap<ByteArray, Info>> pair1 : this.gem.entrySet()) {
        for(Map.Entry<ByteArray, Tuple> pair1 : this.gem.entrySet()) {
            
            for(Map.Entry<ByteArray, Info> pair2 : ((HashMap<ByteArray, Info>)(pair1.getValue().y)).entrySet()) {
                
                if(pair2.getValue().hop_addr.equals(nodeHopAddr)) {
                    return pair2.getKey().getData();
                }
            }
        }
        
        return peerId;
    }

    //NESTE MOMENTO ESTA PARA TODOS
    LinkedList<InetAddress> getReqRankPeers(InetAddress hopVAI, InetAddress hopVEM) {
        LinkedList<InetAddress> ip_list = null;
        LinkedList<Tuple> tuple_list = getRoutes(1);
        
        if(hopVAI != null && hopVEM != null){
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    if(!hopVAI.equals((InetAddress)tup.x) && !hopVEM.equals((InetAddress)tup.x)) ip_list.push((InetAddress)tup.x);
                }
            }
        }else if(hopVAI != null) {
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    if(!hopVAI.equals((InetAddress)tup.x)) ip_list.push((InetAddress)tup.x);
                }
            }
        }else {
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    ip_list.push((InetAddress)tup.x);
                }
            }
        }
        return ip_list;
    }
    
    
    
    //NESTE MOMENTO ESTA PARA TODOS
    LinkedList<InetAddress> getReqPeers(InetAddress hopAddr) {
        LinkedList<InetAddress> ip_list = null;
        LinkedList<Tuple> tuple_list = getRoutes(1);
        
        if(hopAddr != null){
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    if(!hopAddr.equals((InetAddress)tup.x)) ip_list.push((InetAddress)tup.x);
                }
            }
        }else {
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    ip_list.push((InetAddress)tup.x);
                }
            }
        }
        return ip_list;
    }
*/
    
    
}
