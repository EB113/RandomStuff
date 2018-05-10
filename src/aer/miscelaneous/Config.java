/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import static java.lang.Math.exp;

/**
 *
 * @author pedro
 */
public class Config {

    
    //Configs
    
        //DATA
        int queueSize               = 30;
        //int zoneSize               = 2; //Tamanho da Zona
        //int zoneSizeHello          = 1; //Dist maxima dos nodos para ser enviado no Hello
        int zoneCacheSize           = 30; //Tamanho de nodos destinos na tabela zonetopology
        int dataCacheSize           = 30; //Tamanhp da Cache dos Requests
        int peerCacheSize           = 10; //Tamanho da Cache dos HIts

        int zoneMapSize             = 10; //size per map
        int reqMapSize              = 10; //size per array for limiting per nodeid, 1 nodeid can have more than 1 request
        int hitMapSize              = 10;

        long zoneTimeDelta         = 2000;  //Tempo max entre peer hellos  (ms)
        long dataTimeDelta         = 10000; //Tempo max por Req ou Rep na Cache
        
        /*
        long reqTimeDelta          = 60000; //Tempo de vida dos Requests  (ms)
        long repTimeDelta          = 30000; //Tempo de vida dos Requests  (ms)
        long hitTimeDelta          = 60000; //Tempo de vida dos Hit  (ms)
    */
        long watchDogTimer          = 1;  //Tempo de sleep para o WatchDog da tabela ZoneTopology  (s)

        long helloTimer            = 1; //Tempo de intervalos entre hellos enviados
        
        long TTL                   = 50000; //TIME TO LIVE DATAREQPACK

        int attempNo               = 5; //Number of attempts
        int hopLimit               = 10; //Max Hops
        byte security              = 0x00; //SECURITY 0X00 OFF 0X01 ON
    
    //Session
    String coreSession             = "40617";
    int difficulty                 = 1; //Num de zeros msb's no nodeId
    long peerCacheTimeDelta        = 25000; //MAX time for peerCache entrys 1min
    byte mode                      = 0x01; //Changeable vs not changeable
    
    public Config(){
    
    }

    //--------------------------------------
    //TEM QUE SE FAZER SYNCHRONIZE A ISTO TUDO
    //--------------------------------------
    /*
    public int getZoneSizeHello() {
        return zoneSizeHello;
    }

    public void setZoneSizeHello(int zoneSizeHello) {
        this.zoneSizeHello = zoneSizeHello;
    }
*/
    public int getAttempNo() {
        return attempNo;
    }

    public void setAttempNo(int attempNo) {
        this.attempNo = attempNo;
    }

    public byte getSecurity() {
        return security;
    }

    public void setSecurity(byte security) {
        this.security = security;
    }

    public int getHopLimit() {
        return hopLimit;
    }

    public void setHopLimit(int hopLimit) {
        this.hopLimit = hopLimit;
    }

    public int getZoneCacheSize() {
        return zoneCacheSize;
    }

    public void setZoneCacheSize(int zoneCacheSize) {
        this.zoneCacheSize = zoneCacheSize;
    }

    public int getZoneMapSize() {
        return zoneMapSize;
    }

    public void setZoneMapSize(int zoneMapSize) {
        this.zoneMapSize = zoneMapSize;
    }


    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
/*
    public int getZoneSize() {
        return zoneSize;
    }

    public void setZoneSize(int zoneSize) {
        this.zoneSize = zoneSize;
    }

    public int getRequestCacheSize() {
        return requestCacheSize;
    }

    public void setRequestCacheSize(int requestCacheSize) {
        this.requestCacheSize = requestCacheSize;
    }

    public int getHitCacheSize() {
        return hitCacheSize;
    }

    public void setHitCacheSize(int hitCacheSize) {
        this.hitCacheSize = hitCacheSize;
    }
*/
    public long getZoneTimeDelta() {
        return zoneTimeDelta;
    }

    public void setZoneTimeDelta(long zoneTimeDelta) {
        this.zoneTimeDelta = zoneTimeDelta;
    }
/*
    public long getLocalReqTimeDelta() {
        return reqLocalTimeDelta;
    }
    
    public long getRemoteReqTimeDelta() {
        return reqRemoteTimeDelta;
    }

    public void setLocalReqTimeDelta(long reqTimeDelta) {
        this.reqLocalTimeDelta = reqTimeDelta;
    }
    
    public void setRemoteReqTimeDelta(long reqTimeDelta) {
        this.reqRemoteTimeDelta = reqTimeDelta;
    }

    public long getHitTimeDelta() {
        return hitTimeDelta;
    }

    public void setHitTimeDelta(long hitTimeDelta) {
        this.hitTimeDelta = hitTimeDelta;
    }
*/
    public long getWatchDogTimer() {
        return watchDogTimer;
    }

    public void setWatchDogTimer(int watchDogTimer) {
        this.watchDogTimer = watchDogTimer;
    }

    public long getHelloTimer() {
        return this.helloTimer;
    }

    public void setHelloTimer(long helloTimer) {
        this.helloTimer = helloTimer;
    }

    public int getReqMapSize() {
        return reqMapSize;
    }

    public void setReqMapSize(int reqArraySize) {
        this.reqMapSize = reqArraySize;
    }

    public int getHitMapSize() {
        return hitMapSize;
    }

    public void setHitMapSize(int hitArraySize) {
        this.hitMapSize = hitArraySize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public void refreshRate(int peerTraffic) {
        
        Double tmp = exp(peerTraffic/4) - 0.5;
        
        long decimal = Long.parseLong(tmp.toString().split("\\.")[0]);
        long fractional = Long.parseLong(tmp.toString().split("\\.")[1]);
        
        int length = (int)(Math.log10(fractional)+1);
        this.helloTimer  = (decimal*1000 + (long)Math.floor((fractional/(Math.pow(10,length-3)))));
        
        this.watchDogTimer = this.helloTimer+500;
    }

    public String getCoreSession() {
        return this.coreSession;
    }

    public int getPeerCacheSize() {
        return this.peerCacheSize;
    }

    public long getPeerCacheTimeDelta() {
        return this.peerCacheTimeDelta;
    }
    
    public void print() {
    
        System.out.println("HELLO: " + this.helloTimer);
        System.out.println("WATCH: " + this.watchDogTimer);
    }

    public long getTTL() {
        return this.TTL;
    }

    public byte getMode() {
        return this.mode;
    }

    public int getDataCacheSize() {
        return dataCacheSize;
    }
    
    public long getDataReqTimeDelta() {
        return this.dataTimeDelta;
    }
    
}
