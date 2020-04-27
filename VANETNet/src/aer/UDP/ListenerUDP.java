/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.Data.Node;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author pedro
 */
public class ListenerUDP implements Runnable{
    
    //Configs
    private Controller control;
    private Node id;
    //Sockets
    private DatagramSocket ds;
    private DatagramPacket dp;
    
    public ListenerUDP(Controller control, Node id) {
        this.control    = control;
        this.id         = id;
        
        try {
            this.ds         = new DatagramSocket(9999);
            this.ds.setSoTimeout(1000);
        } catch (SocketException ex) {
            Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
            
        while(true){
            
            if(this.control.getUDPFlag().get()){
                try {
                    byte[] b1 = new byte[1024]; // Size to Fix

                    this.dp = new DatagramPacket(b1, b1.length);
                    this.ds.receive(dp);
                    
                    //NEW PDU
                    byte[] data = new byte[dp.getLength()];
                    System.arraycopy(dp.getData(), dp.getOffset(), data, 0, dp.getLength());
                     
                    (new Thread(new Interpreter(control, this.id, data, dp.getAddress()))).start();
                    
                } catch (SocketException ex) {
                    //Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
                    //What to do????
                } catch (IOException ex) {
                    //Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
                    //What to do???? receive method raises exception
                }
            }else return;
            /*
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        
    }
}