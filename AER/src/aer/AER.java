/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *tomas
 * @author pedro
 */
public class AER {

    /**
     * @param args the command line arguments
     */
    public static void main(String argv[]) throws Exception
      {
         ActiveRequest table_active = new ActiveRequest();
         RoutingTable table_route = new RoutingTable();
          
          
 	 SchedulerTCP serverTCP=new SchedulerTCP();
         UDP serverUDP=new UDP(table_active, table_route);
         
         
         Thread listenerTCP=new Thread(serverTCP);
         Thread listenerUDP=new Thread(serverUDP);
      }
    
}
