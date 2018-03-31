/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import aer.Data.Node;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * @author pedro
 */
public class Datagram {
    
    public static byte[] dumpHello(Node node) {
        
        byte[] id               = node.getId();
        byte[] seq              = node.getSeq();
        ArrayList<Tuple> peers  = node.getZonePeersIds(1);
        int zoneSize            = node.config.zoneSize;
        
        int peerslen = 0, counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        //PDU TOTAL SIZE
        if(zoneSize == 2)   for(Tuple i: peers) peerslen+=((byte[])(i.x)).length; //SEM HOP DIST
        else for(Tuple i: peers) peerslen+=(((byte[])(i.x)).length + 4); //COM HOP DIST
        int len = 1 + 4 + 4 + id.length + 4 + peerslen; //PDUTYPE  + ZONESIZE + NODEID + SEQNUM + PEERS
        byte[] raw = new byte[len];
        
        //PDU TYPE
        raw[counter++] = 0x00;
        limit++;
        
        //ZONE SIZE
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(zoneSize);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //PDU TOTAL SIZE
        buffer.clear();
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;
        
        //NODEID DATA
        limit+=id.length;
        for(; counter<limit; counter++) {
            raw[counter] = id[it++];
        }
        it = 0;
        
        //SEQUENCE DATA
        limit+=seq.length;
        for(; counter<limit; counter++) {
            raw[counter] = seq[it++];
        }
        it = 0;
        
        if(node.config.zoneSize == 2){ //SEM HOP LEN
            //PEERS ID ARRAY
            for(Tuple entry: peers) {
                limit+=((byte[])(entry.x)).length;
                for(; counter<limit; counter++) {
                    raw[counter] = ((byte[])(entry.x))[it++];
                }
                it = 0;
            }
        }else{ // COM HOP LEN
            for(Tuple entry: peers) {
                limit+=(((byte[])(entry.x)).length + 4);
                
                //HOP DIST
                buffer.clear();
                buffer = ByteBuffer.allocate(4);
                buffer.putInt((int)(entry.y));
                tmp = buffer.array();
                
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                
                //NODE ID DST
                for(; counter<limit; counter++) {
                    raw[counter] = ((byte[])(entry.x))[it++];
                }
                it = 0;
            }
        }
        
        return raw;
    }
    
    public static void loadHello(byte[] raw, Node id, InetAddress origin) {
        
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        int zoneSize            = 0, totalSize = 0, hopDist = 0; //Obtained Variables
        byte[] tmp              = new byte[4];
        byte[] seq_num          = new byte[4];
        byte[] nodeId           = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        ArrayList<Tuple> tuple  = new ArrayList<>();
        Tuple t                 = null;
        
        //Get ZoneSize
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        ByteBuffer wrapped = ByteBuffer.wrap(tmp);
        zoneSize = wrapped.getInt();
        it = 0;
        
        //GET PDU TOTAL SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped.clear();
        wrapped = ByteBuffer.wrap(tmp);
        totalSize = wrapped.getInt();
        it = 0;
        
        //GET NODEID ORIGIN
        limit+=32;
        for(;counter<limit; counter++) nodeId[it++] = raw[counter];
        it = 0;
        
        //GET SEQ DATA
        limit+=4;
        for(;counter<limit; counter++) seq_num[it++] = raw[counter];
        it = 0;
        
        //GET PEERS DATA
        if(zoneSize == 2){
            limit=totalSize;
            for(;counter<limit;){
                while(it<32){
                    nodeId[it++] = raw[counter++];
                }
                it = 0;
                t = new Tuple(1, nodeId.clone()); //Sera que tenho que criar um novo ou o java ja faz o clone???
                tuple.add(t);
            }
        }else{
            limit=totalSize;
            for(;counter<limit;){
                //GET HOP DIST
                while(it<4){
                    tmp[it++] = raw[counter++];
                }
                it = 0;
                wrapped.clear();
                wrapped = ByteBuffer.wrap(tmp);
                hopDist = wrapped.getInt();
                
                //GET NODE ID
                while(it<32){
                    nodeId[it++] = raw[counter++];
                }
                it = 0;
                
                t = new Tuple(hopDist, nodeId.clone()); //Sera que tenho que criar um novo ou o java ja faz o clone???
                tuple.add(t);
            }
        }
        id.addPeerZone(nodeId, origin, seq_num, tuple);
    }
    
    public static void dumpRReq() {
    }
    
    public static void loadRReq(byte[] raw) { 
    }
    
    public static void dumpRRep() {
    }
    
    public static void loadRRep(byte[] raw) { 
    }
    
    public static void dumpRErr() {
    }
    
    public static void loadRErr(byte[] raw) { 
    }
    
    public static void dumpData() {
    }
    
    public static void loadData(byte[] raw) { 
    }
    
}