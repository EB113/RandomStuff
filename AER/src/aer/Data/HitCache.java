/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request que obteram resposta com successo com origem no host ou outsourced.

import java.net.Inet6Address;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class HitCache {
    
   //Value Class
    class Info {
        Inet6Address    addr6;
        float           rank;
        int             hop_dist;
        byte[]          seq_num;
        
        Info(Inet6Address addr6, float rank, int hop_dist, byte[] seq_num) {
            this.addr6      = addr6;
            this.rank       = rank;
            this.hop_dist   = hop_dist;
            this.seq_num    = seq_num;
        }
        
        public byte[] getSeqNum() {
            return this.seq_num;
        }
    }
    HashMap <byte[], Info> hmap;
    int zoneSize;
    
    public HitCache(int zoneSize) {
       this.zoneSize    = zoneSize;
       this.hmap        = new HashMap<byte[], Info>();
    }
    
    public Object addPeer(byte[] nodeId, Inet6Address addr6, float rank, int hop_dist, byte[] seq_num) {
        return this.hmap.put(nodeId, new Info(addr6, rank, hop_dist, seq_num));
    }
    
    public Object removePeer(byte[] nodeId) {
        return this.hmap.remove(nodeId);
    }
    
    public boolean compare_seq(byte[] nodeId, byte[] seq) {
        //Validar Input etc
        int new_seq = ByteBuffer.wrap(seq).getInt();
        int cur_seq = ByteBuffer.wrap(this.hmap.get(nodeId).getSeqNum()).getInt();
        
        if(new_seq>cur_seq) return true;
        else return false;
    }
    
}
