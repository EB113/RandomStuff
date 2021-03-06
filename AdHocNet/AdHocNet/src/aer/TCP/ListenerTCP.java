/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.TCP;

import aer.Data.Node;
import aer.PDU.Data;
import aer.PDU.RErr;
import aer.PDU.RRep;
import aer.PDU.RReq;
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
                                byte[] req_pdu = RReq.dumpLocal(this.id, nodeIdDst, this.config.getHopLimit());
                                byte[] req_num = this.id.getReqNum();


                                //ROUTE REQUEST SEND

                                Tuple peer = id.getZonePeer(nodeIdDst); //Check Zone
                                if(peer == null) peer = id.getHitPeer(nodeIdDst); //Check Hit
                                
                                if(peer != null) { //Se esta na ZONE TOPOLOGY ou Hit Cache
                                    
                                    System.out.println("INZONE");
                                    //ADD REQUEST TO CACHE
                                    usedPeers.push((InetAddress)peer.y);
                                    id.addReqCache(usedPeers, null, this.id.getId(), nodeIdDst, 0, this.config.getHopLimit(), req_num, null);

                                    //Criar Queue e ficar a espera
                                    this.control.pushQueueUDP(new Tuple(req_pdu, peer.y));

                                    //WAIT REPLY
                                    coms = this.control.popQueueTCP(new ByteArray(req_num), this.id.getPubKey());

                                    //SE NAO TEM RESPOSTA TENTAR TODOS OS OUTROS NODOS


                                    Object obj = null;
                                    if(coms != null){
                                        
                                        try {
                                            obj = coms.getComs().poll(30, TimeUnit.SECONDS);

                                            //imp!!!!!!!!!!!!!!!!!!!!!!!
                                            //imp!!!!!!!!!!!!!!!!!!!!!!!
                                            //imp!!!!!!!!!!!!!!!!!!!!!!!
                                            //TRATAR DE DEVOLVER RESPOSTA
                                            if(obj != null){
                                                //TROCAR HOP VALUES depois para ja estatico
                                                byte[] dataReq = Data.dumpLocal(nodeIdDst, this.config.getHopLimit(), this.id, (byte)0x00, req_num);
                                                //Criar Queue e ficar a espera
                                                this.control.pushQueueUDP(new Tuple(dataReq, (InetAddress)obj)); 

                                                //WAIT REPLY E necessario???
                                                //coms = this.control.popQueueTCP(new ByteArray(req_num), this.id.getPubKey());

                                                if(coms != null){
                                                    
                                                    try {
                                                        obj = coms.getComs().poll(50, TimeUnit.SECONDS);

                                                        if(obj != null){
                                                            
                                                            news = new String(coms.getData(), StandardCharsets.UTF_8);
                                                        }
                                                    } catch (InterruptedException ex) {
                                                        System.out.println("1TCP request TIMEOUT");
                                                    }
                                                }
                                            }
                                            //imp!!!!!!!!!!!!!!!!!!!!!!!
                                            //imp!!!!!!!!!!!!!!!!!!!!!!!
                                            //imp!!!!!!!!!!!!!!!!!!!!!!!
                                        } catch (InterruptedException ex) {
                                            System.out.println("1TCP request TIMEOUT");
                                        } 

                                    }else System.out.println("NULL COMS!");

                                }else { // SE NAO ESTA NA ZONE TOPOLOGY ou Hit Cache
                                System.out.println("OUTZONE");

                                    LinkedList<InetAddress> peerList = id.getReqRankPeers(null, null);
                                    if(peerList != null){

                                        //SEND REQUEST mELHOR nODOS
                                        id.addReqCache(usedPeers, null, this.id.getId(), nodeIdDst, 0, this.config.getHopLimit(), req_num, null);
                                        for(InetAddress addr : peerList)
                                        {
                                            //ADD REQUEST TO CACHE
                                            usedPeers.push(addr);

                                            //mandar para a Queue
                                            this.control.pushQueueUDP(new Tuple(req_pdu, addr));
                                        }

                                        ComsTCP coms = null;
                                        //WAIT REPLY
                                        coms = this.control.popQueueTCP(new ByteArray(req_num), this.id.getPubKey());


                                        //SE NAO TEM RESPOSTA TENTAR TODOS OS OUTROS
                                        Object obj = null;
                                        if(coms != null){
                                            try {
                                                obj = coms.getComs().poll(30, TimeUnit.SECONDS);

                                                //imp!!!!!!!!!!!!!!!!!!!!!!!
                                                //imp!!!!!!!!!!!!!!!!!!!!!!!
                                                //imp!!!!!!!!!!!!!!!!!!!!!!!
                                                //TRATAR DE DEVOLVER RESPOSTA
                                                if(obj != null){
                                                    //TROCAR HOP VALUES depois para ja estatico
                                                    byte[] dataReq = Data.dumpLocal(nodeIdDst, this.config.getHopLimit(), this.id, (byte)0x00, req_num);
                                                    //Criar Queue e ficar a espera
                                                    this.control.pushQueueUDP(new Tuple(dataReq, (InetAddress)obj)); 

                                                    //WAIT REPLY
                                                    //coms = this.control.popQueueTCP(new ByteArray(req_num), this.id.getPubKey());

                                                    if(coms != null){
                                                        
                                                        try {
                                                            obj = coms.getComs().poll(50, TimeUnit.SECONDS);

                                                            if(obj != null){
                                                                
                                                                news = new String(coms.getData(), StandardCharsets.UTF_8);
                                                            }
                                                        } catch (InterruptedException ex) {
                                                            System.out.println("1TCP request TIMEOUT");
                                                        }
                                                    }
                                                }
                                                //imp!!!!!!!!!!!!!!!!!!!!!!!
                                                //imp!!!!!!!!!!!!!!!!!!!!!!!
                                                //imp!!!!!!!!!!!!!!!!!!!!!!!
                                            } catch (InterruptedException ex) {
                                                System.out.println("1TCP request TIMEOUT");
                                            }
                                        }
                                        //DATA REQUEST
                                        //byte[] out_data_pdu = Data.dumpLocal();
                                    }else {
                                        System.out.println("No Zone Peers");
                                        outToClient.writeBytes("No Zone Peers" + '\n');
                                    }
                                }

                                //RETURN DATA

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
