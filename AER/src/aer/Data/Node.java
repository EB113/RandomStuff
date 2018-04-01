/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

//Class que Contem Informacao relativa ao Nodo

import aer.miscelaneous.ByteArray;
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
import java.util.LinkedList;
import java.util.Queue;
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
    private RemoteRequestCache rrcache;
    private HitCache     hcache;   
    private LocalRequestCache lrcache;
    
    public Node(Config config) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        //Configs
        this.config = config;
        //Node Identity
        this.seq_num        = -1;
        genKeyPair();
        //Routing Data
        this.topo       = new ZoneTopology(config);
        this.rrcache    = new RemoteRequestCache(config);
        this.lrcache    = new LocalRequestCache(config);
        this.hcache     = new HitCache(config);
        
        System.out.println("ZonePeerNo: " + this.topo.hmap.size());
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
            this.topo.removePeer(new ByteArray(nodeId));
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
        synchronized(this.rrcache){
            this.rrcache.gcReq();
        }
    }
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    //-----------------------------------------
    //CODE RELATED TO REQUESTING ROUTING DATA
    //-----------------------------------------
    
    //---------NODE CLASS-----------
    public byte[] getSeqNum() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(this.seq_num);
        
        return buffer.array();
    }   
    
    public byte[] getReqNum() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(this.req_num);
        
        return buffer.array();
    }
    
    //---------ZONETOPOLOGY CLASS-----------
    //Get Peers in Zone max distance = hops
    public LinkedList<Tuple> getZonePeersIds(int maxHops) {
        return this.topo.getPeers(maxHops);
    }
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    
    public Tuple getZonePeer(byte[] nodeIdDst) {
        return this.topo.getPeer(nodeIdDst);
    }

    public void addHitCache(InetAddress nodeHopAddr, byte[] nodeHopId, byte[] nodeIdDst , int hop_count) {
        //ADD HIT OF REQUEST SOURCE
        this.hcache.addHit(nodeHopAddr, nodeHopId, nodeIdDst, hop_count);
    }
    
    public void addReqCache(LinkedList<InetAddress> usedPeers, InetAddress nodeHopAddr, byte[] nodeIdSrc, byte[] nodeIdDst, int hop_count, byte[] req_num) {
        
        if(Crypto.cmpByteArray(id, nodeIdSrc)) {
            this.lrcache.addRequest(usedPeers, nodeHopAddr, nodeIdDst, 0, req_num);
        }else {
        
            //ADD HIT OF REQUEST SOURCE
            addHitCache(nodeHopAddr, nodeIdSrc, nodeIdDst, hop_count);
            //ADD REQUEST
            this.rrcache.addRequest(usedPeers, nodeHopAddr, nodeIdSrc, nodeIdDst, req_num);
        }
        
    }

    public byte[] getNodeId(InetAddress nodeHopAddr) {
        return this.topo.getNodeId(nodeHopAddr);
    }

    public InetAddress rmReqCache(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
        return this.rrcache.rmReq(nodeIdDst, nodeIdSrc, req_num);
    }

    public Tuple getHitPeer(byte[] nodeIdDst) {
        return this.hcache.getHit(nodeIdDst);
    }

    public LinkedList<InetAddress> getReqPeers() {
        return this.topo.getReqPeers();
    }

    public byte[] getLocalReqTarget(byte[] req_num) {
        return this.lrcache.getReqTarget(req_num);
    }

    public byte[] getRemoteReqTarget(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        return this.rrcache.getReqTarget(nodeIdSrc, nodeIdDst, req_num);
    }
    
    public LinkedList<InetAddress> getLocalExcludedNodes(byte[] nodeIdDst, byte[] req_num) {
        
        LinkedList<InetAddress> includedNodes = this.lrcache.getIncludedNodes(nodeIdDst, req_num);
        LinkedList<Tuple> totalNodes = getZonePeersIds(1);
        LinkedList<InetAddress> excludedNodes = new LinkedList<>();
        
        for(Tuple tuple : totalNodes) {
            if(!includedNodes.contains(id)) excludedNodes.push((InetAddress)tuple.x);
        }
        
        return excludedNodes;
    }

    public LinkedList<InetAddress> getRemoteExcludedNodes(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        
        LinkedList<InetAddress> includedNodes = this.rrcache.getIncludedNodes(nodeIdSrc, nodeIdDst, req_num);
        LinkedList<Tuple> totalNodes = getZonePeersIds(1);
        LinkedList<InetAddress> excludedNodes = new LinkedList<>();
        
        for(Tuple tuple : totalNodes) {
            if(!includedNodes.contains(id)) excludedNodes.push((InetAddress)tuple.x);
        }
        
        return excludedNodes;
    }

    public InetAddress getLocalReqAddr(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
        return this.rrcache.getReqAddr(nodeIdDst, nodeIdSrc, req_num);
    }

    //REMOVER ROUTE NA HITCACHE OU ZONETOPOLOGY
    public void rmHit(byte[] nodeIdSrc, byte[] nodeIdDst, InetAddress hopAddr) {
        this.topo.rmRoute(nodeIdSrc, nodeIdDst, hopAddr);
        this.hcache.rmRoute(nodeIdSrc, nodeIdDst, hopAddr);
    }

    public LinkedList<InetAddress> getExcludedNodes(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        LinkedList<InetAddress> usedPeers = new LinkedList<InetAddress>();
        
        if(Crypto.cmpByteArray(id, nodeIdSrc)) {
            usedPeers = getLocalExcludedNodes(nodeIdDst, req_num);
        }else {
            usedPeers = getRemoteExcludedNodes(nodeIdSrc, nodeIdDst, req_num);
        }
        
        return usedPeers;
    }
    
    
    
    //DEBUGDEGBUGDEBUG
    //DEBUGDEGBUGDEBUG
    //DEBUGDEGBUGDEBUG
    
    public void print() {
        System.out.println("Load Feito...ZonePeerNo: " + this.topo.hmap.size());
    }
    
    
}
