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
import java.nio.charset.StandardCharsets;

/**
 *
 * @author pedro
 */
public class Data {

    public static void load(byte[] raw, Node id, InetAddress origin, Controller control) {
        int totalSize           = 0, hopCount = 0, hopMax = 0, dataSize = 0, offset = 0, fragTotalSize = 0; //Obtained Variables
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        
        byte secure             = 0x00;
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] tmp              = new byte[4]; //Auxiliary array for Integer
        byte[] req_num          = new byte[4]; // Request Identifier
        
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
         
        //GET PDU OFFSET
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped = ByteBuffer.wrap(tmp);
        offset = wrapped.getInt();
        it = 0;
        
        //GET PDU FRAG SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped = ByteBuffer.wrap(tmp);
        fragTotalSize = wrapped.getInt();
        it = 0;
        
        //GET NODEIDSRC 
        limit+=32;
        for(;counter<limit; counter++) nodeIdSrc[it++] = raw[counter];
        it = 0;
        
        //GET NODEIDDST 
        limit+=32;
        for(;counter<limit; counter++) nodeIdDst[it++] = raw[counter];
        it = 0;
        
        //GET HOPCOUNT
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped = ByteBuffer.wrap(tmp);
        hopCount = wrapped.getInt();
        it = 0;
        
        //GET HOPMAX
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped = ByteBuffer.wrap(tmp);
        hopMax = wrapped.getInt();
        it = 0;
        
       //GET SEQ DATA
        limit+=4;
        for(;counter<limit; counter++) req_num[it++] = raw[counter];
        it = 0;
        
        if(Crypto.cmpByteArray(id.getId(), nodeIdDst)){
            
            //CALCULATE DATA SIZE
            dataSize = totalSize - (1 + 1 + 4 + 4 + 4 + 32 + 32 + 4 + 4 + 4); //type,sec,totalsize, offset, fragsize, src,dst,hopcount,hopmax,reqnum
            byte[] data = new byte[dataSize];

            //GET DATA
            limit+=dataSize;
            for(;counter<limit; counter++) data[it++] = raw[counter];
            it = 0;
            
            //PEDIDO
            if(control.existsTCP(req_num)){
                
                if(secure == 0x00){
                    
                    //Redirecionar o Reply para o TCP, CHAVE PUBLICA DO PEER
                    control.pushQueueTCP(data, new ByteArray(req_num), origin, null);
                }else {
                    
                    
                }
            
            }else {//SENAO RESPONDER COM DATA
                
                if(secure == 0x00){
                
                    byte[] reply = Data.dumpLocal(nodeIdSrc, hopMax, id, secure, req_num);
                    control.pushQueueUDP(new Tuple(reply, origin));
                }else {
                
                }
            }
            
            
            
            
        }else {
            
            hopCount++;
            
            //INCREMENTAR HOP E REDIRECIONAR
            
        }
    }

    
    public static byte[] dumpLocal(byte[] nodeIdDst, int hopMax, Node node, byte secure, byte[] req_num) {
        
        
        byte[] id               = node.getId();
        
        int counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        String data = "Olá sou o peer: " + Crypto.toHex(node.getId());
        
        byte[] data_bytes = data.getBytes(StandardCharsets.UTF_8);
        
        int len = 1 + 1 + 4 + 4 + 4 + 32 + 32 + 4 + 4 + 4 + data_bytes.length;
        
        byte[] raw = new byte[len];
        
        //PDU TYPE
        raw[counter++] = 0x04;
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
        
        //PDU OFFSET
        buffer.clear();
        buffer.putInt(0);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //PDU FRAGTOTALSIZE
        buffer.clear();
        buffer.putInt(data_bytes.length);
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
        limit+=req_num.length;
        for(; counter<limit; counter++) {
            raw[counter] = req_num[it++];
        }
        it = 0;
        
        limit+=data_bytes.length;
        for(; counter<limit; counter++) {
            raw[counter] = data_bytes[it++];
        }
        
        System.out.println("<---DATALOCAL: ");
        
        return raw;
        
    }
    
    
    
    
    
    private static byte[] dumpRemote(byte[] raw, int hopCount) {
        
        int counter = 0;
        
        counter+=2; //PDU TYPE + SECURE OPT
        counter+=4; //PDU TOTAL SIZE
        counter+=4; //PDU OFFSET
        counter+=4; //PDU TOTAL FRAG SIZE
        counter+=64; // NODE ID SRC + DST
        
        //HopCount
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(hopCount);
        byte[] tmp = buffer.array();
        int limit=counter+4, it=0;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        
        //System.out.println(raw.length + "<---RREPREMOTE: " + Crypto.toHex(raw));
        System.out.println("<---DATAREMOTE: ");
        
        return raw;
    }
    
    
    
    
    
}
