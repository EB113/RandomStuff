/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.PDU;


import aer.Data.Node;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author pedro
 */
public class RReq {
    InetAddress hopaddr;
    int hop_count;
    int hop_max;
    byte[] key;
    
    int hopAdvised;
    
    public RReq(int hop_count, int hop_max, byte[] key, int hopAdvised, InetAddress hopaddr) {
        this.hopaddr    = hopaddr;
        
        this.hop_count  = hop_count;
        this.hop_max    = hop_max;
        this.key        = key;
        
        this.hopAdvised = hopAdvised;
    }
    
    public static void load(byte[] raw, Node id, InetAddress peerAddr, Controller control) {
        
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        int hopMax              = 0, hopCount = 0, totalSize = 0, keySize = 0; //Obtained Variables
        byte secure             = 0x00;
        byte[] tmp              = new byte[4];
        byte[] req_num          = new byte[4];
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] peerPubKey       = null;
        
        ByteBuffer wrapped = ByteBuffer.allocate(4);
        
        //GET SECURITY BYTE
        secure  =   raw[1];
        counter++;
        limit++;
        
        //GET PDU TOTAL SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped = ByteBuffer.wrap(tmp);
        totalSize = wrapped.getInt();
        it = 0;
        
        //GET NODEIDSRC 
        limit+=32;
        for(;counter<limit; counter++) nodeIdSrc[it++] = raw[counter];
        it = 0;
        
        //GET NODEIDDST 
        limit+=32;
        for(;counter<limit; counter++) nodeIdDst[it++] = raw[counter];
        it = 0;
        
        if(secure == 0x01) {
            //KEY SIZE
            limit+=4;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];
            wrapped.clear();
            wrapped = ByteBuffer.wrap(tmp);
            keySize = wrapped.getInt();
            it = 0;
            
