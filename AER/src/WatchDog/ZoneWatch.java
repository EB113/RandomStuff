/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WatchDog;

import aer.Data.Node;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class ZoneWatch implements Runnable{
    AtomicBoolean flag;
    int sleepTime;
    Node node;
    
    public ZoneWatch(AtomicBoolean flag, int sleeTime, Node node){
        this.flag       = flag;
        this.sleepTime  = sleeTime;
        this.node       = node;
    }
    
    @Override
    public void run() {
        while(true){
            synchronized(this.flag){
                if(this.flag.get()){
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
