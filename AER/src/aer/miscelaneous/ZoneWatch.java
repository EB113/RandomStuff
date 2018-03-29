/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import aer.Data.Node;
import aer.miscelaneous.Config;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class ZoneWatch implements Runnable{
    Controller control;
    int sleepTime;
    Node node;
    
    public ZoneWatch(Controller control, Config config, Node node){
        this.control    = control;
        this.sleepTime  = config.getWatchDogTimer();
        this.node       = node;
    }
    
    @Override
    public void run() {
        while(true){
            synchronized(this.control){
                if(this.control.getWatchDogFlag().get()){
                    synchronized(this.node){
                        this.node.gcPeerZone();
                    }
                    synchronized(this.node){
                        this.node.gcReqCache();
                    }
                    synchronized(this.node){
                        this.node.gcHitCache();
                    }
                }else return;
            }
            try {
                TimeUnit.SECONDS.sleep(this.sleepTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZoneWatch.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
