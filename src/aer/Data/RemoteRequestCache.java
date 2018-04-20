/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Request Activos.

import aer.PDU.RReq;
import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;



public class RemoteRequestCache {
    
    //Value Class
    class Info {
        byte[]                  nodeId_dst;
        InetAddress             nodeAddr_hop;//USADO PARA RETORNAR 
        byte[]                  peerKey;
        
        int                     advisedHop; //SE ERRO E CONHECER HOP QUAL A DIST?
        byte                    errNo; //USADO PARA COLOCAR ERRO PREDOMINANTE QUE E O 0x01
        LinkedList<InetAddress> usedPeers;
        LinkedList<InetAddress> responsivePeers;
        
        long                    timestamp;
        int                     hop_count;
        int                     hop_max;
        
        Info(LinkedList<InetAddress> usedPeers, InetAddress nodeId_hop, byte[] nodeId_dst, int hop_count, int hop_max, byte[] peerKey) {
            this.nodeId_dst         = nodeId_dst;
            this.nodeAddr_hop       = nodeId_hop;
            this.usedPeers          = usedPeers;
            this.responsivePeers    = new LinkedList();
            this.timestamp          = System.currentTimeMillis();
            this.hop_count          = hop_count;
            this.hop_max            = hop_max;//QUANDO SE REPETE O PEDIDO POR HOP LIMIT MAS CONHECE?
            this.peerKey            = peerKey;
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    //NODEID OF NODE THAT REQUESTED X INFO LIST
    HashMap <ByteArray, HashMap<ByteArray, Info>> hmap;
    Config config;
    
    public RemoteRequestCache(Config config) {
       this.config  = config;
       this.hmap    = new HashMap<ByteArray, HashMap<ByteArray, Info>>();
    }
    
    
    // falta adicionar aos used peers
    //limiting arraylist size
    public void addRequest(LinkedList<InetAddress> usedPeers, InetAddress peer_hop, byte[] nodeIdSrc_old, byte[] nodeIdDst, byte[] req_num_old, int hop_count, int hop_max, byte[] peerKey) {
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {//SE JA TEM
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);
            
            if(tmpMap.containsKey(req_num)){
                
                Info info = tmpMap.get(req_num);
                
                for(InetAddress addr : usedPeers) info.usedPeers.push(addr);
            }else if(tmpMap.size() < config.getReqMapSize()) {
                
                tmpMap.put(req_num, new Info(usedPeers, peer_hop, nodeIdDst, hop_count, hop_max, peerKey));
                this.hmap.put(nodeIdSrc, tmpMap);
            }else{
                
                return;
            }
        }else{
            if(this.hmap.size() < config.getRequestCacheSize()) {//SE TEM ESPACO
                
                HashMap<ByteArray, Info> tmpMap = new HashMap<>();
                Info info = new Info(usedPeers, peer_hop, nodeIdDst, hop_count, hop_max, peerKey);
                
                tmpMap.put(req_num, info);
                this.hmap.put(nodeIdSrc, tmpMap);
                
            }else {
                
                //se nao tem tamanho devolver error sem tamanho IMPORTANTE MAIS UMA FLAG NO ERRO
                return;
            }
        }
        
        return;
    }
    
    public Object removeRequest(ByteArray nodeId) {
        return this.hmap.remove(nodeId);
    }
    
    public void gcReq() {
        long now  = System.currentTimeMillis();
        
        Iterator<Map.Entry<ByteArray, HashMap<ByteArray, Info>>> iter1 = this.hmap.entrySet().iterator();
        
        while (iter1.hasNext()) {
            Map.Entry<ByteArray, HashMap<ByteArray, Info>>entry1 = iter1.next();
            
            if(entry1.getValue().size() > 0) {
                
                entry1.getValue().entrySet().removeIf(entry2 -> (now - entry2.getValue().getTimeStamp() > config.getRemoteReqTimeDelta()));
                
            } else iter1.remove();
            
        }
        
        
        //ConcurrentModificationException
        /*
        
        // HashMap <ByteArray, LinkedList<Info>> hmap;
        for(Map.Entry<ByteArray, LinkedList<Info>> pair1 : this.hmap.entrySet()) {
            for(Info i : pair1.getValue()) {
                if(now - i.getTimeStamp()>config.getReqTimeDelta()) removeRequest(pair1.getKey());
            }
        }
        
        this.hmap.forEach((k, v) -> {
            for(Info i : v) {
                if(now - i.getTimeStamp()>config.getReqTimeDelta()) removeRequest(k);
            }
        });*/
    }

