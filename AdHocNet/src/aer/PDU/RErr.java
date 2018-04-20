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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author pedro
 */
public class RErr {


    public static void load(byte[] raw, Node id, InetAddress hopAddr, Controller control) {
    
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        int totalSize           = 0, hopCount = 0, hopMax = 0, leftHops = 0; //Obtained Variables
        byte errNo              = 0x00;
        
        byte[] nodeIdOriginalDst    = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdSrc            = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst            = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] tmp                  = new byte[4]; //Auxiliary array for Integer
        byte[] req_num              = new byte[4]; //Request Identifier
        byte[] nodeIdTarget         = null; //NODEID OF REQUEST in Cache
                
        //GET ERRNO
        errNo = raw[limit];
        counter++;
        limit++;
        
        ByteBuffer wrapped = ByteBuffer.allocate(4);
        
        //GET HOP LEFT IF HOP LIMIT BUT KNOWS ROUTE
        if(errNo == 0x01){
            limit+=4;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];
            wrapped = ByteBuffer.wrap(tmp);
            leftHops = wrapped.getInt();
            it = 0;
        }
        
        //GET PDU TOTAL SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped.clear();
        totalSize = wrapped.getInt();
        it = 0;
        
        //ORIGINAL DESTINATION
        limit+=32;
        for(;counter<limit; counter++) nodeIdOriginalDst[it++] = raw[counter];
        it = 0;
        
        //GET NODEIDSRC 
        limit+=32;
        for(;counter<limit; counter++) nodeIdSrc[it++] = raw[counter];
        it = 0;
        
        //GET NODEIDDST 
        limit+=32;
        for(;counter<limit; counter++) nodeIdDst[it++] = raw[counter];
        it = 0;
        
        
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
        
        //RTEMOVER HIT CACHE
        //TODO MUITA COISA
        
        byte[] hopId = id.getNodeId(hopAddr);
        
        if(hopId != null)   id.addHitCache(hopAddr, hopId, nodeIdSrc, hopCount);
        id.rmHit(nodeIdSrc, nodeIdOriginalDst, hopAddr); //Remover entrada na zone topology ou hit cache disto
        
        
        if(id.existsReq(nodeIdDst, nodeIdOriginalDst, req_num)) {
            
            //DEVOLVE TAMANHO DO USED PEERS E RESPONSIVE PEERS
            Tuple tuple = id.addResponsivePeer(nodeIdDst, nodeIdOriginalDst, req_num, hopAddr, errNo, leftHops);

            int responsiveSize  = (int)((Tuple)(tuple.y)).x; //no de peers que responderam
            int usedSize        = (int)((Tuple)(tuple.y)).y; //no de peers a quem enviamos

            byte strongerErrNo  = (byte)tuple.x; //ERRO MAIS FORTE DOS JA RECEBIDOS
            
            //GET ORIGINAL REQUEST VALUES + hopadvised
            RReq requestPDU = id.getReqValues(nodeIdDst, nodeIdOriginalDst, req_num);
            
            LinkedList<InetAddress> peers = id.getReqPeers(hopAddr);
        
            if(Crypto.cmpByteArray(id.getId(), nodeIdDst)) {//SE PARA MIM
                
                if(usedSize == 1) {//Se estava na zone ou na hit cache
                    
                    if(errNo == 0x01 && strongerErrNo != 0x03) {//PRIMEIRO QUE RECEBE NOS PROXIMOS DESCARTA
                        
                        byte[] reply = RReq.dumpLocal(id, nodeIdOriginalDst, leftHops + id.config.getHopLimit());
                        control.pushQueueUDP(new Tuple(reply, hopAddr));
                    }else {
                        
                        LinkedList<InetAddress> peersRank = id.getReqRankPeers(hopAddr, null);
                        
                        if(peersRank != null && peersRank.size() > 0) {
                            
                            //ADD REQUEST TO CACHE
                            id.addReqCache(peersRank, hopAddr, nodeIdDst, nodeIdOriginalDst, hopCount, id.config.getHopLimit(), req_num, null);

                            //SEND REQUEST
                            for(InetAddress val : peersRank) {
                                byte[] reply = RReq.dumpLocal(id, nodeIdOriginalDst, id.config.getHopLimit());
                                control.pushQueueUDP(new Tuple(reply, val));
                            }
                        }else {
                            
                            //REMOVE LOCAL REQUEST
                            id.rmReqCache(nodeIdOriginalDst, nodeIdDst, req_num);

                            //TRANSMIT TO TCP
                            control.pushQueueTCP(null, new ByteArray(req_num), hopAddr, null);
                        }
                    }
                
                }else if(usedSize < peers.size()) { //se ja tinha tentado o rank
                    
                    if(errNo == 0x01 && strongerErrNo != 0x03) {//PRIMEIRO QUE RECEBE NOS PROXIMOS DESCARTA
                        
                        byte[] reply = RReq.dumpLocal(id, nodeIdOriginalDst, leftHops + id.config.getHopLimit());
                        control.pushQueueUDP(new Tuple(reply, hopAddr));
                    }else {
                        
                        LinkedList<InetAddress> peersRemaining = id.getExcludedNodes(nodeIdDst, nodeIdOriginalDst, req_num);
                        
                        if(peersRemaining != null && peersRemaining.size() > 0) {
                            
                            //ADD REQUEST TO CACHE
                            id.addReqCache(peersRemaining, hopAddr, nodeIdDst, nodeIdOriginalDst, hopCount, id.config.getHopLimit(), req_num, null);

                            //SEND REQUEST
                            for(InetAddress val : peersRemaining) {
                                byte[] reply = RReq.dumpLocal(id, nodeIdOriginalDst, id.config.getHopLimit());
                                control.pushQueueUDP(new Tuple(reply, val));
                            }
                        }else {
                            
                            //REMOVE LOCAL REQUEST
                            id.rmReqCache(nodeIdOriginalDst, nodeIdDst, req_num);

                            //TRANSMIT TO TCP
                            control.pushQueueTCP(null, new ByteArray(req_num), hopAddr, null);
                        }
                    }
                }else {
                
                    //REMOVE LOCAL REQUEST
                    id.rmReqCache(nodeIdOriginalDst, nodeIdDst, req_num);
                    
                    //TRANSMIT TO TCP
                    control.pushQueueTCP(null, new ByteArray(req_num), hopAddr, null);
                }
            }else {
                
                if(usedSize == 1) {//Se estava na zone ou na hit cache
                    System.out.println("ESTAVA NA ZONE!");
                    //DEVOLVER LOGO OUY CONTINUAR COM PEDIDOS???????????PARA JA ESTA CONTINUAR COM PEDIDOS
                    LinkedList<InetAddress> peersRank = id.getReqRankPeers(hopAddr, requestPDU.hopaddr);

                    if(peersRank != null && peersRank.size() > 0) {

                        //ADD REQUEST TO CACHE
                        id.addReqCache(peersRank, hopAddr, nodeIdDst, nodeIdOriginalDst, hopCount, id.config.getHopLimit(), req_num, null);

                        //SEND REQUEST
                        for(InetAddress val : peersRank) {
                            byte[] reply = RReq.dumpRemoteWithValues(nodeIdDst, nodeIdOriginalDst, requestPDU.hop_count, requestPDU.hop_max, requestPDU.key, req_num);
                            control.pushQueueUDP(new Tuple(reply, val));
                        }
                    }else { // Se So tinha 1 nodo a quem mandar
                        
                        //GET ADDRESS TO WhICH RETURN
                        InetAddress addr = id.getRReqHopAddr(nodeIdOriginalDst, nodeIdDst, req_num);
                        
                        //DEVOLVER ERRO
                        byte[] reply = RErr.dumpRemote(raw, hopCount);
                        control.pushQueueUDP(new Tuple(reply, addr));
                        
                        //REMOVE LOCAL REQUEST
                        id.rmReqCache(nodeIdOriginalDst, nodeIdDst, req_num);
                    }
                }else if(usedSize < peers.size()) { //se ja tinha tentado o rank
                    System.out.println("ESTAVA NO RANK!");
                    LinkedList<InetAddress> peersRemaining = id.getExcludedNodes(nodeIdDst, nodeIdOriginalDst, req_num);
                        
                    if(peersRemaining != null && peersRemaining.size() > 0) {

                        //ADD REQUEST TO CACHE
                        id.addReqCache(peersRemaining, hopAddr, nodeIdDst, nodeIdOriginalDst, hopCount, id.config.getHopLimit(), req_num, null);

                        //SEND REQUEST
                        for(InetAddress val : peersRemaining) {
                            byte[] reply = RReq.dumpRemoteWithValues(nodeIdDst, nodeIdOriginalDst, requestPDU.hop_count, requestPDU.hop_max, requestPDU.key, req_num);
                            control.pushQueueUDP(new Tuple(reply, val));
                        }
                    }else {
                        
                        //GET ADDRESS TO WhICH RETURN
                        InetAddress addr = id.getRReqHopAddr(nodeIdOriginalDst, nodeIdDst, req_num);
                        
                        //DEVOLVER ERRO Mais Forte
                        byte[] reply = RErr.dumpLocal(strongerErrNo, hopMax, requestPDU.hopAdvised, id, nodeIdOriginalDst, nodeIdDst, req_num);  
                        control.pushQueueUDP(new Tuple(reply, addr));
                        
                        //REMOVE LOCAL REQUEST
                        id.rmReqCache(nodeIdOriginalDst, nodeIdDst, req_num);
                    }
                    
                }else {
                    System.out.println("NAO HA MAIS NADA!");
                    //GET ADDRESS TO WhICH RETURN
                    InetAddress addr = id.getRReqHopAddr(nodeIdOriginalDst, nodeIdDst, req_num);

                    //DEVOLVER ERRO
                    byte[] reply = RErr.dumpLocal(strongerErrNo, hopMax, requestPDU.hopAdvised, id, nodeIdOriginalDst, nodeIdDst, req_num); 
                    control.pushQueueUDP(new Tuple(reply, addr));

                    //REMOVE LOCAL REQUEST
                    id.rmReqCache(nodeIdOriginalDst, nodeIdDst, req_num);
                }
                
            }
        }else {
            System.out.println("NAO EXISTE REQUEST!");
            //ACRESCENTAR PONTOS por devolver fora do tempo???
            return;
        }
    }

    //TODO COM ERRNO!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    static byte[] dumpRemote(byte[] raw, int hopCount) {
    
        int counter = 0;
        
        counter+=2; //PDU TYPE + errNo 
        if(raw[1] == 0x01) counter+=4;
        
        counter+=4; //PDU TOTAL SIZE
        counter+=96; // NODE ID SRC + DST
        
        //HopCount
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(hopCount);
        byte[] tmp = buffer.array();
        int limit=counter+4, it=0;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        
        //System.out.println(raw.length + "<---RRERRORREMOTE: " + Crypto.toHex(raw));
        System.out.println("<---RRERRORREMOTE: ");
        return raw;
    }

    static byte[] dumpLocal(byte errNo, int hopMax, int hopAdvised, Node node, byte[] nodeIdOriginalDst, byte[] nodeIdDst, byte[] req_num) {
        byte[] id               = node.getId();
        
        int counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        int len = 0;
        if(errNo == 0x01) {
        
            len = 1 + 1 + 4 + 4 + id.length + nodeIdOriginalDst.length + nodeIdDst.length + 4 + 4 + req_num.length; //PDUTYPE+errNo+PDUTOTALSIZE+NODEIDSRC+NODEIDDST+PUBKEY+dw+SEQNUM+PEERS
        }else {
            
            len = 1 + 1 + 4 + id.length + nodeIdOriginalDst.length + nodeIdDst.length + 4 + 4 + req_num.length; //PDUTYPE+errNo+PDUTOTALSIZE+NODEIDSRC+NODEIDDST+PUBKEY+dw+SEQNUM+PEERS
        }
        byte[] raw = new byte[len];
        ByteBuffer buffer = ByteBuffer.allocate(4);// VaRIAVEL AUXILIAR PARA BUFFER DE NUMEROS
        
        //PDU TYPE
        raw[counter++] = 0x03;
        limit++;
        
        //ErrNo
        raw[counter++] = errNo;
        limit++;
        
        //SE HOP LIMIT MAS CONHECE
        if(errNo == 0x01){
            buffer.putInt(hopAdvised);
            tmp = buffer.array();
            limit+=4;
            for(; counter<limit; counter++) {
                raw[counter] = tmp[it++];
            }
            it = 0;
        }
        
        //PDU TOTAL SIZE
        buffer.clear();
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //NODEIDOriginalSRC DATA
        limit+=nodeIdOriginalDst.length;
        for(; counter<limit; counter++) {
            raw[counter] = nodeIdOriginalDst[it++];
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
        buffer.putInt(hopMax);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //REQUEST SEQUENCE NUM
        limit+=node.getReqNum().length;
        for(; counter<limit; counter++) {
            raw[counter] = req_num[it++];
        }
        it = 0;
        
        //System.out.println(raw.length + "<---RERRORLOCAL: " + Crypto.toHex(raw));
        System.out.println("<---RERRORLOCAL: ");
        return raw;
    }
    
}
