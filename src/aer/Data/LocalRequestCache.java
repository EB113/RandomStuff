/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author pedro
 */
public class LocalRequestCache {

    //Value Class
    class Info {
        LinkedList<InetAddress> usedPeers;
        byte                    errNo; //USADO PARA COLOCAR ERRO PREDOMINANTE QUE E O 0x01
        LinkedList<InetAddress> responsivePeers; //DOIS ARRAYS PARA DPS PODER USAR O DE USEDPEERS PARA PEDIR AOS NODOS QUE AINDA NAO FORAM USADOS, Tuple<Error Type, InetAddress>
        LinkedList<InetAddress> tempPeers;//PEERS COM RESPOSTA DE HOP LIMIT MAS CONHECEM
        
        long                    timestamp;
        int                     hop_count;
        int                     hop_max;
        
        Info(LinkedList<InetAddress> usedPeers, int hop_max) {
            
            this.usedPeers       = usedPeers;
            this.responsivePeers = new LinkedList();
            this.tempPeers       = new LinkedList();
            this.errNo           = 0x02;
            this.timestamp       = System.currentTimeMillis();
            this.hop_max         = hop_max;//QUANDO SE REPETE O PEDIDO POR HOP LIMIT MAS CONHECE?
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }
    }
    //NODE ID DST X info list
    HashMap <ByteArray, HashMap<ByteArray, Info>> hmap;
    Config config;
    
    public LocalRequestCache(Config config) {
       this.config  = config;
       this.hmap    = new HashMap<ByteArray, HashMap<ByteArray, Info>>();
    }

    // falta adicionar aos used peers
    void addRequest(LinkedList<InetAddress> usedPeers, byte[] nodeIdDst_old, byte[] req_num_old, int hop_max) {
    
        ByteArray nodeIdDst = new ByteArray(nodeIdDst_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdDst)) { //SE JA TEM DESTINO
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdDst);
            
            if(tmpMap.containsKey(req_num)){
                
                Info info = tmpMap.get(req_num);
                
                for(InetAddress addr : usedPeers) info.usedPeers.push(addr);
            }else if(tmpMap.size() < config.getReqMapSize()) {
                
                tmpMap.put(req_num, new Info(usedPeers, hop_max));
                this.hmap.put(nodeIdDst, tmpMap);
            }else{
                
                return;
            }
        }else{//SE NAO TEM DESTINO
            
            if(this.hmap.size() < config.getRequestCacheSize()) {//Se tem tamanho
                
                HashMap<ByteArray, Info> tmpMap = new HashMap<ByteArray, Info>();
                
                tmpMap.put(req_num, new Info(usedPeers, hop_max));
                
                this.hmap.put(nodeIdDst, tmpMap);
            }else {
                
                //se nao tem tamanho devolver error sem tamanho IMPORTANTE MAIS UMA FLAG NO ERRO
                return;
            }
        }
    }

    //ESTOU A SUPOR QUE NAO HA PEDIDO COM MESMO SEQ ID, como resolver??
    //ISTO E UM BOCADO DUVIDOSO
    byte[] getReqTarget(byte[] req_num_old) {
        
        ByteArray req_num = new ByteArray(req_num_old);
        
        byte[] nodeId = null;
        
        for(Map.Entry<ByteArray, HashMap<ByteArray, Info>> pair : this.hmap.entrySet()) {
            if(pair.getValue().containsKey(req_num)) {
                nodeId = pair.getKey().getData();
            }
        }
        
        return nodeId;
    }

    LinkedList<InetAddress> getIncludedNodes(byte[] nodeIdDst_old, byte[] req_num_old) {
        LinkedList<InetAddress> usedPeers = null;
        
        ByteArray nodeIdDst = new ByteArray(nodeIdDst_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdDst)) {
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdDst);
        
            if(tmpMap.containsKey(req_num)) {
                usedPeers = tmpMap.get(req_num).usedPeers;
            }   
        }
        return usedPeers;
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
                
                entry1.getValue().entrySet().removeIf(entry2 -> (now - entry2.getValue().getTimeStamp() > config.getLocalReqTimeDelta()));
                
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
    
    

    void rmReq(byte[] nodeIdDst_old, byte[] req_num_old) {
        
        ByteArray nodeIdDst = new ByteArray(nodeIdDst_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdDst)) {
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdDst);

            if(tmpMap.containsKey(req_num)) tmpMap.remove(req_num);
       }  
        
       return;
    }

    Boolean existsReq(byte[] nodeIdDst_old, byte[] req_num_old) {
        
        ByteArray nodeIdDst = new ByteArray(nodeIdDst_old);
        ByteArray req_num   = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdDst)){
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdDst);
            if(tmpMap.containsKey(req_num)) return true;
        }
        return false;
    }
    
    

    Tuple addResponse(byte[] nodeIdOriginalDst_old, byte[] req_num_old, InetAddress nodeAddrHop, byte errNo) {
        //0 igual, 1 diff, 2 hop, 3 null
        //<errno,<size,size>>
        Tuple tuple = new Tuple(null, new Tuple(0, 0));
        
        ByteArray nodeIdOriginalDst = new ByteArray(nodeIdOriginalDst_old);
        ByteArray req_num           = new ByteArray(req_num_old);
        
        if(this.hmap.containsKey(nodeIdOriginalDst)){
            
            HashMap<ByteArray, Info> tmpMap = this.hmap.get(nodeIdOriginalDst);

            if(tmpMap.containsKey(req_num)){
                
                Info info = tmpMap.get(req_num);
                tuple.x = errNo;
                
                if(errNo == 0x01){
                    
                    info.errNo = errNo;
                    
                    if(!info.tempPeers.contains(nodeAddrHop)) {
                        
                        info.tempPeers.add(nodeAddrHop);
                    }else if(!info.responsivePeers.contains(nodeAddrHop)){
                        tuple.x = 0x03;
                        info.responsivePeers.add(nodeAddrHop);
                    }else tuple.x = 0x03;
                }else {
                    
                    if(errNo == 0x00 && info.errNo != 0x01) {
                        info.errNo = errNo;
                    }
                    info.responsivePeers.add(nodeAddrHop);
                }
            
                ((Tuple)tuple.y).x = info.usedPeers.size();
                ((Tuple)tuple.y).y = info.responsivePeers.size();    
            }
        }
        
        return tuple;
    }
    
}
