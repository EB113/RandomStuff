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

/**
 *
 * @author pedro
 */
public class Hello {
    public static byte[] dump(Node node) {
        
        byte[] id               = node.getId();
        //byte[] seq              = node.getSeqNum();
        //int zoneSizeHello       = node.config.getZoneSizeHello();
        
        
        int posData = 0, counter=0, limit = 0, it = 0;
        byte[] tmp = null;
        
        posData = 8+8 + 8+8 + 8; //POS + DIR + SPEED
        
        //PDU TOTAL SIZE
        int len = 1 + 4 + id.length + posData; //PDUTYPE+PDUTOTALSIZE+NODEID+POSDATA
        byte[] raw = new byte[len];
        
        //PDU TYPE
        raw[counter++] = 0x00;
        limit++;
        /*
        //PDU TOTAL SIZE
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(len);
        tmp = buffer.array();
        limit+=4;
        for(; counter<limit; counter++) {
            raw[counter] = tmp[it++];
        }
        it = 0;*/
        
        //NODEID DATA
        limit+=id.length;
        for(; counter<limit; counter++) {
            raw[counter] = id[it++];
        }
        it = 0;
        
        //POSITION DATA
        Tuple pos       = node.getPosition();
        Tuple dir       = node.getDirection();
        Double speed    = node.getSpeed();
        
            //POSITION
                //X
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.putDouble(((double)pos.x));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                //Y
                buffer.clear();
                buffer.putDouble(((double)pos.y));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                
            //DIRECTION
                //X
                buffer.clear();
                buffer.putDouble(((double)dir.x));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                //Y
                buffer.clear();
                buffer.putDouble(((double)dir.y));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                
            //SPEED
                //X
                buffer.clear();
                buffer.putDouble(speed);
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
        
        
        return raw;
    }
    
    public static void load(byte[] raw, Node node, InetAddress origin, Controller control) {
        
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        //int zoneSizeHello       = 0, totalSize = 0, hopDist = 0; //Obtained Variables
        byte[] tmp              = new byte[8];
        //byte[] seq_num          = new byte[4];
        byte[] nodeId           = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        //byte[] nodeIdpeer       = new byte[32];
        //ArrayList<Tuple> tuple  = new ArrayList<>();
        Tuple position          = null, direction = null;
        Double speed            = 0.0, x = 0.0, y = 0.0;
        
        
        
        //GET NODEID ORIGIN
        limit+=32;
        for(;counter<limit; counter++) nodeId[it++] = raw[counter];
        it = 0;
        
        //POSITION DATA
        
            //POSITION
            limit+=8;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];

            ByteBuffer  wrapped = ByteBuffer.wrap(tmp);
            x = wrapped.getDouble();
            it = 0;
        
            limit+=8;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];

            wrapped.clear();
            wrapped = ByteBuffer.wrap(tmp);
            y = wrapped.getDouble();
            it = 0;       
        
            position = new Tuple(x,y);
            
            //DIRECTION
            limit+=8;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];

            wrapped.clear();
            wrapped = ByteBuffer.wrap(tmp);
            x = wrapped.getDouble();
            it = 0;
        
            limit+=8;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];

            wrapped.clear();
            wrapped = ByteBuffer.wrap(tmp);
            y = wrapped.getDouble();
            it = 0;       
        
            direction = new Tuple(x,y);
            
            //SPEED
            limit+=8;
            for(;counter<limit; counter++) tmp[it++] = raw[counter];

            wrapped.clear();
            wrapped = ByteBuffer.wrap(tmp);
            speed = wrapped.getDouble();
            it = 0;
            
        if(!Crypto.cmpByteArray(nodeId, node.getId())){
            
            if(!node.existsZonePeer(nodeId)){
                
                //VERIFY IF PENDING REQUESTS
                ArrayList<byte[]> reqs = node.getReqCache(nodeId, true);
                ArrayList<byte[]> reps = node.getRepCache(nodeId, true);
                        
                if(reqs != null)
                    for(byte[] req : reqs){
                        control.pushQueueUDP(new Tuple(req, origin));
                    }

                if(reps != null)
                    for(byte[] rep : reps){
                        control.pushQueueUDP(new Tuple(rep, origin));
                    }
            }
            
            node.addPeerZone(nodeId, origin, position, direction, speed);
            
        }
        
        
        
        
        
        /*
        //GET PDU TOTAL SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        
        ByteBuffer  wrapped = ByteBuffer.wrap(tmp);
        totalSize = wrapped.getInt();
        it = 0;
        
        //Get ZoneSize
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped.clear();
        wrapped = ByteBuffer.wrap(tmp);
        zoneSizeHello = wrapped.getInt();
        it = 0;
        */
        
        
        
        
        /*
        //GET SEQ DATA
        limit+=4;
        for(;counter<limit; counter++) seq_num[it++] = raw[counter];
        it = 0;
        
        
        if(!(Crypto.cmpByteArray(nodeId, id.getId())) && !Crypto.cmpByteArray(id.getPeerSeqNum(nodeId),seq_num)){
            
            
            //GET PEERS DATA
            if(zoneSizeHello == 1){
                
                limit=totalSize;
                for(;counter<limit;){
                    while(it<32){
                        nodeIdpeer[it++] = raw[counter++];
                    }
                    it = 0;
                    if(!(Crypto.cmpByteArray(nodeIdpeer, id.getId()))){
                        
                        t = new Tuple(2, nodeIdpeer.clone()); //Sera que tenho que criar um novo ou o java ja faz o clone???
                        tuple.add(t);
                    }
                }
            }else{
                System.out.println("SIZE>1");
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
                        nodeIdpeer[it++] = raw[counter++];
                    }
                    it = 0;
                    
                    if(!(Crypto.cmpByteArray(nodeIdpeer, id.getId()))){
                        
                        t = new Tuple(hopDist+1, nodeIdpeer.clone()); //Sera que tenho que criar um novo ou o java ja faz o clone???
                        tuple.add(t);
                    }
                }
            }

            id.addPeerZone(nodeId, origin, seq_num, tuple);
        }else System.out.println("JA TENHO OU SOU EU");*/
        
    }
}
