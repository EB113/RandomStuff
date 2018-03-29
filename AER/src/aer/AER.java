/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;


import aer.Data.Node;
import aer.TCP.EmitterTCP;
import aer.TCP.ListenerTCP;
import aer.UDP.ListenerUDP;
import aer.UDP.EmitterUDP;
import aer.miscelaneous.Config;
import aer.miscelaneous.Controller;
import aer.miscelaneous.ZoneWatch;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author pedro
 */
public class AER {

    public static void main(String argv[]) throws Exception
      {
         System.out.println("Init Adhoc Node....");
         
         Controller control = new Controller();
         Config config = new Config();
         
         // Node Setup
         Node id = new Node(config);
         
         //WatchDog
         ZoneWatch wd_zone = new ZoneWatch(control, config, id);
         Thread t_wd_zone  = new Thread(wd_zone);
         
 	 //UDP Thread Object Init + Thread Init
         ListenerUDP listenerUDP    = new ListenerUDP();
         EmitterUDP emitterUDP      = new EmitterUDP(control);
         
         Thread t_listener_UDP      = new Thread(listenerUDP);
         Thread t_emitter_UDP       = new Thread(emitterUDP);
        
         //TCP Thread Object Init + Thread Init
         //EmitterTCP emitterTCP      = new EmitterTCP();
         //ListenerTCP listenerTCP    = new ListenerTCP();
         
         //Thread t_emitter_TCP       = new Thread(emitterTCP);
         //Thread t_listener_TCP      = new Thread(listenerTCP);
         
         // TCP + UDP + WatchDog's Thread Start
        
         t_listener_UDP.start();
         t_emitter_UDP.start();
         //t_emitter_TCP.start();
         //t_listener_TCP.start();
         t_wd_zone.start();
         
         synchronized(control){
             control.setUDPFlag(false);
         }
         synchronized(control){
             control.setWatchDogFlag(false);
         }
         
         // Thread close Wait
         t_listener_UDP.join();
         t_emitter_UDP.join();
         //t_emitter_TCP.join();
         //t_listener_TCP.join();
         t_wd_zone.join(); //Add a way to kill Watchdog Thread!!
        
         System.out.println("Close Adhoc Node....");
      }
    
}
