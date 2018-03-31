/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

//Class que Contem Informacao relativa ao Nodo

import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import static aer.miscelaneous.Crypto.decryptString;
import static aer.miscelaneous.Crypto.encryptString;
import static aer.miscelaneous.Crypto.generateSharedSecret;
import static aer.miscelaneous.Crypto.toHex;
import aer.miscelaneous.Tuple;
import java.net.Inet6Address;
import java.net.InetAddress;

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
import java.util.ArrayList;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class Node {
    //Configs
    public Config  config;
    
    //Identity
    private byte[]      id;
    private PrivateKey  privk;
    private PublicKey   pubk;
    private Integer     seq_num;
    private Integer     req_num;
    
    //Routing
    private ZoneTopology topo;
    private RequestCache rcache;
    private HitCache     hcache;   
    
    public Node(Config config) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        //Configs
        this.config = config;
        //Node Identity
        this.seq_num        = -1;
        genKeyPair();
        //Routing Data
        this.topo   = new ZoneTopology(config);
        this.rcache = new RequestCache(config);
        this.hcache = new HitCache(config);
    }
    
    
    //-----------------------------
    //CODE RELATED TO NODE IDENTITY
    //------------------------------
    
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
        KeyPair pair    = Crypto.genValidPair(this.config.getDifficulty());
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
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    //-----------------------------------------
    //CODE RELATED TO MAINTAINING ROUTING DATA
    //-----------------------------------------
    
    //---------NODE CLASS----------------
    void incrSeq(){
        this.seq_num++;
    }
    void incrReq(){
        this.req_num++;
    }
    
    boolean compare_seq(byte[] seq) {
        //Validar Input etc
        int new_seq = ByteBuffer.wrap(seq).getInt();
        
        if(new_seq>this.seq_num) return true;
        else return false;
    }
    
    //-----------ZONETOPOLOGY CLASS--------------
    public void addPeerZone(byte[] nodeId, InetAddress addr6, byte[] seq_num, ArrayList<Tuple> peers) {
        //CHECK SEQUNCE NUMBER
        synchronized(this.topo){
            this.topo.addPeerZone(nodeId, addr6, seq_num, peers);
        }
    }
    
    //Vale a pena verificar o IPV6????
    public void rmPeerZone(byte[] nodeId) {
        synchronized(this.topo){
            this.topo.removePeer(nodeId);
        }
    }
    
    //GarbageCollect
    public void gcPeerZone() {
        synchronized(this.topo){
            this.topo.gcPeer();
        }
    }
    
    //-----------HITCACHE CLASS-----------
    //GarbageCollect
    public void gcHitCache() {
        synchronized(this.hcache){
            this.hcache.gcHit();
        }
    }
    
    //-------------REQUESTCACHE CLASS----------
    //GarbageCollect
    public void gcReqCache() {
        synchronized(this.rcache){
            this.rcache.gcReq();
        }
    }
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    //-----------------------------------------
    //CODE RELATED TO REQUESTING ROUTING DATA
    //-----------------------------------------
    
    //---------NODE CLASS-----------
    public byte[] getSeq() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(this.seq_num);
        
        return buffer.array();
    }   
    
    //---------ZONETOPOLOGY CLASS-----------
    //Get Peers in Zone max distance = hops
    public ArrayList<Tuple> getZonePeersIds(int maxHops) {
        ArrayList<Tuple> out = this.topo.getPeer(maxHops);
        if(out.size() > 0) return out;
        return new ArrayList<Tuple>();
    }
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    
    
    
    
    
    //TODO
    public void getRoute() {
            //Search for Hit
            
            //Request on Topology
    }
}
