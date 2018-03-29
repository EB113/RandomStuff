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
public class ListenerUDP implements Runnable{

    public ListenerUDP() throws SocketException {
    }
    
    public void run(){
         
        try {
            DatagramSocket ds = new DatagramSocket(9999);
              
            byte[] b1 = new byte[1024];
    
            DatagramPacket dp = new DatagramPacket(b1, b1.length);
            ds.receive(dp);
            String str = new String(dp.getData(),0,dp.getLength());
            System.out.println("Mensagem do Emiter foi: "+ str);
            //int num = Integer.parseInt(str.trim());
            //System.out.println("Num "+ num);
            //int result = num * num;
            String strr = "Hello tmb";
            byte[] b2 = strr.getBytes();
            InetAddress ia = InetAddress.getLocalHost();
            DatagramPacket dp1 = new DatagramPacket(b2, b2.length, ia, dp.getPort());
            ds.send(dp1);
            
//    for(int i = 6 ; i < 11; i++)
//System.out.println("Hello " + i);
        } catch (SocketException ex) {
            Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
        
        } 
    }
}

//Tread que vai ler o que está no socket
//escreve no ecra o Hello
//meter ambos a falar para o mesmo sitio, para o local host