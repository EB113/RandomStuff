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
    Controller      control;
    Config          config;
    Node            node;
    private Boolean bool;
    
    public ZoneWatch(Controller control, Config config, Node node){
        this.control    = control;
        this.config     = config;
        this.node       = node;
        this.bool       = this.control.getWatchDogFlag().get();
    }
    
    @Override
    public void run() {
        while(true){
            
            synchronized(this.control){
                
                this.bool = this.control.getWatchDogFlag().get();
            }
            if(this.bool){
                    
                this.node.gcPeerZone();
                this.node.gcPeerCache();
                this.node.gcDataCache();

                try {
                    TimeUnit.MILLISECONDS.sleep(config.getWatchDogTimer());
                } catch (InterruptedException ex) {
                    Logger.getLogger(ZoneWatch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else return;
        }
    }
    
}
