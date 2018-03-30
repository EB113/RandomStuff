/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.Data.Node;
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
public class ListenerUDP implements Runnable{
    private Controller control;
    private Node id;
    private Boolean bool;
    private DatagramSocket ds;
    
    public ListenerUDP(Controller control, Node id) {
        this.control    = control;
        this.id         = id;
        this.bool       = this.control.getUDPFlag().get();
    }
    
    public void run(){
        try {
            this.ds         = new DatagramSocket(9999);
            this.ds.setSoTimeout(1000);
        } catch (SocketException ex) {
            Logger.getLogger(ListenerUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while(true){
            
            synchronized(this.control){
                this.bool = this.control.getUDPFlag().get();
            }
            if(this.bool){
                try {
                    byte[] b1 = new byte[1024]; // Size to Fix

                    DatagramPacket dp = new DatagramPacket(b1, b1.length);
                    this.ds.receive(dp);
                    String str = new String(dp.getData(),0,dp.getLength());
                    System.out.println("Mensagem do Emiter foi: "+ str);

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

//Tread que vai ler o que está no socket
//escreve no ecra o Hello
//meter ambos a falar para o mesmo sitio, para o local host