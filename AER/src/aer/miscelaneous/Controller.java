/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import aer.Data.Node;
import aer.TCP.ComsTCP;
import java.net.InetAddress;
import java.security.Key;
import java.security.PublicKey;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class Controller {
    private Node                        id;
    private AtomicBoolean               watchDogFlag;
    private AtomicBoolean               UDPFlag;        
    private AtomicBoolean               TCPFlag; 
    private PriorityQueue               queueUDP;
    private HashMap<ByteArray, ComsTCP> queueTCP;
            
    public Controller(int queueSize, Node id) {
        this.id            = id;
        this.watchDogFlag  = new AtomicBoolean(true); //Flag para termino da THread
        this.UDPFlag       = new AtomicBoolean(true); //Flag para termino da THread
        this.TCPFlag       = new AtomicBoolean(true); //Flag para termino da THread
        
        this.queueUDP      = new PriorityQueue<Object>(queueSize, 
                                        new Comparator<Object>() {
                                            @Override
                                            public int compare(Object o1, Object o2) {
                                                
                                                byte o1_type = ((byte[])((Tuple)o1).x)[0];
                                                byte o2_type = ((byte[])((Tuple)o1).x)[0];
                                                
                                                if(o1_type == o2_type){
                                                    return 0;
                                                } else if(o1_type == 0x00){
                                                    return -1;
                                                } else if(o2_type == 0x00) {
                                                    return 1;
                                                }else return 0;
                                            }
                                        }); //fILA the PDU Objects
        this.queueTCP      = new HashMap<>();//Coms com TCP
    }
    
    //NOTA: VERIFDICAR A CLASS DE TODOS OS OBJECTOS RECEBVIDOS NOS SETS 
    
    public ComsTCP popQueueTCP(ByteArray req_num, byte[] pubk) {
        if(this.queueTCP.containsKey(req_num)) return null;
        
        ComsTCP coms = new ComsTCP(pubk);
        this.queueTCP.put(req_num, coms);
        
        return coms;
    }
    
    public void pushQueueTCP(byte[] data, ByteArray req_num, InetAddress addr, byte[] peer_pubk) {
        //SE PEDIDO PERDIDO
        if(!this.queueTCP.containsKey(req_num)) return; 
        
        //VERIFICACAO DE PDU TYPE
        ComsTCP coms = this.queueTCP.get((ByteArray)req_num);
        
        if(data == null) {//SE ROUTE REPLY
            
            if(peer_pubk == null) {
            
                coms.setAddr(addr);
            }else {
                /*
                coms.setPeer_pubk(peer_pubk);
                coms.setShared_key(this.id.getShared(new Key(peer_pubk)));*/
            }
            try {
                coms.getComs().put(addr);
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else {// SE DATA
            
        }
    }
    

    public Object popQueueUDP() {
        return queueUDP.poll();
    }
    
    public void pushQueueUDP(Object obj) {
        this.queueUDP.add(obj);
    }

    public AtomicBoolean getWatchDogFlag() {
        synchronized(this.watchDogFlag){
            return this.watchDogFlag;
        }
    }

    public void setWatchDogFlag(Boolean watchDogFlag) {
        synchronized(this.watchDogFlag){
            this.watchDogFlag.set(watchDogFlag);
        }
    }

    public AtomicBoolean getUDPFlag() {
        synchronized(this.UDPFlag){
            return this.UDPFlag;
        }
    }

    public void setUDPFlag(Boolean UDPFlag) {
        synchronized(this.UDPFlag){
            this.UDPFlag.set(UDPFlag);
        }
    }

    public AtomicBoolean getTCPFlag() {
        synchronized(this.TCPFlag){
            return this.TCPFlag;
        }
    }

    public void setTCPFlag(Boolean TCPFlag) {
        synchronized(this.TCPFlag){
            this.TCPFlag.set(TCPFlag);
        }
    }
    
}
