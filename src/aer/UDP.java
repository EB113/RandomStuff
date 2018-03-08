/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.net.DatagramSocket;
import java.net.SocketException;


/**
 *
 * @author pedro
 */
public class UDP implements Runnable{

    public UDP(ActiveRequest table_active, RoutingTable table_route) throws SocketException {
        
        DatagramSocket serverSocket = new DatagramSocket(9999);
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void run(){
            
    } 
}
