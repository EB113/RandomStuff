/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.PDU;

import aer.Data.Node;
import aer.miscelaneous.Config;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author pedro
 */
public class DataRequest {
    
    //TYPE+MODE+SEC+GPS+TTL+SRC+DST+REQNUM+[GPSDATASRC]+[PUBK+NODESIG||NULL]+[TIMESTAMPGPS+GPSDATADST||NULL]+[PUBK+NODESIG||NULL]
    public static void load(byte[] raw, Node node, InetAddress peerAddr, Controller control) throws UnknownHostException {
        
        //LOCAL VARIABLES
        long now = System.currentTimeMillis();
        
        //Auxiliary variables
        int counter             = 1, limit = 1, it = 0;
        ByteBuffer wrapped      = ByteBuffer.allocate(8);
        byte[] tmp              = new byte[8];
        
        //EXTRACTED DATA
        byte secure             = 0x00, mode = 0x00, gps = 0x00;
        long TTL                = 0;
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] req_num          = new byte[4];
        
        
        
        //GET MODE BYTE
        mode  =   raw[counter];
        counter++;
        limit++;
        
        //GET SECURITY BYTE
        secure  =   raw[counter];
        counter++;
        limit++;
        
        //GET GPS BYTE
        gps  =   raw[counter];
        counter++;
        limit++;
        
        //GET PDU TIME TO LIVE
        limit+=8;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        wrapped = ByteBuffer.wrap(tmp);
        TTL = wrapped.getLong();
        it = 0;
        
