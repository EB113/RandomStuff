/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 *
 * @author pedro
 */
public class ListenerUDP implements Runnable{

    public ListenerUDP() throws SocketException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void run(){
     System.out.println("Hello");
    } 
}

//Tread que vai ler o que está no socket
//escreve no ecra o Hello
//meter ambos a falar para o mesmo sitio, para o local host