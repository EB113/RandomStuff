/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.miscelaneous.Controller;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author pedro
 */
public class EmitterUDP implements Runnable{
    private Controller control;
    
    DatagramSocket ds;
    String i;
    byte[] b;
    InetAddress ia;
    DatagramPacket dp;
    byte[] b1;
    DatagramPacket dp1;
    String str;
    
    public EmitterUDP(Controller control) throws SocketException {
        this.control = control;
    }    
    
    @Override
    public void run(){
        
        while(true){
            synchronized(this.control){
                if(this.control.getUDPFlag().get()){
                    try {
                        this.ds = new DatagramSocket();
                        this.i = "Hello";
                        this.b = i.getBytes();

                        this.ia = InetAddress.getLocalHost();
                        this.dp = new DatagramPacket(b, b.length, ia, 9999);
                        this.ds.send(dp);
                    } catch (SocketException ex) {
                        Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else return;
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
}

//tread que vai escrever na tread
//envia o Hello