        if(now < TTL){
            
            //GET NODEIDSRC 
            limit+=32;
            for(;counter<limit; counter++) nodeIdSrc[it++] = raw[counter];
            it = 0;

            //GET NODEIDDST 
            limit+=32;
            for(;counter<limit; counter++) nodeIdDst[it++] = raw[counter];
            it = 0;
            
            if(!Crypto.cmpByteArray(node.getId(), nodeIdSrc) && !node.existsDataRequest(nodeIdSrc, nodeIdDst, req_num)) {
                
                //GET REQ_NUM
                limit+=4;
                for(;counter<limit; counter++) req_num[it++] = raw[counter];
                it = 0;
                
                Tuple  tuple            = null;
                    
                //GET SRC POS DATA
                    ArrayList<Double> posData = new ArrayList<>(5);

                    //TIMESTAMP
                    limit+=8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    long timestamp = wrapped.getLong();
                    it = 0;

                    //POSITION
                    limit+=8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    posData.add(wrapped.getDouble());
                    it = 0;
                    limit+=8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    posData.add(wrapped.getDouble());
                    it = 0;

                    //DIRECTION
                    limit+=8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    posData.add(wrapped.getDouble());
                    it = 0;
                    limit+=8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    posData.add(wrapped.getDouble());
                    it = 0;

                    //SPEED
                    limit+=8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    posData.add(wrapped.getDouble());
                    it = 0;

                    tuple = new Tuple(posData, timestamp); 
                    
                    //ADD REQ CACHE
                    boolean exists = node.addDataReq(nodeIdSrc, nodeIdDst, req_num, TTL, null, raw);
                    
                    //VERIFY SRC PENDING REQUESTS
                    ArrayList<byte[]> pendingReqSrc = node.getReqCache(nodeIdSrc, false);
                    if(pendingReqSrc != null)  for(byte[] req : pendingReqSrc) control.pushQueueUDP(new Tuple(req, peerAddr));
                    ArrayList<byte[]> pendingRepSrc = node.getRepCache(nodeIdSrc, false);
                    if(pendingRepSrc != null)  for(byte[] rep : pendingRepSrc) control.pushQueueUDP(new Tuple(rep, peerAddr));
                    
                    //ADD PEER REQ CACHE SRC
                    node.addPeerCache(nodeIdSrc, tuple);
                if(Crypto.cmpByteArray(node.getId(), nodeIdDst)) {
                    
                    System.out.println("--->LocalREQ");
                    //PDU REMOTE REQUEST
                    raw = DataReply.dumpLocal(node, nodeIdSrc, req_num, tuple);

                    //UDPQUEUEPUSH
                    if(raw != null) control.pushQueueUDP(new Tuple(raw, peerAddr));
                }else {
                    
                    System.out.println("--->RemoteREQ");
                    
                    if(gps != 0x00){
                        if(secure != 0x00){
                        
                            counter+=32;//pubk
                            counter+=32;//sig
                            limit=counter;
                        }
                        
                        //GET DST POS DATA
                        posData = new ArrayList<>(5);

                        //TIMESTAMP
                        limit+=8;
                        for(;counter<limit; counter++) tmp[it++] = raw[counter];
                        wrapped = ByteBuffer.wrap(tmp);
                        timestamp = wrapped.getLong();
                        it = 0;

                        //POSITION
                        limit+=8;
                        for(;counter<limit; counter++) tmp[it++] = raw[counter];
                        wrapped = ByteBuffer.wrap(tmp);
                        posData.add(wrapped.getDouble());
                        it = 0;
                        limit+=8;
                        for(;counter<limit; counter++) tmp[it++] = raw[counter];
                        wrapped = ByteBuffer.wrap(tmp);
                        posData.add(wrapped.getDouble());
                        it = 0;

                        //DIRECTION
                        limit+=8;
                        for(;counter<limit; counter++) tmp[it++] = raw[counter];
                        wrapped = ByteBuffer.wrap(tmp);
                        posData.add(wrapped.getDouble());
                        it = 0;
                        limit+=8;
                        for(;counter<limit; counter++) tmp[it++] = raw[counter];
                        wrapped = ByteBuffer.wrap(tmp);
                        posData.add(wrapped.getDouble());
                        it = 0;

                        //SPEED
                        limit+=8;
                        for(;counter<limit; counter++) tmp[it++] = raw[counter];
                        wrapped = ByteBuffer.wrap(tmp);
                        posData.add(wrapped.getDouble());
                        it = 0;
                        
                        //ADD PEER REQ CACHE SRC DST
                        node.addPeerCache(nodeIdDst, tuple);
                    }
                    
                    
                    if(!exists){
                        raw = DataRequest.dumpRemote(raw, node, nodeIdDst);

                        LinkedList<InetAddress> peerList = null;
                        LinkedList<InetAddress> usedPeers = new LinkedList<>();

                        //FORWARDING STATEGIES
                        peerList = node.getZonePeer(nodeIdDst);
                        if(peerList == null) {
                            
                            peerList = node.getPeerCache(nodeIdDst);
                            
                            if(peerList == null) {

                                peerList = node.getOrientUnic(null);
                            }
                        }

                        if(peerList != null){
                            for(InetAddress addr : peerList)
                            {
                                //ADD REQUEST TO CACHE
                                usedPeers.push(addr);

                                //ADD QUEUE
                                control.pushQueueUDP(new Tuple(raw, addr));
                            }
                        }
                    }    
                }
            }
        }else System.out.println("TIMEOUT");
        
        
    }
    
    public static byte[] dumpLocal(Node node, byte[] nodeDst, byte[] seq_num) {
        
        System.out.println("<---LocalREQ");
        
        //GET LOCAL NODE PDU DATA
        byte[]              id        = node.getId();
        byte[]              pubk      = node.getPubKey();
        byte                secure    = node.config.getSecurity();
        byte                mode      = node.config.getMode();
        byte[]              req_num   = seq_num;
        Tuple               posData   = node.getPosData(nodeDst); //<ArrayList<Double>,Long>
        
        //AUXILIARY
        int     len     = 0, counter = 0, limit = 0, it = 0;
        byte[]  tmp     = null;
        
        //TOTAL SIZE
        int posDataLen  = 8+8 + 8+8 + 8 + 8; //POS + DIR + SPEED + TIMESTAMP
        len             = 1 + 1 + 1 + 1 + 8 + id.length + nodeDst.length + req_num.length + posDataLen;
        
        //TYPE+MODE+SEC+GPS+TTL+SRC+DST+REQNUM+[TIMESTAMPGPS+GPSDATASRC]+[PUBK+NODESIG||NULL]+[TIMESTAMPGPS+GPSDATADST||NULL]+[PUBK+NODESIG||NULL]
        if(secure == 0x01)
            len += pubk.length + 32;
        if(posData != null)
            len += posDataLen;
        if(secure == 0x01 && posData != null)
            len += pubk.length + 32; 
        
        //ALOCATE PDU BYTE ARRAY
        byte[] raw = new byte[len];
        
        //PDU TYPE
        raw[counter++] = 0x01;
        limit++;
        
        //PDU MODE
        raw[counter++] = mode;
        limit++;
        
        //PDU SECURITY
        raw[counter++] = secure;
        limit++;
        
        //PDU POSMODE
        if(posData != null)
            raw[counter++] = 0x01;
        else raw[counter++] = 0x00;
        limit++;
        
        //PDU TIME TO LIVE
        long ttl = node.getTTL() + System.currentTimeMillis();
        
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(ttl);
        tmp = buffer.array();
        limit+=8;
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
        limit+=nodeDst.length;
        for(; counter<limit; counter++) {
            raw[counter] = nodeDst[it++];
        }
        it = 0;
        
        //REQUEST SEQUENCE NUM
        limit+=req_num.length;
        for(; counter<limit; counter++) {
            raw[counter] = req_num[it++];
        }
        it = 0;
        
        Tuple   posLocal       = node.getPosition();
        Tuple   dirLocal       = node.getDirection();
        Double  speedLocal     = node.getSpeed();
        long    timestamp      = node.getTimeStamp();
        
            //TIMESTAMP
            buffer.clear();
            buffer.putLong(((long)timestamp));
            tmp = buffer.array();
            limit+=8;
            for(; counter<limit; counter++) {
                raw[counter] = tmp[it++];
            }
            it = 0;
            
            //POSITION
                //X
                buffer.clear();
                buffer.putDouble(((double)posLocal.x));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                //Y
                buffer.clear();
                buffer.putDouble(((double)posLocal.y));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                
            //DIRECTION
                //X
                buffer.clear();
                buffer.putDouble(((double)dirLocal.x));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                //Y
                buffer.clear();
                buffer.putDouble(((double)dirLocal.y));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                
            //SPEED
                //X
                buffer.clear();
                buffer.putDouble(speedLocal);
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
                
        //SE SECURITY ACTIVE
        if(secure == 0x01) {
            
            //GENERATE SIG
            byte[] sig = node.GenerateSignature(raw, limit);

            //PUBKEY
            limit+=pubk.length;
            for(; counter<limit; counter++) {
                raw[counter] = pubk[it++];
            }
            it = 0;
            
            limit+=32;
            for(; counter<limit; counter++) {
                raw[counter] = sig[it++];
            }
            it = 0;
            
        }
                
                
        //SE DSTGPSDATA ACTIVE
        if(posData != null) {
            
            //TIMESTAMP
            buffer.clear();
            buffer.putLong(((long)posData.y));
            tmp = buffer.array();
            limit+=8;
            for(; counter<limit; counter++) {
                raw[counter] = tmp[it++];
            }
            it = 0;
                
            ArrayList<Double> pos = ((ArrayList<Double>)(posData.x));
            //MOVEMENT VALUES
            for(Double data : pos) {
            
                buffer.clear();
                buffer.putDouble(((double)data));
                tmp = buffer.array();
                limit+=8;
                for(; counter<limit; counter++) {
                    raw[counter] = tmp[it++];
                }
                it = 0;
            }
            
            if(secure == 0x01) {
            
                //GENERATE SIG
                byte[] sig = node.GenerateSignature(raw, limit);

                //PUBKEY
                limit+=pubk.length;
                for(; counter<limit; counter++) {
                    raw[counter] = pubk[it++];
                }
                it = 0;

                limit+=32;
                for(; counter<limit; counter++) {
                    raw[counter] = sig[it++];
                }
                it = 0;

            }
        }   
        
        //ADD DATAREQUESTCACHE
        node.addDataReq(node.getId(), nodeDst, seq_num, ttl, null, raw);
        
        return raw;
    }
    
    //TYPE+SEC+GPS+SIZE+TTL+SRC+DST+REQNUM+[GPSDATASRC]+[GPSDATADST||NULL]+TIMESTAMPGPS+[PUBK+NODESIG||NULL]
    public static byte[] dumpRemote(byte[] raw, Node node, byte[] nodeIdDst) {
        
        System.out.println("<---remoteREQ");
        
        byte[]              pubk      = node.getPubKey();
        
        byte[]      tmp     = new byte[8];
        ByteBuffer  wrapped = ByteBuffer.allocate(8);
        
        int counter = 0, limit = 0, it = 0;
        
        //PDU TYPE
        counter++;
        //PDU MODE
        byte mode = raw[counter++];
        
        if(mode != 0x01){
            //SEC OPT
            byte secByte = raw[counter++];
            //GPS
            byte gpsByte = raw[counter++];
            //TTL
            counter+=8;
            //NODE ID SRC + DST
            counter+=64;
            //PDU REQ NUM
            counter+=4; 
            //ORIGIN GPS DATA
            counter+= 8+8 + 8+8 + 8;

            //GETLOCALDESTGPSDATA
            Tuple posData = node.getPosData(nodeIdDst); //<ArrayList<Double>,Long>

            if(secByte != 0x00 || secByte != 0x02) {

                //KEY + SIG
                counter+=64;
            }

            if(posData != null) {

                if(gpsByte != 0x00){

                    //TIMESTAMP
                    limit = counter+8;
                    for(;counter<limit; counter++) tmp[it++] = raw[counter];
                    wrapped = ByteBuffer.wrap(tmp);
                    it = 0;
                    long timestamp = wrapped.getLong();

                    if(timestamp < ((long)posData.y)){

                        if(secByte == 0x00){
                            
                            if(node.config.getSecurity() == 0x01){
                                
                                //NEW BYTEARRAY
                                int gpsLen = 8 +8+8+ 8+8+ 8;
                                int newlen = raw.length + gpsLen + 32+32; //GPSDATA + KEY + SIG
                                byte[] newraw = new byte[newlen];
                                
                                //EXTRACT DATA
                                timestamp = ((long)posData.y);
                                ArrayList<Double> coords = ((ArrayList<Double>)posData.x);

                                limit = counter;
                                for(int i = 0; i<limit; i++)
                                    newraw[i] = raw[i];

                                //TIMESTAMP
                                wrapped.clear();
                                wrapped.putLong(((long)posData.y));
                                tmp = wrapped.array();
                                limit+=8;
                                for(; counter<limit; counter++) {
                                    newraw[counter] = tmp[it++];
                                }
                                it = 0;

                                //MOVEMENT VALUES
                                for(Double data : coords) {

                                    wrapped.clear();
                                    wrapped.putDouble(((double)data));
                                    tmp = wrapped.array();
                                    limit+=8;
                                    for(; counter<limit; counter++) {
                                        newraw[counter] = tmp[it++];
                                    }
                                    it = 0;
                                }
                                
                                //GENERATE SIG
                                byte[] sig = node.GenerateSignature(newraw, limit);

                                //PUBKEY
                                limit+=pubk.length;
                                for(; counter<limit; counter++) {
                                    raw[counter] = pubk[it++];
                                }
                                it = 0;

                                limit+=32;
                                for(; counter<limit; counter++) {
                                    raw[counter] = sig[it++];
                                }
                                it = 0;
                                
                            }else{
                                //NEW BYTEARRAY
                                int gpsLen = 8 +8+8+ 8+8+ 8;
                                int newlen = raw.length + gpsLen; //GPSDATA + KEY + SIG
                                byte[] newraw = new byte[newlen];
                                
                                //EXTRACT DATA
                                timestamp = ((long)posData.y);
                                ArrayList<Double> coords = ((ArrayList<Double>)posData.x);

                                limit = counter;
                                for(int i = 0; i<limit; i++)
                                    newraw[i] = raw[i];

                                //TIMESTAMP
                                wrapped.clear();
                                wrapped.putLong(((long)posData.y));
                                tmp = wrapped.array();
                                limit+=8;
                                for(; counter<limit; counter++) {
                                    newraw[counter] = tmp[it++];
                                }
                                it = 0;

                                //MOVEMENT VALUES
                                for(Double data : coords) {

                                    wrapped.clear();
                                    wrapped.putDouble(((double)data));
                                    tmp = wrapped.array();
                                    limit+=8;
                                    for(; counter<limit; counter++) {
                                        newraw[counter] = tmp[it++];
                                    }
                                    it = 0;
                                }
                            }

                        }else{

                            //TIMESTAMP
                            wrapped.clear();
                            wrapped.putLong(((long)posData.y));
                            tmp = wrapped.array();
                            limit+=8;
                            for(; counter<limit; counter++) {
                                raw[counter] = tmp[it++];
                            }
                            it = 0;

                            ArrayList<Double> pos = ((ArrayList<Double>)(posData.x));
                            //MOVEMENT VALUES
                            for(Double data : pos) {

                                wrapped.clear();
                                wrapped.putDouble(((double)posData.x));
                                tmp = wrapped.array();
                                limit+=8;
                                for(; counter<limit; counter++) {
                                    raw[counter] = tmp[it++];
                                }
                                it = 0;
                            }

                            
                            //GENERATE SIG
                            byte[] sig = node.GenerateSignature(raw, limit);

                            //PUBKEY
                            limit+=pubk.length;
                            for(; counter<limit; counter++) {
                                raw[counter] = pubk[it++];
                            }
                            it = 0;

                            limit+=32;
                            for(; counter<limit; counter++) {
                                raw[counter] = sig[it++];
                            }
                            it = 0;
                        }
                    }
                }else {
                    
                    int gpsLen = 8 +8+8+ 8+8+ 8;
                    int newlen = raw.length + gpsLen + 32+32; //GPSDATA + KEY + SIG
                    byte[] newraw = new byte[newlen];
                    
                    //EXTRACT DATA
                    long timestamp = ((long)posData.y);
                    ArrayList<Double> coords = ((ArrayList<Double>)posData.x);
                    
                    limit = counter;
                    for(int i = 0; i<limit; i++)
                        newraw[i] = raw[i];
                    
                    //TIMESTAMP
                    wrapped.clear();
                    wrapped.putLong(((long)posData.y));
                    tmp = wrapped.array();
                    limit+=8;
                    for(; counter<limit; counter++) {
                        newraw[counter] = tmp[it++];
                    }
                    it = 0;

                    //MOVEMENT VALUES
                    for(Double data : coords) {

                        wrapped.clear();
                        wrapped.putDouble(((double)data));
                        tmp = wrapped.array();
                        limit+=8;
                        for(; counter<limit; counter++) {
                            newraw[counter] = tmp[it++];
                        }
                        it = 0;
                    }
                    
                    //COM SECURE
                    if(node.config.getSecurity() == 0x01){
                        
                        if(secByte == 0x00){
                            newraw[1] = 0x02;
                            newraw[2] = 0x02;
                        }else {
                            newraw[1] = 0x03;
                            newraw[2] = 0x02;
                        }
                        
                        //GENERATE SIG
                        byte[] sig = node.GenerateSignature(raw, limit);

                        //PUBKEY
                        limit+=pubk.length;
                        for(; counter<limit; counter++) {
                            newraw[counter] = pubk[it++];
                        }
                        it = 0;

                        limit+=32;
                        for(; counter<limit; counter++) {
                            newraw[counter] = sig[it++];
                        }
                        it = 0;
                    }
                    
                    return newraw;
                }
            }
        }
        
        return raw;
    }
    
}
