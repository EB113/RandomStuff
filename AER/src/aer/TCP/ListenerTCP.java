/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.TCP;

import aer.Data.Node;
import aer.PDU.RErr;
import aer.PDU.RRep;
import aer.PDU.RReq;
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
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class ListenerTCP implements Runnable{

    private Controller  control;
    private Config config;
    private Node id;
    ServerSocket s1;
    Socket ss;
    
    public ListenerTCP(Controller control, Config config, Node id) throws SocketException {
       this.control = control;
       this.config = config;
       this.id = id;
       
       try {
          this.s1 = new ServerSocket(9999);
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            Logger.getLogger(ListenerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        try {
        String clientSentence;
         while (true) {
            Socket connectionSocket = this.s1.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            System.out.println("Received: " + clientSentence);
           
            byte[] nodeIdDst = Crypto.toBytes(clientSentence);
            LinkedList<InetAddress> usedPeers = new LinkedList<>(); 
            
            //byte[] bit = RReq.dumpLocal(Crypto.toBytes(clientSentence), this.config.getHopLimit(), this.id, (byte)0);
            byte[] pdu = RReq.dumpLocal(this.id, nodeIdDst, this.config.getHopLimit());
            
           
            Tuple peer = id.getZonePeer(nodeIdDst); //Check Zone
            if(peer == null) 
                peer = id.getHitPeer(nodeIdDst); //Check Hit
            
            if(peer != null) { //Se esta na ZONE TOPOLOGY ou Hit Cache
                //ADD REQUEST TO CACHE
                usedPeers.push((InetAddress)peer.x);
                id.addReqCache(usedPeers, null, this.id.getId(), nodeIdDst, 0, this.id.getReqNum());
                
                //mandar para a Queue
                this.control.pushQueueUDP(new Tuple(pdu, peer.x));

            }else { // SE NAO ESTA NA ZONE TOPOLOGY ou Hit Cache
                LinkedList<InetAddress> peerList = id.getReqPeers(); 
                
                id.addReqCache(usedPeers, null, this.id.getId(), nodeIdDst, 0, this.id.getReqNum());
                for(InetAddress addr : peerList)
                {
                    //ADD REQUEST TO CACHE
                    usedPeers.push(addr);
                    
                    //mandar para a Queue
                    this.control.pushQueueUDP(new Tuple(pdu, addr));
                }
                
            }
           
            
            String News = "Noticias do Jornal Nacional Construtores dizem que são precisos mais 80 a 100 mil operários para suportar o acréscimo de produção de 4,5% previsto para este ano. Sindicatos reclamam melhores salários" + '\n';
            outToClient.writeBytes(News);
  }
        } catch (IOException ex) {
            Logger.getLogger(ListenerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
