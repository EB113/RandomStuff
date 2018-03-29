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

    int zoneSize               = 2; //Tamanho da Zona
    int requestCacheSize       = 5; //Tamanhp da Cache dos Requests
    int hitCacheSize           = 5; //Tamanho da Cache dos HIts

    int reqArraySize           = 5; //size per array for limiting per nodeid, 1 nodeid can have more than 1 request
    int hitArraySize           = 5;
    
    long zoneTimeDelta         = 60;  //Tempo max entre peer hellos  (s)
    long reqTimeDelta          = 600; //Tempo de vida dos Requests  (s)
    long hitTimeDelta          = 600; //Tempo de vida dos Hit  (s)
    int watchDogTimer          = 10;  //Tempo de sleep para o WatchDog da tabela ZoneTopology  (s)
    
    
    public Config(){
    
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
    
    
}
