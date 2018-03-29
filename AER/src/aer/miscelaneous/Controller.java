/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author pedro
 */
public class Controller {
    private AtomicBoolean watchDogFlag;
    private AtomicBoolean UDPFlag;        
    private AtomicBoolean TCPFlag;        
            
    public Controller() {
        this.watchDogFlag  = new AtomicBoolean(true); //Falg para termino da THread
        this.UDPFlag       = new AtomicBoolean(true); //Falg para termino da THread
        this.TCPFlag       = new AtomicBoolean(true); //Falg para termino da THread
    }

    public AtomicBoolean getWatchDogFlag() {
        return this.watchDogFlag;
    }

    public void setWatchDogFlag(Boolean watchDogFlag) {
        this.watchDogFlag.set(watchDogFlag);
    }

    public AtomicBoolean getUDPFlag() {
        return this.UDPFlag;
    }

    public void setUDPFlag(Boolean UDPFlag) {
        
        this.UDPFlag.set(UDPFlag);
    }

    public AtomicBoolean getTCPFlag() {
        return this.TCPFlag;
    }

    public void setTCPFlag(Boolean TCPFlag) {
        this.TCPFlag.set(TCPFlag);
    }
    
}
