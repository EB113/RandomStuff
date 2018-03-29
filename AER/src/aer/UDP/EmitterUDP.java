/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

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
    DatagramSocket ds;
    String i;
    byte[] b;
    InetAddress ia;
    DatagramPacket dp;
    byte[] b1;
    DatagramPacket dp1;
    String str;
    public EmitterUDP() throws SocketException {
    }    
    
    @Override
    public void run(){
        
        while(true){
        try {
            this.ds = new DatagramSocket();
            this.i = "Hello";
            this.b = i.getBytes();
        
            this.ia = InetAddress.getLocalHost();
            this.dp = new DatagramPacket(b, b.length, ia, 9999);
            this.ds.send(dp);
        
            this.b1 = new byte[1024];
            this.dp1 = new DatagramPacket(b1, b1.length);
            this.ds.receive(dp1);
        
            this.str = new String(dp1.getData(),0,dp1.getLength());
            System.out.println("Mensagem do Listener foi: "+ str);
            TimeUnit.SECONDS.sleep(5);

        } catch (SocketException ex) {
            Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
       


// for(int i = 0 ; i < 6; i++)
       //   System.out.println("Hello " + i);
    } 
    }
}

//tread que vai escrever na tread
//envia o Hello