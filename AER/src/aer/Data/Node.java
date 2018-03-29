/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

//Class que Contem Informacao relativa ao Nodo

import aer.miscelaneous.Crypto;
import static aer.miscelaneous.Crypto.decryptString;
import static aer.miscelaneous.Crypto.encryptString;
import static aer.miscelaneous.Crypto.generateSharedSecret;
import static aer.miscelaneous.Crypto.toHex;
import java.net.Inet6Address;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class Node {
    //Configs
    private int     difficulty;
    private byte[]  seq_num;
    private long    zoneTimeDelta;  
    private long    reqTimeDelta;
    private long    hitTimeDelta;
    
    //Identity
    private byte[]      id;
    private PrivateKey  privk;
    private PublicKey   pubk;
    
    //Routing
    private ZoneTopology topo;
    private RequestCache rcache;
    private HitCache     hcache;   
    
    public Node(int difficulty, int zoneSize, int requestCacheSize, int hitCacheSize, long zoneTimeDelta, long reqTimeDelta, long hitTimeDelta) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        //Configs
        this.difficulty     = difficulty;
        this.zoneTimeDelta  = zoneTimeDelta;
        this.reqTimeDelta   = reqTimeDelta;
        this.hitTimeDelta   = hitTimeDelta;
        //Node Identity
        genKeyPair();
        //Routing Data
        this.topo   = new ZoneTopology(zoneSize);
        this.rcache = new RequestCache(requestCacheSize);
        this.hcache = new HitCache(hitCacheSize);
    }
    
    //
    //CODE RELATED TO NODE IDENTITY
    //
    public SecretKey getShared(PublicKey peer_pubk) {
        return generateSharedSecret(this.privk, peer_pubk);
    }
    public String encrypt(SecretKey key, String ptxt) {
            return encryptString(key, ptxt);
    }
    
    public String decrypt(SecretKey key, String ctxt) {
            return decryptString(key, ctxt);
    }
    
    public byte[] getPubKey() {
        return this.pubk.getEncoded();
    }
    public byte[] getId() {
        return this.id;
    }
    public void genKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPair pair    = Crypto.genValidPair(this.difficulty);
        this.privk      = pair.getPrivate();
        this.pubk       = pair.getPublic();
        this.id         = Crypto.getId(this.pubk.getEncoded());
    }
    public void test() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException{
        KeyPair pair1       = Crypto.genValidPair(1);
        PrivateKey privk1   = pair1.getPrivate();
        PublicKey pubk1     = pair1.getPublic();
        byte[] id1          = Crypto.getId(pubk1.getEncoded());
        
        KeyPair pair2       = Crypto.genValidPair(1);
        PrivateKey privk2   = pair2.getPrivate();
        PublicKey pubk2     = pair2.getPublic();
        byte[] id2          = Crypto.getId(pubk2.getEncoded());
        
        SecretKey k1 = generateSharedSecret(privk1, pubk2);
        SecretKey k2 = generateSharedSecret(privk2, pubk1);
        
        if(k1.getEncoded().equals(k2)) System.out.println("ola"); else System.out.println("adeus");
        
        System.out.println(toHex(k1.getEncoded()));
        System.out.println(toHex(k2.getEncoded()));
        
        System.out.println(decrypt(k1, encrypt(k2, "ola")));
    }
    
    //
    //CODE RELATED TO MAINTAINING ROUTING DATA
    //
            
    void incrSeq(){
        int aux         = ByteBuffer.wrap(this.seq_num).getInt();
        aux++;
        this.seq_num    = ByteBuffer.allocate(4).putInt(aux).array();
    }
    
    public void addPeerZone(byte[] nodeId, Inet6Address addr6, float rank, int hop_dist, byte[] seq_num) {
        if(this.topo.compare_seq(nodeId, seq_num)) this.topo.addPeer(nodeId, addr6, rank, hop_dist, seq_num);
    }
    
    //Vale a pena verificar o IPV6????
    public void rmPeerZone(byte[] nodeId) {
        this.topo.removePeer(nodeId);
    }
    
    //GarbageCollect
    public void gcPeerZone() {
        this.topo.gcPeer(this.zoneTimeDelta);
    }
    
    //GarbageCollect
    public void gcHitCache() {
        this.hcache.gcHit(this.hitTimeDelta);
    }
    
    //GarbageCollect
    public void gcReqCache() {
        this.rcache.gcReq(this.reqTimeDelta);
    }
    
    //TODO
    public void getRoute() {
            //Search for Hit
            
            //Request on Topology
    }
}
