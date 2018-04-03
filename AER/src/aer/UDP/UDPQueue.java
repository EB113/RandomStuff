/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import aer.miscelaneous.Tuple;
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
public class UDPQueue implements Runnable{
    private Controller control;
    private DatagramPacket dp;
    private DatagramSocket ds;
    private Tuple tuple;
    
    public UDPQueue(Controller control) throws SocketException {
        this.control    = control;
        this.tuple      = null;
        this.ds         = new DatagramSocket();
    }
    
    @Override
    public void run() {
        
        while(true){
            
            if(this.control.getUDPFlag().get()){
                
                this.tuple = (Tuple)control.popQueueUDP();
                
                if(tuple != null) {
                    this.dp = new DatagramPacket((byte[])tuple.x, ((byte[])tuple.x).length, (InetAddress)tuple.y, 9999);
                    System.out.println("<---: " + Crypto.toHex((byte[])tuple.x));
                }
                
                try {
                    if(this.dp != null) this.ds.send(dp);
                    this.dp = null;
                } catch (IOException ex) {
                    Logger.getLogger(UDPQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else return;
            
        }    
            
    }
    
}
