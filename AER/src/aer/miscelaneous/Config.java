/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

/**
 *
 * @author pedro
 */
public class Config {

    
    //Configs
    int difficulty             = 1; //Num de zeros msb's no nodeId

    int queueSize              = 30;
    int zoneSize               = 2; //Tamanho da Zona
    int zoneCacheSize          = 30; //Tamanho de nodos destinos na tabela zonetopology
    int requestCacheSize       = 30; //Tamanhp da Cache dos Requests
    int hitCacheSize           = 30; //Tamanho da Cache dos HIts

    int zoneMapSize            = 10; //size per map
    int reqArraySize           = 10; //size per array for limiting per nodeid, 1 nodeid can have more than 1 request
    int hitArraySize           = 10;
    
    long zoneTimeDelta         = 60;  //Tempo max entre peer hellos  (s)
    long reqTimeDelta          = 600; //Tempo de vida dos Requests  (s)
    long hitTimeDelta          = 600; //Tempo de vida dos Hit  (s)
    int watchDogTimer          = 10;  //Tempo de sleep para o WatchDog da tabela ZoneTopology  (s)
    
    long helloTimer            = 1; //Tempo de intervalos entre hellos enviados
    
    int attempNo               = 5; //Number of attempts
    int hopLimit               = 10; //Max Hops
    byte security              = 0x01; //SECURITY 0X00 OFF 0X01 ON
    
    public Config(){
    
    }

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

    public long getZoneTimeDelta() {
        return zoneTimeDelta;
    }

    public void setZoneTimeDelta(long zoneTimeDelta) {
        this.zoneTimeDelta = zoneTimeDelta;
    }

    public long getReqTimeDelta() {
        return reqTimeDelta;
    }

    public void setReqTimeDelta(long reqTimeDelta) {
        this.reqTimeDelta = reqTimeDelta;
    }

    public long getHitTimeDelta() {
        return hitTimeDelta;
    }

    public void setHitTimeDelta(long hitTimeDelta) {
        this.hitTimeDelta = hitTimeDelta;
    }

    public int getWatchDogTimer() {
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

    public int getReqArraySize() {
        return reqArraySize;
    }

    public void setReqArraySize(int reqArraySize) {
        this.reqArraySize = reqArraySize;
    }

    public int getHitArraySize() {
        return hitArraySize;
    }

    public void setHitArraySize(int hitArraySize) {
        this.hitArraySize = hitArraySize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
    
    
}
