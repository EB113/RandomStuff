/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.PDU;

import aer.miscelaneous.Config;

/**
 *
 * @author pedro
 */
class DataReply {

    //Config
    Config config;
        
    DataReply(Config config) {
        
        this.config = config;
    }
    
    public static void load() {
    
    }
    
    public static byte[] dumpLocal() {
        byte[] raw = null;
        
        return raw;
    }
    
    public static byte[] dumpRemote() {
        byte[] raw = null;
        
        return raw;
    }
    
}
