/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import java.net.DatagramSocket;
import java.net.SocketException;


/**
 *
 * @author pedro
 */
public class EmitterUDP implements Runnable{

    public EmitterUDP() throws SocketException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void run(){
          System.out.println("Hello");
    } 
}

//tread que vai escrever na tread
//envia o Hello