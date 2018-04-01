/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.Data.Node;
import aer.miscelaneous.Config;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hello implements Runnable{
    private Controller  control;
    private Config      config;
    private Node        id;
    private Boolean     bool;
    
    
    private DatagramSocket ds;
    private DatagramPacket dp;
    
    public Hello(Controller control, Config config, Node id) throws SocketException {
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
                    byte[] raw = aer.PDU.Hello.dump(id);
                    this.dp = new DatagramPacket(raw, raw.length, InetAddress.getByName("127.0.0.1"), 9999);//FF02::1
                    this.ds.send(dp);
                    
                } catch (SocketException ex) {
                    Logger.getLogger(Hello.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Hello.class.getName()).log(Level.SEVERE, null, ex);
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