/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.TCP;

import aer.Data.Node;
import aer.PDU.DataRequest;
import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class ListenerTCP implements Runnable{
    private ComsTCP coms;
    private Controller  control;
    private Config config;
    private Node id;
    ServerSocket s1;
    Socket ss;
    String clientSentence;
    
    public ListenerTCP(Controller control, Config config, Node id) throws SocketException {
       
        this.coms       = null;
        this.control    = control;
        this.config     = config;
        this.id         = id;
       
        try {
           this.s1 = new ServerSocket(9999);
           //this.s1.setSoTimeout(1500);
             //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
             Logger.getLogger(ListenerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        try {
            this.ss = this.s1.accept();
        } catch (IOException ex) {
            Logger.getLogger(ListenerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        String news = "ALGO ESTÁ MAL";
         
            while (true) {

                if(this.control.getTCPFlag().get()){
                    try {
                        
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.ss.getInputStream()));
                        DataOutputStream outToClient = new DataOutputStream(this.ss.getOutputStream());
                        
                        clientSentence = inFromClient.readLine();
                        if(clientSentence.equals("exit")) {
                            
                            this.control.setUDPFlag(false);
                            this.control.setTCPFlag(false);
                            this.control.setWatchDogFlag(false);
                            
                            inFromClient.close();
                            outToClient.close();
                            
                            return;
                        }else {
                            
                            //ROUTE REQUEST DATA
                            byte[] nodeIdDst = Crypto.toBytes(clientSentence);
                            if(Crypto.cmpByteArray(nodeIdDst, this.id.getId())){
                            
                                outToClient.writeBytes("SOU EU HOMEM!" + "\n");
                            }else {
                                
                                LinkedList<InetAddress> usedPeers = new LinkedList<>();
                                
                                //GET REQ_NUM AND INCREMENT
                                byte[] seq_num = this.id.getReqNum();
                                this.id.incReqNum();
                                
                                //GETPDU DATA INSIDE ADDS REQUEST TO CACHE
                                byte[] req_pdu = DataRequest.dumpLocal(this.id, nodeIdDst, seq_num);

                                LinkedList<InetAddress> peerList = null;
                                
                                int[] mode = {0,0,0,0};
                                int i = 0;
                                
                                Boolean used = false;
                                while(i<4) {
                                
                                    switch((mode[i++])++){
                                        
                                        //ZONE REQUEST
                                        case 0:
                                            peerList = id.getZonePeer(nodeIdDst);
                                            
                                            if(peerList != null) {
                                                
                                                used = true;
                                                
                                                //SEND REQUESTS
                                                for(InetAddress addr : peerList)
                                                {
                                                    //ADD REQUEST TO CACHE
                                                    usedPeers.push(addr);

                                                    //ADD QUEUE
                                                    this.control.pushQueueUDP(new Tuple(req_pdu, addr));
                                                }
                                            }else used = false;
                                            
                                            break;
                                            
                                        //PEERCACHE REQUEST
                                        case 1:
                                            peerList = id.getPeerCache(nodeIdDst);
                                            
                                            if(peerList != null) {
                                                
                                                used = true;
                                                
                                                //SEND REQUESTS
                                                for(InetAddress addr : peerList)
                                                {
                                                    //ADD REQUEST TO CACHE
                                                    usedPeers.push(addr);

                                                    //ADD QUEUE
                                                    this.control.pushQueueUDP(new Tuple(req_pdu, addr));
                                                }
                                            }else used = false;
                                            
                                            break;
                                            
                                        //ORIENTATIONAL UNICAST REQUEST    
                                        case 2:
                                            peerList = id.getOrientUnic(usedPeers);
                                            
                                            if(peerList != null) {
                                                
                                                used = true;
                                                
                                                //SEND REQUESTS
                                                for(InetAddress addr : peerList)
                                                {
                                                    //ADD REQUEST TO CACHE
                                                    usedPeers.push(addr);

                                                    //ADD QUEUE
                                                    this.control.pushQueueUDP(new Tuple(req_pdu, addr));
                                                }
                                            }else used = false;
                                            
                                            break;
                                            
                                        //MULTICAST REQUEST
                                        case 3:
                                            used = true;
                                            this.control.pushQueueUDP(new Tuple(req_pdu, (InetAddress)(InetAddress.getByName("FF02::1")))); 
                                            break;
                                        default:
                                            break;
                                    }
                                    if(!used) continue;
                                    
                                    //WAIT REPLY
                                    coms = this.control.popQueueTCP(new ByteArray(id.getReqNum()), this.id.getPubKey());
                                    Object obj = null;
                                    if(coms != null){
                                        try {
                                            obj = coms.getComs().poll(3, TimeUnit.SECONDS);
                                            if(obj != null){
                                               news = new String(coms.getData(), StandardCharsets.UTF_8);
                                               break;
                                            }
                                        } catch (InterruptedException ex1) {
                                            System.out.println("TIMEOUT!");
                                        }
                                    }
                                
                                }
                                outToClient.writeBytes(news + "\n");
                                news = "ALGO ESTÁ MAL";
                            }
                        }
                    } catch (IOException ex) {
                    }
                }else return;
            }
    }
                     
                                
}