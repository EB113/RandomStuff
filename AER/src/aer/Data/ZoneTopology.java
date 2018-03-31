/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Topologia Local com tamanho N 

import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import static aer.miscelaneous.Crypto.hexStringToByteArray;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ZoneTopology {

    
    //Value Class
    class Info {
        byte[]          hop_id;
        InetAddress    hop_addr6;
        float           rank;
        int             hop_dist;
        byte[]          seq_num;
        long            timestamp;
        
        Info(InetAddress addr6, float rank, int hop_dist, byte[] seq_num) {
            this.hop_addr6  = addr6;
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

        public byte[] getHop_id() {
            return hop_id;
        }
               
    }
    
    HashMap <byte[], HashMap<byte[], Info>> hmap;
    Config config;
    
    public ZoneTopology(Config config) {
       this.config    = config;
       this.hmap      = new HashMap<byte[], HashMap<byte[], Info>>();
    }
    
    //RANKRANKRANK??? Muita optimizacao por fazer nas pesquisas e insercoes
    public void addPeerZone(byte[] nodeId, InetAddress addr6, byte[] seq_num, ArrayList<Tuple> peers) {
        long now  = System.currentTimeMillis();
        byte[] peerId   = null;
        int    peerDist = 0;
        
        
        for(Tuple peer: peers) {
            peerId      = (byte[])peer.y;
            peerDist    = (int)peer.x;
            Info info = new Info(addr6, now, peerDist, seq_num);
            
            if(this.hmap.containsKey(peerId)) {//Se ja tem o peer na tabela
                HashMap<byte[], Info> tmp1 = this.hmap.get(peerId);
                
                if(tmp1.containsKey(nodeId)){//se ja tem o hop verificar distancias... OBS:problema relativo a TimeStamp podemos estar a nao inserir algo mais recente
                    if(tmp1.get(nodeId).hop_dist > peerDist) {
                        tmp1.put(nodeId, info);
                    }
                }else if (tmp1.size() < config.getZoneMapSize()){//se nao tem o hop e caso exista espaco adicionar
                    tmp1.put(nodeId, info);
                    this.hmap.put(peerId, tmp1);
                }else {//se nao tem o hop e nao ha espaco retirar hop com dist mais longa
                    byte[]  curNodeId   = null;
                    int     maxDist     = 0;
                    for (Map.Entry<byte[], Info> entry : tmp1.entrySet()){
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
                HashMap<byte[], Info> tmp2 = new HashMap<>();
                tmp2.put(nodeId, info);
                this.hmap.put(peerId, tmp2);
            }
        }
    }
    /*
    public Object addPeer(byte[] nodeId, Inet6Address addr6, int hop_dist, byte[] seq_num) {
        return this.hmap.put(nodeId, new Info(addr6, 0, hop_dist, seq_num));
    }*/
    
    public void removePeer(byte[] nodeId) {
        this.hmap.remove(nodeId);
    }
    
    public void removePeerLink(byte[] nodeIdDst, byte[] nodeIdHop) {
        HashMap<byte[], Info> tmp = this.hmap.get(nodeIdDst);
        tmp.remove(nodeIdHop);
        this.hmap.put(nodeIdDst, tmp);
    }
    
    public void gcPeer() {
        long now  = System.currentTimeMillis();
        
        this.hmap.forEach((k1, v1) -> {
            v1.forEach((k2, v2) -> {
                if(now - v2.getTimeStamp() > config.getZoneTimeDelta()) removePeerLink(k1,k2);
            });
        });
    }
    
    public ArrayList<Tuple> getPeer(int maxHops) {
        ArrayList<Tuple> peers = new ArrayList<>();
        
        this.hmap.forEach((k1, v1) -> {
            v1.forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(k1, v2.hop_dist);
                }
            });
        });
        
        return peers;
    }
}