    InetAddress rmReq(byte[] nodeIdDst, byte[] nodeIdSrc_old, byte[] req_num_old) {
        
        InetAddress hopAddr = null;
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);

            if(tmpMap.containsKey(req_num)) {
                
                Info info = tmpMap.get(req_num);
                
                hopAddr = info.nodeAddr_hop;
                tmpMap.remove(req_num);
            }
        }
        
        return hopAddr;
    }

    byte[] getReqTarget(byte[] nodeIdSrc_old, byte[] nodeIdDst, byte[] req_num_old) {
        
        byte[] nodeId = null;
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);

            if(tmpMap.containsKey(req_num)) {
                
                Info info = tmpMap.get(req_num);
                
                nodeId = info.nodeId_dst;
            }
        }
        
        return nodeId;
    }
    
    InetAddress getReqAddr(byte[] nodeIdDst, byte[] nodeIdSrc_old, byte[] req_num_old) {
        
        InetAddress nodeAddr = null;
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);
            
            if(tmpMap.containsKey(req_num)) {
                
                Info info = tmpMap.get(req_num);
                
                if(Crypto.cmpByteArray(info.nodeId_dst, nodeIdDst))  nodeAddr = info.nodeAddr_hop;
            }
        }
        
        return nodeAddr;
    }

    LinkedList<InetAddress> getIncludedNodes(byte[] nodeIdSrc_old, byte[] nodeIdDst, byte[] req_num_old) {
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);

            if(tmpMap.containsKey(req_num)) {
                
                Info info = tmpMap.get(req_num);
                if(Crypto.cmpByteArray(info.nodeId_dst, nodeIdDst)) return info.usedPeers;
            }
        }
        return null;
    }
    
    

    Boolean existsReq(byte[] nodeIdSrc_old, byte[] nodeIdDst, byte[] req_num_old) {
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)) {
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);

            if(tmpMap.containsKey(req_num)) {
                
                Info info = tmpMap.get(req_num);
                if(Crypto.cmpByteArray(info.nodeId_dst, nodeIdDst)) return true;
            }
        }    
        
        return false;
    }

    Tuple addResponse(byte[] nodeIdSrc_old, byte[] nodeIdOriginalDst, byte[] req_num_old, InetAddress nodeAddrHop, byte errNo, int hopAdvised) {
        
        //<errno,<size,size>>
        Tuple tuple = new Tuple(null, new Tuple(0, 0));
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)){
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);

            if(tmpMap.containsKey(req_num)){
                
                tuple.x = errNo;
                
                Info info = tmpMap.get(req_num);
                
                if(Crypto.cmpByteArray(info.nodeId_dst, nodeIdOriginalDst)) {
                    
                    info.responsivePeers.add(nodeAddrHop);
                    
                    if(errNo == 0x01) {
                        if(hopAdvised > info.advisedHop)    info.advisedHop = hopAdvised;
                        info.errNo = 0x01;
                    }
                    
                    else if(errNo == 0x00 && info.errNo != 0x01) {
                        info.errNo = errNo;
                    }
                    
                    ((Tuple)tuple.y).x = info.usedPeers.size();
                    ((Tuple)tuple.y).y = info.responsivePeers.size();
                }
            }
        }
        
        return tuple;
    }

    RReq getReqValues(byte[] nodeIdSrc_old, byte[] nodeIdDst, byte[] req_num_old) {
        
        RReq pduValues = null;
        
        ByteArray nodeIdSrc = new ByteArray(nodeIdSrc_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdSrc)){
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdSrc);

            if(tmpMap.containsKey(req_num)){
                Info info = tmpMap.get(req_num);
                
                if(Crypto.cmpByteArray(info.nodeId_dst, nodeIdDst)) {
                    pduValues = new RReq(info.hop_count, info.hop_max, info.peerKey, info.advisedHop, info.nodeAddr_hop);
                }
            }
        }
        
        return pduValues;
    }
}
