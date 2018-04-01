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
    private Boolean bool;
    //Sockets
    private DatagramSocket ds;
    private DatagramPacket dp;
    
    public ListenerUDP(Controller control, Node id) {
        this.control    = control;
        this.id         = id;
        this.bool       = this.control.getUDPFlag().get();
        
        try {
            this.ds         = new DatagramSocket(9999);
            this.ds.setSoTimeout(1000);
        } catch (SocketException ex) {
            Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
            
        while(true){
            
            synchronized(this.control){
                this.bool = this.control.getUDPFlag().get();
            }
            if(this.bool){
                try {
                    byte[] b1 = new byte[1024]; // Size to Fix

                    this.dp = new DatagramPacket(b1, b1.length);
                    this.ds.receive(dp);
                    (new Thread(new Interpreter(control, this.id, dp.getData(), dp.getAddress()))).start();
                    
                } catch (SocketException ex) {
                    //Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
                    //What to do????
                } catch (IOException ex) {
                    //Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
                    //What to do???? receive method raises exception
                }
            }else return;
            
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}