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
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author pedro
 */
public class RErr {


    public static void load(byte[] raw, Node id, InetAddress hopAddr, Controller control) {
    
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        int totalSize           = 0, hopCount = 0, hopMax = 0; //Obtained Variables
        byte errNo              = 0x00;
        
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] tmp              = new byte[4]; //Auxiliary array for Integer
        byte[] req_num          = new byte[4]; //Request Identifier
        byte[] nodeIdTarget     = null; //NODEID OF REQUEST in Cache
                
        //GET ERRNO
        errNo = raw[limit];
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
        
        switch(errNo) {
            case 0x00: // SE HOP LIMIT
                
                if(Crypto.cmpByteArray(id.getId(), nodeIdDst)) {
                    nodeIdTarget = id.getLocalReqTarget(req_num);
                
                    //VERIFICAR EXISTENCIA DE REQUEST
                    if(nodeIdTarget != null) {

                        //GET NODES NOT USED IN PREVIOUS REQUEST that have neighbours
                        LinkedList<InetAddress> notUsedPeers = id.getExcludedNodes(nodeIdDst, nodeIdSrc, req_num);//Na pesquisa pelo request e ao contrario src e dst

                        //ALREADY TRIED ALL PEERS
                        if(notUsedPeers.size() == 0) {
                            //WHAT TO DO????
                            System.out.println("NO ROUTE(ALL PEERS TRIED)!");
                        }else {//TRY REMAINING PEERS

                            while(notUsedPeers.size() > 0) {

                                byte[] reply = RReq.dumpLocal(id, nodeIdDst, id.config.getHopLimit());
                                control.pushQueueUDP(new Tuple(reply, notUsedPeers.poll()));
                            }
                        }
                    }else { //REQUEST LOST
                        //WHAT TO DO????
                        System.out.println("LOCAL REQUEST LOST!"); 
                    }
                    
                } else {
                    if(hopCount<hopMax) {
                        InetAddress nextHopAddr = id.getLocalReqAddr(nodeIdDst, nodeIdSrc, req_num);
                        byte[] reply = RErr.dumpRemote(raw, hopCount);
                        control.pushQueueUDP(new Tuple(reply, nextHopAddr));
                    }
                }
                
                break;
            case 0x01: // SE HOP LIMIT MAS TEM VISAO
                
                if(Crypto.cmpByteArray(id.getId(), nodeIdDst)) {
                    //TENTAR O MESMO MAS COM MAIS HOPS
                    byte[] reply = RReq.dumpLocal(id, nodeIdDst, id.config.getHopLimit() * 2);
                    control.pushQueueUDP(new Tuple(reply, hopAddr));
                }else {
                    if(hopCount<hopMax) {
                        InetAddress nextHopAddr = id.getLocalReqAddr(nodeIdDst, nodeIdSrc, req_num);
                        byte[] reply = RErr.dumpRemote(raw, hopCount);
                        control.pushQueueUDP(new Tuple(reply, nextHopAddr));
                    }
                }
                
                break;
            case 0x02: //SE PERDEU ROUTE, NORMALMENTE RESPOSTA A UM REPLY REMOVER ENTRADA
                
                if(Crypto.cmpByteArray(id.getId(), nodeIdDst)) {
                    nodeIdTarget = id.getLocalReqTarget(req_num);
                    id.rmHit(nodeIdDst, nodeIdTarget, hopAddr);
                    System.out.println("NODE LOST ROUTE!");
                }else {
                    if(hopCount<hopMax) {
                        InetAddress nextHopAddr = id.getLocalReqAddr(nodeIdDst, nodeIdSrc, req_num);
                        byte[] reply = RErr.dumpRemote(raw, hopCount);
                        control.pushQueueUDP(new Tuple(reply, nextHopAddr));
                    }
                }
                
                break;
            default:
                System.out.println("WRONG ERRNO!");
                break;
        }
    }

    static byte[] dumpRemote(byte[] raw, int hopCount) {
    
        int counter = 0;
        
        counter+=2; //PDU TYPE + errNo 
        counter+=4; //PDU TOTAL SIZE
        counter+=64; // NODE ID SRC + DST
        
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

    static byte[] dumpLocal(byte errNo, int hopCount, Node node, byte[] nodeIdDst, byte[] req_num) {
        byte[] id               = node.getId();
        
        int counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        int len = 1 + 1 + 4 + id.length + nodeIdDst.length + 4 + 4 + req_num.length; //PDUTYPE+PDUSECURity+PDUTOTALSIZE+NODEIDSRC+NODEIDDST+PUBKEY+dw+SEQNUM+PEERS
        byte[] raw = new byte[len];
        
        
        //PDU TYPE
        raw[counter++] = 0x03;
        limit++;
        
        //ErrNo
        raw[counter++] = errNo;
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
            raw[counter] = req_num[it++];
        }
        it = 0;
        
        return raw;
    }
    
}
