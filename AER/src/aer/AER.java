/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import WatchDog.ZoneWatch;
import aer.Data.Node;
import aer.TCP.EmitterTCP;
import aer.TCP.ListenerTCP;
import aer.UDP.ListenerUDP;
import aer.UDP.EmitterUDP;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author pedro
 */
public class AER {

    public static void main(String argv[]) throws Exception
      {
         System.out.println("Init Adhoc Node....");
         //Configs
         int difficulty         = 1;
         int zoneSize           = 2;
         int requestCacheSize   = 5;
         int hitCacheSize       = 5;
         long maxdelta          = 60; //Tempo max entre peer hellos
         int watchDogTimer      = 10; //Tempo de sleep para o WatchDog da tabela ZoneTopology
         AtomicBoolean watchDogFlag = new AtomicBoolean(true);
        // Boolean watchDogFlag   = true; //Falg para termino da THread
         
         // Node Setup
         Node id = new Node(difficulty, zoneSize, requestCacheSize, hitCacheSize, maxdelta);
         
         //WatchDog
         ZoneWatch wd_zone = new ZoneWatch(watchDogFlag, watchDogTimer, id);
         Thread t_wd_zone  = new Thread(wd_zone);
         
 	 //UDP Thread Object Init + Thread Init
         EmitterUDP emitterUDP      = new EmitterUDP();
         ListenerUDP listenerUDP    = new ListenerUDP();
         
         Thread t_emitter_UDP       = new Thread(emitterUDP);
         Thread t_listener_UDP      = new Thread(listenerUDP);
         
         
         Thread.sleep(2000);
         
         synchronized(watchDogFlag){
             watchDogFlag.set(false);
         }

         //TCP Thread Object Init + Thread Init
         //EmitterTCP emitterTCP      = new EmitterTCP();
         //ListenerTCP listenerTCP    = new ListenerTCP();
         
         //Thread t_emitter_TCP       = new Thread(emitterTCP);
         //Thread t_listener_TCP      = new Thread(listenerTCP);
         
         // TCP + UDP + WatchDog's Thread Start
         t_emitter_UDP.start();
         t_listener_UDP.start();
         //t_emitter_TCP.start();
         //t_listener_TCP.start();
         t_wd_zone.start();
         
         
         // Thread close Wait
         t_emitter_UDP.join();
         t_listener_UDP.join();
         //t_emitter_TCP.join();
         //t_listener_TCP.join();
         synchronized(watchDogFlag){
             watchDogFlag.set(false);
         }
         t_wd_zone.join(); //Add a way to kill Watchdog Thread!!
         
         System.out.println("Close Adhoc Node....");
      }
    
}
