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
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author pedro
 */
public class EmitterUDP implements Runnable{

    public EmitterUDP() throws SocketException {
    }

    public void run(){
        
        DatagramSocket ds;
        try {
            ds = new DatagramSocket();
            String i = "Hello";
            byte[] b = i.getBytes();
        
            InetAddress ia = InetAddress.getLocalHost();
            DatagramPacket dp = new DatagramPacket(b, b.length, ia, 9999);
            ds.send(dp);
        
            byte[] b1 = new byte[1024];
            DatagramPacket dp1 = new DatagramPacket(b1, b1.length);
            ds.receive(dp1);
        
            String str = new String(dp1.getData(),0,dp1.getLength());
            System.out.println("Mensagem do Listener foi: "+ str);
            
        } catch (SocketException ex) {
            Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmitterUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        



// for(int i = 0 ; i < 6; i++)
       //   System.out.println("Hello " + i);
    } 
}

//tread que vai escrever na tread
//envia o Hello