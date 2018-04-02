/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.PDU;

import aer.Data.Node;
import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author pedro
 */
public class RRep {


    private static byte[] dumpRemote(byte[] raw, int hopCount, int keySize) {
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
        
        return raw;
    }

    
    public static byte[] dumpLocal(byte[] nodeIdDst, int hopMax, Node node, byte secure) {
        byte[] id               = node.getId();
        byte[] pubk             = node.getPubKey();
        
        int counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        
        int len = 1 + 1 + 4 + id.length + nodeIdDst.length + 4 + pubk.length + 4 + 4 + node.getReqNum().length; //PDUTYPE+PDUSECURity+PDUTOTALSIZE+NODEIDSRC+NODEIDDST+PUBKEY+dw+SEQNUM+PEERS
        byte[] raw = new byte[len];
        
        //PDU TYPE
        raw[counter++] = 0x02;
        limit++;
        
        //PDU SECURITY
        raw[counter++] = secure;
        limit++;
        
        //PDU TOTAL SIZE
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //NODEIDSRC DATA
        limit+=id.length;
        for(; counter<limit; counter++) {
            raw[counter] = id[it++];
        }
        it = 0;
        
        //NODEIDDST DATA
        limit+=nodeIdDst.length;
        for(; counter<limit; counter++) {
            raw[counter] = nodeIdDst[it++];
        }
        it = 0;
        
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
        
        //HopCount
        buffer.clear();
        buffer.putInt(0);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //HopLimit
        buffer.clear();
        buffer.putInt(node.config.getHopLimit());
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //REQUEST SEQUENCE NUM
        limit+=node.getReqNum().length;
        for(; counter<limit; counter++) {
            raw[counter] = node.getReqNum()[it++];
        }
        it = 0;
        
        return raw;
    }

    public static void load(byte[] raw, Node id, InetAddress nodeHopAddr, Controller control) {
        int totalSize           = 0, keySize = 0, hopCount = 0, hopMax = 0; //Obtained Variables
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        byte secure             = 0x00;
        
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] tmp              = new byte[4]; //Auxiliary array for Integer
        byte[] req_num          = new byte[4]; // Request Identifier
        byte[] peerPubKey       = null;
        
        
        //GET SECURITY BYTE
        secure  =   raw[1];
        
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
            peerPubKey = new byte[keySize];
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
        
        if(Crypto.cmpByteArray(id.getId(), nodeIdDst)) { //SE PARA MIM
        
            //REMOVER LOCAL REQUEST CACHE
            InetAddress addr = id.rmReqCache(nodeIdDst, nodeIdSrc, req_num);
            if(addr !=  null) {
                //Redirecionar o Reply para o TCP, CHAVE PUBLICA DO PEER
                control.pushQueueTCP(null, new ByteArray(req_num), nodeHopAddr, peerPubKey);
            }
            
            //Adicionar Rota na Hit Cache
            byte[] nodeHopId = id.getNodeId(nodeHopAddr);
            if(nodeHopId != null)   id.addHitCache(nodeHopAddr, nodeHopId, nodeIdDst, hopCount);
            
            
        } else {
            // Retirar Request da Cache
            InetAddress nextHopAddr = id.rmReqCache(nodeIdSrc, nodeIdDst, req_num);
                    
            //INCREMENTAR HOP E REDIRECIONAR
            if(nextHopAddr != null) {
                byte[] reply = RRep.dumpRemote(raw, hopCount, keySize);
                control.pushQueueUDP(new Tuple(reply, nextHopAddr));
                
            } else { //ROUTE ERROR LOST ROUTE
                byte[] reply = RErr.dumpRemote(raw, hopCount);
                control.pushQueueUDP(new Tuple(reply, nodeHopAddr));
                
            }
        }
          
    }

}
