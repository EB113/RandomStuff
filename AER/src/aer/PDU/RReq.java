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
    public static void load(byte[] raw, Node id, InetAddress peerAddr, Controller control) {
        
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        int hopMax              = 0, hopCount = 0, totalSize = 0, keySize = 0; //Obtained Variables
        byte secure             = 0x00;
        byte[] tmp              = new byte[4];
        byte[] req_num          = new byte[4];
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] peerPubKey       = null;
        
        //GET SECURITY BYTE
        secure  =   raw[1];
        counter++;
        limit++;
        
        //GET PDU TOTAL SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        ByteBuffer wrapped = ByteBuffer.wrap(tmp);
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
            //System.out.println("PARA MIM!");
            //Se Route Reply corre mal tentar outro????
            //?????????????????????????????????????????
            
            //CRIAR UM ROUTE REPLY
            byte[] reply = RRep.dumpLocal(nodeIdSrc, hopCount, id, secure, req_num);
            control.pushQueueUDP(new Tuple(reply, peerAddr));
        } else if(Crypto.cmpByteArray(id.getId(), nodeIdSrc)) {
            //DO NOTHING, IM THE ORIGIN OF REQUEST
            //System.out.println("FUI EU!");
        } else {
            //System.out.println(Crypto.toHex(id.getId()));
            //System.out.println(Crypto.toHex(nodeIdDst));
            //System.out.println("PARA Outro!" + Crypto.cmpByteArray(id.getId(), nodeIdDst) + " | " + Arrays.equals(id.getId(), nodeIdDst));
            Tuple peer = id.getZonePeer(nodeIdDst); //Check Zone
            if(peer == null) peer = id.getHitPeer(nodeIdDst); //Check Hit
            
            if(peer != null) { //Se esta na ZONE TOPOLOGY ou Hit Cache
                usedPeers.push((InetAddress)peer.x);
                
                if(hopCount+(int)peer.y<hopMax) { //SE HOP COUNT AINDA BOM NOTA FALTA 
                    
                    //ADD REQUEST TO CACHE
                    id.addReqCache(usedPeers, peerAddr, nodeIdSrc, nodeIdDst, hopCount, req_num);
                    
                    //new RReq para peerRouteId
                    byte[] reply = RReq.dumpRemote(raw, hopCount, keySize);
                    control.pushQueueUDP(new Tuple(reply, peer.x));
                    
                } else { //SE HOP COUNT MAX
                    
                    //RError mas tem caminho
                    byte[] reply = RErr.dumpLocal((byte)0x01, hopCount, id, nodeIdSrc, req_num);
                    control.pushQueueUDP(new Tuple(reply, peer.x));
                }
            }else { //SE NAO ESTA NA ZONE TOPOLOGY ou Hit Cache
                if(hopCount+1<hopMax) {
                    
                    //SENAO ROUTE REQUEST OPTIMISTA
                    LinkedList<InetAddress> peerList = id.getReqPeers(); //Get Most Probable Peers
                    
                    //ADD REQUEST TO CACHE
                    id.addReqCache(peerList, peerAddr, nodeIdSrc, nodeIdDst, hopCount, req_num);
                    
                    //SEND REQUEST
                    for(InetAddress val : peerList) {
                        byte[] reply = RReq.dumpRemote(raw, hopCount, keySize);
                        control.pushQueueUDP(new Tuple(reply, val));
                    }
                    
                } else {
                    //ROUTE ERROR HOP LIMIT
                    byte[] reply = RErr.dumpLocal((byte)0x00, hopCount, id, nodeIdSrc, req_num);
                    control.pushQueueUDP(new Tuple(reply, peerAddr));
                }
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
        
        System.out.println(raw.length + "<---RREQLOCAL: " + Crypto.toHex(raw));
        
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
        
        System.out.println(raw.length + "<---RREQREMOTE: " + Crypto.toHex(raw));
        
        return raw;
    }
}
