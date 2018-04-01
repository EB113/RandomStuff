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
import java.util.LinkedList;
import java.util.Map;

public class ZoneTopology {
    
    //Value Class
    class Info {
        InetAddress     hop_addr;
        float           rank;
        int             hop_dist;
        byte[]          seq_num;
        long            timestamp;
        
        Info(InetAddress addr, float rank, int hop_dist, byte[] seq_num) {
            this.hop_addr   = addr;
            this.rank       = rank;
            this.hop_dist   = hop_dist;
            this.seq_num    = seq_num;
            this.timestamp  = System.currentTimeMillis();
        }
        
        public byte[] getSeqNum() {
            return this.seq_num;
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
    
    HashMap <ByteArray, HashMap<ByteArray, Info>> hmap;
    Config config;
    
    public ZoneTopology(Config config) {
       this.config    = config;
       this.hmap      = new HashMap<ByteArray, HashMap<ByteArray, Info>>();
    }
    
    //RANKRANKRANK??? Muita optimizacao por fazer nas pesquisas e insercoes
    public void addPeerZone(byte[] nodeId_old, InetAddress addr6, byte[] seq_num, ArrayList<Tuple> peers) {
        long        now         = System.currentTimeMillis();
        ByteArray   peerId      = null;
        int         peerDist    = 0;
        Info        info        = null;
        
        ByteArray   nodeId      = new ByteArray(nodeId_old);
        
        //ADD HELLO OWNER
        if(!this.hmap.containsKey(nodeId)){
            
            HashMap<ByteArray, Info> tmp2 = new HashMap<>();
            info = new Info(addr6, 0, 1, seq_num);
            
            tmp2.put(nodeId, info);
            this.hmap.put(nodeId, tmp2);
        }
        
        //ADD PEERS
        for(Tuple peer: peers) {
            peerId      = new ByteArray((byte[])peer.y);
            peerDist    = (int)peer.x;
            info = new Info(addr6, 0, peerDist, seq_num);
            
            if(this.hmap.containsKey(peerId)) {//Se ja tem o peer na tabela
                HashMap<ByteArray, Info> tmp1 = this.hmap.get(peerId);
                
                if(tmp1.containsKey(nodeId)){//se ja tem o hop verificar distancias... OBS:problema relativo a TimeStamp podemos estar a nao inserir algo mais recente
                    if(tmp1.get(nodeId).hop_dist > peerDist) {
                        tmp1.put(nodeId, info);
                    }
                }else if (tmp1.size() < config.getZoneMapSize()){//se nao tem o hop e caso exista espaco adicionar
                    tmp1.put(nodeId, info);
                    this.hmap.put(peerId, tmp1);
                }else {//se nao tem o hop e nao ha espaco retirar hop com dist mais longa
                    ByteArray   curNodeId       = null;
                    int         maxDist         = 0;
                    for (Map.Entry<ByteArray, Info> entry : tmp1.entrySet()){
                        if(entry.getValue().hop_dist > maxDist) {
                            curNodeId   = entry.getKey();
                            maxDist     = entry.getValue().hop_dist;
                        }
                    }
                    if(maxDist >= peerDist && curNodeId != null) {
                        tmp1.remove(curNodeId);
                        tmp1.put(nodeId, info);
                        this.hmap.put(peerId, tmp1);
                    }
                }
            }else {//Se nao tem o peer na tabela
                HashMap<ByteArray, Info> tmp2 = new HashMap<>();
                info = new Info(addr6, 0, peerDist, seq_num);

                tmp2.put(nodeId, info);
                this.hmap.put(peerId, tmp2);
            }
        }
    }
    
    public void removePeer(ByteArray nodeId) {
        this.hmap.remove(nodeId);
    }
    
    public void removePeerLink(ByteArray nodeIdDst, ByteArray nodeIdHop) {
        HashMap<ByteArray, Info> tmp = this.hmap.get(nodeIdDst);
        tmp.remove(nodeIdHop);
        this.hmap.put(nodeIdDst, tmp);
    }
    
    public void gcPeer() {
        long now  = System.currentTimeMillis();
        
        this.hmap.forEach((k1, v1) -> {
            if(v1.size() > 0 ){
                v1.forEach((k2, v2) -> {
                    if(now - v2.getTimeStamp() > config.getZoneTimeDelta()) removePeerLink(k1,k2);
                });
            }else {
                removePeer(k1);
            }
        });
    }
    
    public LinkedList<Tuple> getPeers(int maxHops) {
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.hmap.forEach((k1, v1) -> {
            v1.forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(k1.getData(), v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    public Tuple getPeer(byte[] nodeId) {
        Tuple tuple = null;
        
        if(this.hmap.containsKey(nodeId)){
            InetAddress peer = null;
            int minDist = config.getZoneSize(); //MAX DIST is BORDER
            HashMap<ByteArray, Info> routes = this.hmap.get(nodeId);
            
            for(Map.Entry<ByteArray, Info> pair : routes.entrySet()) {
                if(pair.getValue().getHop_dist() < minDist) {
                    peer    = pair.getValue().getHop_addr();
                    minDist = pair.getValue().getHop_dist();
                }
            }
            tuple = new Tuple(peer, minDist);
        }
        
        return tuple;
    }

    public byte[] getNodeId(InetAddress nodeHopAddr) {
        byte[] peerId = null;
        
        for(Map.Entry<ByteArray, HashMap<ByteArray, Info>> pair1 : this.hmap.entrySet()) {
            for(Map.Entry<ByteArray, Info> pair2 : pair1.getValue().entrySet()) {
                if(pair2.getValue().hop_addr.equals(nodeHopAddr)) {
                    return pair2.getKey().getData();
                }
            }
        }
        
        return peerId;
    }

    //NESTE MOMENTO ESTA PARA TODOS
    LinkedList<InetAddress> getReqPeers() {
        LinkedList<InetAddress> ip_list = null;
        LinkedList<Tuple> tuple_list = getPeers(1);
        
        if(tuple_list.size() > 0) {
            ip_list = new LinkedList<>();
            
            for(Tuple tup : tuple_list) {
                ip_list.push((InetAddress)tup.x);
            }
        }
        
        return ip_list;
    }

    //INETADDRESS PARA PREVENIR IDS REPETIDOS
    void rmRoute(byte[] nodeIdSrc_old, byte[] nodeIdDst_old, InetAddress hopAddr) {
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray nodeIdDst = new ByteArray(nodeIdDst_old);
        
        if(this.hmap.containsKey(nodeIdDst)){
            HashMap<ByteArray, Info> tmpArray = this.hmap.get(nodeIdDst);
            if(tmpArray.containsKey(nodeIdSrc) && tmpArray.get(nodeIdSrc).hop_addr.equals(hopAddr)) {
                tmpArray.remove(nodeIdSrc);
            }
            this.hmap.put(nodeIdDst, tmpArray);
        }
    }

}
