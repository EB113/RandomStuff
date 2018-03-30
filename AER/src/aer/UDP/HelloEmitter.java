/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import PDU.Hello;
import aer.Data.Node;
import aer.miscelaneous.Config;
import aer.miscelaneous.Controller;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloEmitter implements Runnable{
    private Controller  control;
    private Config      config;
    private Node        id;
    private Boolean     bool;
    
    
    DatagramSocket ds;
    byte[] b;
    DatagramPacket dp;
    
    public HelloEmitter(Controller control, Config config, Node id) throws SocketException {
        this.control    = control;
        this.config     = config;
        this.id         = id;
        this.bool       = this.control.getUDPFlag().get();
        this.ds         = new DatagramSocket();
    }    
    
    @Override
    public void run(){
        
        while(true){
            
            synchronized(this.control){
                this.bool = this.control.getUDPFlag().get();
            }
            if(this.bool){
                try {
                    byte[] raw = Hello.dump(id);
                    
                    this.dp = new DatagramPacket(raw, raw.length, InetAddress.getLocalHost(), 9999);
                    this.ds.send(dp);
                    
                } catch (SocketException ex) {
                    Logger.getLogger(UDPQueue.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UDPQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else return;
            
            try {
                TimeUnit.SECONDS.sleep(config.getHelloTimer());
            } catch (InterruptedException ex) {
                Logger.getLogger(UDPQueue.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
}