            //KEY
            limit+=keySize;
            peerPubKey = new byte[keySize];
            for(;counter<limit; counter++) peerPubKey[it++] = raw[counter];
            it = 0;
        }
        
        //GET HOP COUNT
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped.clear();
        wrapped = ByteBuffer.wrap(tmp);
        hopCount = wrapped.getInt();
        it = 0;
        
        //GET HOP MAX
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped.clear();
        wrapped = ByteBuffer.wrap(tmp);
        hopMax = wrapped.getInt();
        it = 0;
        
        //GET SEQ DATA
        limit+=4;
        for(;counter<limit; counter++) req_num[it++] = raw[counter];
        it = 0;
        
        hopCount++; //INCREMENTAR CONTADOR
        
        LinkedList<InetAddress> usedPeers = new LinkedList<>(); //ARRAY COMPEERS USADOS PARA REENCAMINHA PACOTE
        
        if(Crypto.cmpByteArray(id.getId(), nodeIdDst)) { //SE PARA MIM
            
            //ADICIONAR HIT CACHE
            //Adicionar Rota na Hit Cache
            byte[] nodeHopId = id.getNodeId(peerAddr);
            if(nodeHopId != null)   id.addHitCache(peerAddr, nodeHopId, nodeIdSrc, hopCount);
            
            //CRIAR UM ROUTE REPLY
            byte[] reply = RRep.dumpLocal(nodeIdSrc, hopCount, id, secure, req_num);
            control.pushQueueUDP(new Tuple(reply, peerAddr));
        } else if(Crypto.cmpByteArray(id.getId(), nodeIdSrc) || id.existsReq(nodeIdSrc, nodeIdDst, req_num)){
            
            //VALE A PENA ADICIONAR?
            //ADICIONAR HIT CACHE
            //Adicionar Rota na Hit Cache
            byte[] nodeHopId = id.getNodeId(peerAddr);
            if(nodeHopId != null)   id.addHitCache(peerAddr, nodeHopId, nodeIdSrc, hopCount);
            
            return;
        } else {
            
            //ADICIONAR HIT CACHE
            //Adicionar Rota na Hit Cache
            byte[] nodeHopId = id.getNodeId(peerAddr);
            if(nodeHopId != null)   id.addHitCache(peerAddr, nodeHopId, nodeIdSrc, hopCount);
            
            Tuple peer = id.getZonePeer(nodeIdDst); //Check Zone
            if(peer == null) peer = id.getHitPeer(nodeIdDst); //Check Hit
            
            if(peer != null) { //Se esta na ZONE TOPOLOGY ou Hit Cache
                usedPeers.push((InetAddress)peer.y);
                
                if(hopCount+(int)peer.x<hopMax) { //SE HOP COUNT AINDA BOM NOTA FALTA 
                    
                    //ADD REQUEST TO CACHE
                    id.addReqCache(usedPeers, peerAddr, nodeIdSrc, nodeIdDst, hopCount, hopMax, req_num, peerPubKey);
                    
                    //new RReq para peerRouteId
                    System.out.println("1");
                    byte[] reply = RReq.dumpRemote(raw, hopCount, keySize);
                    control.pushQueueUDP(new Tuple(reply, (InetAddress)peer.y));
                    
                }/* else { //SE HOP COUNT MAXm
                    System.out.println("2");
                    //RError mas tem caminho
                    byte[] reply = RErr.dumpLocal((byte)0x01, hopCount, (int)peer.x, id, nodeIdDst, nodeIdSrc, req_num);
                    control.pushQueueUDP(new Tuple(reply, (InetAddress)peer.y));
                }*/
            }else { //SE NAO ESTA NA ZONE TOPOLOGY ou Hit Cache
                if(hopCount+2<hopMax) {
                    System.out.println("3");
                    //SENAO ROUTE REQUEST OPTIMISTA
                    LinkedList<InetAddress> peerList = id.getReqRankPeers(peerAddr, null); //Get Most Probable Peers Without the origin node
                    if(peerList != null && peerList.size() > 0){
                        //ADD REQUEST TO CACHE
                        id.addReqCache(peerList, peerAddr, nodeIdSrc, nodeIdDst, hopCount, hopMax, req_num, peerPubKey);

                        byte[] reply = RReq.dumpRemote(raw, hopCount, keySize);
                        
                        //SEND REQUEST
                        for(InetAddress val : peerList) {
                            control.pushQueueUDP(new Tuple(reply, val));
                        }
                    }/*else {
                        //ROUTE ERROR NO ROUTE
                        byte[] reply = RErr.dumpLocal((byte)0x02, hopCount, 0, id, nodeIdDst, nodeIdSrc, req_num);
                        control.pushQueueUDP(new Tuple(reply, peerAddr));
                    }*/
                    
                }/*else {
                    //ROUTE ERROR HOP LIMIT
                    byte[] reply = RErr.dumpLocal((byte)0x00, hopCount, 0, id, nodeIdDst, nodeIdSrc, req_num);
                    control.pushQueueUDP(new Tuple(reply, peerAddr));
                }*/
            }
        
        }  
    }
    
    public static byte[] dumpLocal(Node node, byte[] nodeDst, int hopLimit) {
        
        byte[] id               = node.getId();
        byte[] pubk             = node.getPubKey();
        
        int counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        byte secure = node.config.getSecurity();
        int len = 0;
        
        node.incReqNum();
        byte[] req_num = node.getReqNum();
        
        if(secure == 0x01)
            len = 1 + 1 + 4 + id.length + nodeDst.length + 4 + pubk.length + 4 + 4 + req_num.length; //PDUTYPE+PDUSECURity+PDUTOTALSIZE+NODEIDSRC+NODEIDDST+PUBKEY+dw+SEQNUM+PEERS
        else
            len = 1 + 1 + 4 + id.length + nodeDst.length + 4 + 4 + req_num.length;
        
        byte[] raw = new byte[len];
        
        //System.out.println(len + "REQLEN: " + node.getReqNum().length);
        
        //PDU TYPE
        raw[counter++] = 0x01;
        limit++;
        //System.out.print("|" + counter + "," + limit + "|");
        //PDU SECURITY
        raw[counter++] = secure;
        limit++;
        //System.out.print("|" + limit + "|");
        //PDU TOTAL SIZE
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //NODEIDSRC DATA
        limit+=id.length;
        for(; counter<limit; counter++) {
            raw[counter] = id[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //NODEIDDST DATA
        limit+=nodeDst.length;
        for(; counter<limit; counter++) {
            raw[counter] = nodeDst[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //SE SECURITY ACTIVE
        if(secure == 0x01) {
            //PUBKEY SIZE
            buffer.clear();
            buffer.putInt(pubk.length);
            tmp = buffer.array();
            limit+=4;
            for(; counter<limit; counter++) {
                raw[counter] = tmp[it++];
            }
            it = 0;

            //PUBKEY
            limit+=pubk.length;
            for(; counter<limit; counter++) {
                raw[counter] = pubk[it++];
            }
            it = 0;
        }
        //System.out.print("|" + counter + "," + limit + "|");
        //HopCount
        buffer.clear();
        buffer.putInt(0);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //HopLimit
        buffer.clear();
        buffer.putInt(hopLimit);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //REQUEST SEQUENCE NUM
        limit+=req_num.length;
        for(; counter<limit; counter++) {
            raw[counter] = req_num[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        
        //System.out.println(raw.length + "<---RREQLOCAL: " + Crypto.toHex(raw));
        System.out.println("<---RREQLOCAL: ");
        
        return raw;
    }

    public static byte[] dumpRemote(byte[] raw, int hopCount, int keySize) {
        int counter = 0;
        
        counter+=2; //PDU TYPE + SECURE OPT
        counter+=4; //PDU TOTAL SIZE
        counter+=64; // NODE ID SRC + DST
        if(raw[1] == 0x01) {
            counter += 4; //KEYSIZE
            counter += keySize; //KEY
        }
        
        //HopCount
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(hopCount);
        byte[] tmp = buffer.array();
        int limit=counter+4, it=0;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        
        //System.out.println(raw.length + "<---RREQREMOTE: " + Crypto.toHex(raw));
        System.out.println("<---RREQREMOTE: ");
        return raw;
    }
    
    public static byte[] dumpRemoteWithValues(byte[] nodeIdSrc, byte[] nodeIdDst, int hopCount, int hopMax, byte[] key, byte[] req_num) {
        
        
        int counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        byte secure = 0x00;
        
        int len = 0;
        
        
        if(key != null){
            secure = 0x01;
            len = 1 + 1 + 4 + nodeIdSrc.length + nodeIdDst.length + 4 + key.length + 4 + 4 + req_num.length; //PDUTYPE+PDUSECURity+PDUTOTALSIZE+NODEIDSRC+NODEIDDST+PUBKEY+dw+SEQNUM+PEERS
        }else
            len = 1 + 1 + 4 + nodeIdSrc.length + nodeIdDst.length + 4 + 4 + req_num.length;
        
        byte[] raw  = new byte[len];
        
        //System.out.println(len + "REQLEN: " + node.getReqNum().length);
        
        //PDU TYPE
        raw[counter++] = 0x01;
        limit++;
        
        //System.out.print("|" + counter + "," + limit + "|");
        //PDU SECURITY
        raw[counter++] = secure;
        limit++;
        //System.out.print("|" + limit + "|");
        //PDU TOTAL SIZE
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //NODEIDSRC DATA
        limit+=nodeIdSrc.length;
        for(; counter<limit; counter++) {
            raw[counter] = nodeIdSrc[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //NODEIDDST DATA
        limit+=nodeIdDst.length;
        for(; counter<limit; counter++) {
            raw[counter] = nodeIdDst[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //SE SECURITY ACTIVE
        if(secure == 0x01) {
            //PUBKEY SIZE
            buffer.clear();
            buffer.putInt(key.length);
            tmp = buffer.array();
            limit+=4;
            for(; counter<limit; counter++) {
                raw[counter] = tmp[it++];
            }
            it = 0;

            //PUBKEY
            limit+=key.length;
            for(; counter<limit; counter++) {
                raw[counter] = key[it++];
            }
            it = 0;
        }
        //System.out.print("|" + counter + "," + limit + "|");
        //HopCount
        buffer.clear();
        buffer.putInt(hopCount);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //HopLimit
        buffer.clear();
        buffer.putInt(hopMax);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        //REQUEST SEQUENCE NUM
        limit+=req_num.length;
        for(; counter<limit; counter++) {
            raw[counter] = req_num[it++];
        }
        it = 0;
        //System.out.print("|" + counter + "," + limit + "|");
        
        //System.out.println(raw.length + "<---RREQLOCAL: " + Crypto.toHex(raw));
        System.out.println("<---RREQREMOTE ");
        
        return raw;
    }
}
