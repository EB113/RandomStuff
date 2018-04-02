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
        
        System.out.println("NodeId: " + Crypto.toHex(this.id));
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
        byte[] outPubK = null;
        
        if(this.pubk !=null)
            synchronized(this.pubk){
                outPubK = this.pubk.getEncoded();
            }
        
        return outPubK;
    }
    public byte[] getId() {
        byte[] outId = null;
        
        if(this.id != null){
            synchronized(this.id){
                outId = this.id;
            }
        }
        return outId;
    }
    public void genKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPair pair    = Crypto.genValidPair(this.config.getDifficulty());
        
        this.privk      = pair.getPrivate();
        this.pubk       = pair.getPublic();
        this.id         = Crypto.getId(this.pubk.getEncoded());
    }
    
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    //-----------------------------------------
    //CODE RELATED TO MAINTAINING ROUTING DATA
    //-----------------------------------------
    
    //---------NODE CLASS----------------
    void incrSeq(){
        if(this.seq_num != null)
            synchronized(this.seq_num){
                this.seq_num++;
            }
    }
    void incrReq(){
        if(this.req_num != null)
            synchronized(this.req_num){
                this.req_num++;
            }
    }
    
    boolean compare_seq(byte[] seq) {
        Boolean bool = false;
        
        //Validar Input etc
        int new_seq = ByteBuffer.wrap(seq).getInt();
        if(this.seq_num != null)
            synchronized(this.seq_num){
                if(new_seq>this.seq_num) bool =  true;
            }
        return bool;
    }
    
    //-----------ZONETOPOLOGY CLASS--------------
    public void addPeerZone(byte[] nodeId, InetAddress addr6, byte[] seq_num, ArrayList<Tuple> peers) {
        //CHECK SEQUNCE NUMBER
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.addPeerZone(this.id, nodeId, addr6, seq_num, peers);
            }
        return;
    }
    
    //Vale a pena verificar o IPV6????
    public void rmPeerZone(byte[] nodeId) {
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.removePeer(new ByteArray(nodeId));
            }
        return;
    }
    
    //GarbageCollect
    public void gcPeerZone() {
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.gcPeer();
            }
        return;
    }
    
    //-----------HITCACHE CLASS-----------
    //GarbageCollect
    public void gcHitCache() {
        if(this.hcache != null)
            synchronized(this.hcache){
                this.hcache.gcHit();
            }
        return;
    }
    
    //-------------REQUESTCACHE CLASS----------
    //GarbageCollect
    public void gcReqCache() {
        if(this.rrcache != null)
            synchronized(this.rrcache){
                this.rrcache.gcReq();
            }
        if(this.rrcache != null)
            synchronized(this.rrcache){
                this.lrcache.gcReq();
            }
        return;
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
        if(this.seq_num != null)
            synchronized(this.seq_num){
                buffer.putInt(this.seq_num);
            }
        
        return buffer.array();
    }   
    
    public byte[] getReqNum() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        if(this.req_num != null)
            synchronized(this.req_num){
                buffer.putInt(this.req_num);
            }
        
        return buffer.array();
    }
    
    //---------ZONETOPOLOGY CLASS-----------
    //Get Peers in Zone max distance = hops
    //NOTA: PODEMOS RETORNAR NULL E QUEM CHAMA PODE NAO ESTAR A ESPERA!!!
    public LinkedList<Tuple> getZonePeersIds(int maxHops) {
        LinkedList<Tuple> out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getPeers(maxHops);
            }
        
        return out;
    }
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    //NOTA: PODEMOS RETORNAR NULL E QUEM CHAMA PODE NAO ESTAR A ESPERA!!!
    public Tuple getZonePeer(byte[] nodeIdDst) {
        Tuple tuple = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                tuple = this.topo.getPeer(nodeIdDst);
            }
        
        return tuple;
    }

    public void addHitCache(InetAddress nodeHopAddr, byte[] nodeHopId, byte[] nodeIdDst , int hop_count) {
        //ADD HIT OF REQUEST SOURCE
        if(this.hcache != null)
            synchronized(this.hcache){
                this.hcache.addHit(nodeHopAddr, nodeHopId, nodeIdDst, hop_count);
            }
    }
    
    public void addReqCache(LinkedList<InetAddress> usedPeers, InetAddress nodeHopAddr, byte[] nodeIdSrc, byte[] nodeIdDst, int hop_count, byte[] req_num) {
        
        if(Crypto.cmpByteArray(id, nodeIdSrc)) {
            if(this.lrcache != null)
                synchronized(this.lrcache){
                    this.lrcache.addRequest(usedPeers, nodeHopAddr, nodeIdDst, 0, req_num);
                }
        }else {
        
            //ADD HIT OF REQUEST SOURCE
            addHitCache(nodeHopAddr, nodeIdSrc, nodeIdDst, hop_count);
            if(this.rrcache != null)
                synchronized(this.rrcache){
                    //ADD REQUEST
                    this.rrcache.addRequest(usedPeers, nodeHopAddr, nodeIdSrc, nodeIdDst, req_num);
                }
        }
        
    }

    public byte[] getNodeId(InetAddress nodeHopAddr) {
        byte[] out = null;
        
        if(this.topo != null)
                synchronized(this.topo){
                    out = this.topo.getNodeId(nodeHopAddr);
                }
        
        return out;
    }

    public InetAddress rmReqCache(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
        InetAddress reqPeerAddr = null;
    
        if(this.rrcache != null)
            synchronized(this.rrcache){
                reqPeerAddr = this.rrcache.rmReq(nodeIdDst, nodeIdSrc, req_num);
            }
        
        return reqPeerAddr;
    }

    public Tuple getHitPeer(byte[] nodeIdDst) {
        Tuple out = null;
        
        if(this.hcache != null)
            synchronized(this.hcache){
                out = this.hcache.getHit(nodeIdDst);
            }
                
        return out;
    }
    
    //NOTA: PODEMOS RETORNAR NULL E QUEM CHAMA PODE NAO ESTAR A ESPERA!!!   
    public LinkedList<InetAddress> getReqPeers() {
        LinkedList<InetAddress> out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getReqPeers();
            }
        
        return out;
    }

    public byte[] getLocalReqTarget(byte[] req_num) {
        byte[] out = null;

        if(this.lrcache != null)
            synchronized(this.lrcache){
                out = this.lrcache.getReqTarget(req_num);
            }
        
        return out;
    }

    public byte[] getRemoteReqTarget(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        byte[] out = null;

        if(this.rrcache != null)
            synchronized(this.rrcache){
                out = this.rrcache.getReqTarget(nodeIdSrc, nodeIdDst, req_num);
            }
        
        return out;
        
    }
    
    public LinkedList<InetAddress> getLocalExcludedNodes(byte[] nodeIdDst, byte[] req_num) {
        LinkedList<InetAddress> excludedNodes = null;
        
        LinkedList<InetAddress> includedNodes = null;
        if(this.lrcache != null)
            synchronized(this.lrcache){
                includedNodes = this.lrcache.getIncludedNodes(nodeIdDst, req_num);
            }
        
        if(includedNodes != null) {
            LinkedList<Tuple> totalNodes = getZonePeersIds(1);
            excludedNodes = new LinkedList<>();

            for(Tuple tuple : totalNodes) {
                if(!includedNodes.contains(id)) excludedNodes.push((InetAddress)tuple.x);
            }
        }

        return excludedNodes;
    }

    public LinkedList<InetAddress> getRemoteExcludedNodes(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        LinkedList<InetAddress> excludedNodes = null;
        
        LinkedList<InetAddress> includedNodes = null;
        
        if(this.rrcache != null)
            synchronized(this.rrcache){
                includedNodes = this.rrcache.getIncludedNodes(nodeIdSrc, nodeIdDst, req_num);
            }
        
        if(includedNodes != null) {
            LinkedList<Tuple> totalNodes = getZonePeersIds(1);
            excludedNodes = new LinkedList<>();
            
            for(Tuple tuple : totalNodes) {
                if(!includedNodes.contains(id)) excludedNodes.push((InetAddress)tuple.x);
            }
        }
        
        return excludedNodes;
    }

    public InetAddress getLocalReqAddr(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
        InetAddress out = null;
        
        if(this.rrcache != null)
            synchronized(this.rrcache){
                out = this.rrcache.getReqAddr(nodeIdDst, nodeIdSrc, req_num);
            }
        
        return out;
    }

    //REMOVER ROUTE NA HITCACHE OU ZONETOPOLOGY
    public void rmHit(byte[] nodeIdSrc, byte[] nodeIdDst, InetAddress hopAddr) {
        
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.rmRoute(nodeIdSrc, nodeIdDst, hopAddr);
            }
        
        if(this.hcache != null)
            synchronized(this.hcache){
                this.hcache.rmRoute(nodeIdSrc, nodeIdDst, hopAddr);
            }
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
        if(this.topo != null)
            synchronized(this.topo){
                System.out.println("Load Feito...ZonePeerNo: " + this.topo.hmap.size());
                this.topo.printPeers();
            }
    }
    
    
}
