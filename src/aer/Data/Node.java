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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class Node {
    //Configs
    public Config   config;
    public String   coreID;
    
    //Identity
    private byte[]      id;
    private PrivateKey  privk;
    private PublicKey   pubk;
    private Integer     seq_num;
    private Integer     req_num;
    
    //Routing
    private ZoneTopology        topo;
    private DataRequestCache    drqcache;
    private DataReplyCache      drpcache;
    private PeerCache           pcache;
    
    //private RemoteRequestCache rrcache;
    //private LocalRequestCache lrcache;
    
    //Position
    private Tuple position;
    private Tuple direction;
    private Double speed;
    private long timestamp;
    
    public Node(Config config, String coreID) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        
        //Configs
        this.config = config;
        this.coreID = coreID;
        //Node Identity
        this.seq_num        = -1;
        this.req_num        = 0;
        genKeyPair();
        
        //Routing Data
        this.pcache         = new PeerCache(config);
        this.topo           = new ZoneTopology(config, this.pcache);
        this.drqcache       = new DataRequestCache(config);
        this.drpcache       = new DataReplyCache(config);
        //this.rrcache    = new RemoteRequestCache(config);
        //this.lrcache    = new LocalRequestCache(config);
        
        //Position
        this.position   = new Tuple(0.0,0.0);
        this.direction  = new Tuple(0.0,0.0);
        this.speed      = 0.0;
        this.timestamp  = 0;
        
        System.out.println("NodeId: " + Crypto.toHex(this.id));
        System.out.println("ZonePeerNo: " + this.topo.gem.size());
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
    public void addPeerZone(byte[] nodeId, InetAddress addr6, Tuple position, Tuple direction, Double speed) {
        //CHECK SEQUNCE NUMBER
        if(this.topo != null && this.pcache != null)
            synchronized(this.pcache){
                synchronized(this.topo){
                    this.topo.addPeerZone(nodeId, addr6, position, direction, speed);
                }
            }
        return;
    }
    
    /*
    //Vale a pena verificar o IPV6????
    public void rmPeerZone(byte[] nodeId) {
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.removePeer(new ByteArray(nodeId));
            }
        return;
    }
    */
    
    //GarbageCollect
    public void gcPeerZone() {
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.gcPeer();
            }
        return;
    }
    public void gcPeerCache() {
        if(this.pcache != null)
            synchronized(this.pcache){
                this.pcache.gcPeer();
            }
        return;
    }
    
    //-------------REQUESTCACHE CLASS----------
    //GarbageCollect
   /* public void gcReqCache() {
        if(this.rrcache != null)
            synchronized(this.rrcache){
                this.rrcache.gcReq();
            }
        if(this.rrcache != null)
            synchronized(this.rrcache){
                this.lrcache.gcReq();
            }
        return;
    }*/
    //-----------------------------------------
    //-------------------END------------------
    //-----------------------------------------
    
    
    //-----------------------------------------
    //CODE RELATED TO REQUESTING ROUTING DATA
    //-----------------------------------------
    
    //---------NODE CLASS-----------
    /*public byte[] getSeqNum() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        
        synchronized(this.seq_num){
            buffer.putInt(this.seq_num);
        }
        
        return buffer.array();
    } */  
    
    public void incReqNum() {
        synchronized(this.req_num){
            this.req_num++;
        }
    }
    
    public byte[] getReqNum() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        
        synchronized(this.req_num){
            buffer.putInt(this.req_num);
        }
        
        return buffer.array();
    }
    
    //DEBUGDEGBUGDEBUG
    //DEBUGDEGBUGDEBUG
    //DEBUGDEGBUGDEBUG
    
    public void print() {
        if(this.topo != null)
            synchronized(this.topo){
                System.out.println("ZonePeerNo: " + this.topo.gem.size());
                this.topo.printPeers();
            }
        if(this.pcache != null)
            synchronized(this.pcache){
                System.out.println("PeerCacheNo: " + this.pcache.gemZone.size());
                this.topo.printPeers();
            }
        System.out.println("POS: [" + ((double)this.position.x) + ", " + ((double)this.position.y) + "] " 
                +  "DIR: [" + ((double)this.direction.x) + ", " + ((double)this.direction.y) + "] "
                        + "SPEED: " + this.speed);
        
        this.config.print();
    }
    
    public int getPeerTraffic() {
        
        int out = 0;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getCount();
            }
        
        return out;
    }
    
    public Tuple getPosition() {
        Tuple out = null;
        
        if(this.position != null)
            synchronized(this.position){
                out = this.position;
            }
        
        return out;
    }

    public Tuple getDirection() {
        Tuple out = null;
        
        if(this.direction != null)
            synchronized(this.direction){
                out = this.direction;
            }
        
        return out;        
    }

    public Double getSpeed() {
        Double out = null;
        
        if(this.speed != null)
            synchronized(this.speed){
                out = this.speed;
            }
        
        return out;        
    }

    public long getTimeStamp() {
        return this.timestamp;
    }
    
    
    public void refreshPosData() throws IOException {
        
        String session  = config.getCoreSession();
        String path     = "/tmp/pycore." + session + "/" + this.coreID + ".xy";
        
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        
        if(line != null){
            String[] parts = line.split(" ");

            Double x = Double.parseDouble(parts[0]);
            Double y = Double.parseDouble(parts[1]);

            if(this.direction != null)
                synchronized(this.direction){
                    this.direction.x = x - (double)this.position.x;
                    this.direction.y = y - (double)this.position.y;
                }

            if(this.position != null)
                synchronized(this.position){
                    this.position.x = x;
                    this.position.y = y;
                }
            long now = System.currentTimeMillis();
            long timestamp = (now - this.timestamp)/1000;
            this.timestamp = now;

            if(this.speed != null)
                synchronized(this.speed){
                    this.speed = Math.sqrt(Math.pow((double)this.direction.x,2) + Math.pow((double)this.direction.y,2))/timestamp;
                }
        }
    }
    
    
    /*
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
                tuple = this.topo.getPeer(new ByteArray(nodeIdDst));
            }
        
        return tuple;
    }
*/
    /*
    public void addReqCache(LinkedList<InetAddress> usedPeers, InetAddress nodeHopAddr, byte[] nodeIdSrc, byte[] nodeIdDst, int hop_count, int hop_max, byte[] req_num, byte[] peerKey) {
        
        if(Crypto.cmpByteArray(id, nodeIdSrc)) {// TROCAR PARA HOP COUNT == 0 mais rapido
            if(this.lrcache != null)
                synchronized(this.lrcache){
                    this.lrcache.addRequest(usedPeers, nodeIdDst, req_num, hop_max);
                }
        }else {
        
            if(this.rrcache != null)
                synchronized(this.rrcache){
                    //ADD REQUEST
                    this.rrcache.addRequest(usedPeers, nodeHopAddr, nodeIdSrc, nodeIdDst, req_num, hop_count, hop_max, peerKey);
                }
        }
        
    }*/
/*
    public byte[] getNodeId(InetAddress nodeHopAddr) {
        byte[] out = null;
        
        if(this.topo != null)
                synchronized(this.topo){
                    out = this.topo.getNodeId(nodeHopAddr);
                }
        
        return out;
    }*/
/*
    public InetAddress rmReqCache(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
    
        if(Crypto.cmpByteArray(id, nodeIdSrc)) {
            if(this.lrcache != null)
                synchronized(this.lrcache){
                    this.lrcache.rmReq(nodeIdDst, req_num);
                }
        }else {
            if(this.rrcache != null)
                synchronized(this.rrcache){
                    return this.rrcache.rmReq(nodeIdDst, nodeIdSrc, req_num);
                }
        }
        
        return null;
    }
 */   
    /*
    public LinkedList<InetAddress> getPeers(InetAddress hopPeer) {
        LinkedList<InetAddress> out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getReqPeers(hopPeer);
            }
        
        return out;
    }*//*
    
    //CHAMADA PARA OBTER PEERS A QUEM MANDAR COM RANK
    //NOTA: PODEMOS RETORNAR NULL E QUEM CHAMA PODE NAO ESTAR A ESPERA!!!   
    public LinkedList<InetAddress> getReqRankPeers(InetAddress hopVAI, InetAddress hopVEM) {
        LinkedList<InetAddress> out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getReqRankPeers(hopVAI, hopVEM);
            }
        
        return out;
    }
*/
/*
    //CHAMADA PARA OBTER Todos os peers a quem mandar exceptuando 1 hop
    //NOTA: PODEMOS RETORNAR NULL E QUEM CHAMA PODE NAO ESTAR A ESPERA!!!   
    public LinkedList<InetAddress> getReqPeers(InetAddress hopPeer) {
        LinkedList<InetAddress> out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getReqPeers(hopPeer);
            }
        
        return out;
    }*/
/*
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
                if(!includedNodes.contains((InetAddress)tuple.x)) excludedNodes.push((InetAddress)tuple.x);
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

    //
    public InetAddress getRReqHopAddr(byte[] nodeIdDst, byte[] nodeIdSrc, byte[] req_num) {
        InetAddress out = null;
        
        if(this.rrcache != null)
            synchronized(this.rrcache){
                out = this.rrcache.getReqAddr(nodeIdDst, nodeIdSrc, req_num);
            }
        
        return out;
    }
*//*
    //REMOVER ROUTE NA HITCACHE OU ZONETOPOLOGY
    public void rmRoute(byte[] nodeIdSrc, byte[] nodeIdDst, InetAddress hopAddr) {
        
        if(this.topo != null)
            synchronized(this.topo){
                this.topo.rmRoute(nodeIdSrc, nodeIdDst, hopAddr);
            }
    }*/
/*
    public LinkedList<InetAddress> getExcludedNodes(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        LinkedList<InetAddress> usedPeers = new LinkedList<InetAddress>();
        
        if(Crypto.cmpByteArray(id, nodeIdSrc)) {
            usedPeers = getLocalExcludedNodes(nodeIdDst, req_num);
        }else {
            usedPeers = getRemoteExcludedNodes(nodeIdSrc, nodeIdDst, req_num);
        }
        
        return usedPeers;
    }
    
  */  
    
    
/*
    public Boolean existsReq(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        
        if(Crypto.cmpByteArray(this.id, nodeIdSrc)) {
            
            if(this.lrcache != null)
                synchronized(this.lrcache){
                    return this.lrcache.existsReq(nodeIdDst, req_num);
                }
            else return false;
        }else {
            
            if(this.rrcache != null)
                synchronized(this.rrcache){
                    return this.rrcache.existsReq(nodeIdSrc, nodeIdDst, req_num);
                }
            else return false;
        }
    }

    public Tuple addResponsivePeer(byte[] nodeIdDst, byte[] nodeIdOriginalDst, byte[] req_num, InetAddress nodeAddrHop, byte errNo, int hopAdvised) {
        
        if(Crypto.cmpByteArray(id, nodeIdDst)) {
            if(this.lrcache != null)
                synchronized(this.lrcache){
                    return this.lrcache.addResponse(nodeIdOriginalDst, req_num, nodeAddrHop, errNo);
                }
            else return null;
        }else {
            if(this.rrcache != null)
                synchronized(this.rrcache){
                    return this.rrcache.addResponse(nodeIdDst, nodeIdOriginalDst, req_num, nodeAddrHop, errNo, hopAdvised);
                }
            else return null;
        }
    }

    public RReq getReqValues(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        
        if(this.rrcache != null)
            synchronized(this.rrcache){
                return this.rrcache.getReqValues(nodeIdSrc, nodeIdDst, req_num);
            }
        else return null;
    }
*//*
    public byte[] getPeerSeqNum(byte[] nodeId) {
        byte[] out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getPeerSeqNum(nodeId);
            }
        return out;
    }*/
/*
    public byte[] getMode() {
        
        byte[] out = new byte[3];
        
        out[0] = 0x00;
        out[1] = 0x00;
        out[2] = 0x00;
        
        return out;
    }
    */

    
    public Tuple getPosData(byte[] nodeDst) {
        
        ByteArray nodeIdDst_new = new ByteArray(nodeDst);
        Tuple tuple = null;
        
        //CHECK LOCAL
        if(this.topo != null)
            synchronized(this.topo){
                tuple = this.topo.getPosPeer(nodeIdDst_new);
            }
        
        //CHECK CACHE
        if(tuple == null)
            if(this.pcache != null)
                synchronized(this.pcache){
                    tuple = this.pcache.getPeer(nodeIdDst_new);
                }
        
        return tuple;
    }

    public long getTTL() {
        return this.config.getTTL();
    }

    public byte[] GenerateSignature(byte[] raw, int limit) {
        
        byte[] tmp = new byte[limit];
        byte[] out = null;
        for(int i=0; i<limit; i++) tmp[i] = raw[i];
        
        try {
            
            out = Crypto.GenerateSignature(tmp, this.privk);
        } catch (SignatureException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return out;
    }

    public boolean addDataReq(byte[] nodeSrc, byte[] nodeDst, byte[] seq_num, long ttl, LinkedList<InetAddress> usedPeers, byte[] raw) {
        
        boolean out = false;
        if(this.drqcache != null)
            synchronized(this.drqcache){
                out = this.drqcache.addReq(nodeSrc, nodeDst, seq_num, ttl, usedPeers, raw);
            }
        
        return out;
    }
    
    public boolean addDataRep(byte[] nodeSrc, byte[] nodeDst, byte[] seq_num, long ttl, LinkedList<InetAddress> usedPeers, byte[] raw) {
        
        boolean out = false;
        if(this.drpcache != null)
            synchronized(this.drpcache){
                out = this.drpcache.addRep(nodeSrc, nodeDst, seq_num, ttl, usedPeers, raw);
            }
        if(this.drqcache != null)
            synchronized(this.drqcache){
                this.drqcache.rmReqwithRep(new ByteArray(nodeDst), new ByteArray(seq_num), nodeSrc);
            }
        return out;
    }
    
    public LinkedList<InetAddress> getZonePeer(byte[] nodeIdDst) {
        
        ByteArray nodeIdDst_new = new ByteArray(nodeIdDst);
        LinkedList<InetAddress> out = null;
        
        if(this.topo != null)
            synchronized(this.topo){
                out = this.topo.getPeer(nodeIdDst_new);
            }
        
        return out;
    }

    public LinkedList<InetAddress> getPeerCache(byte[] nodeIdDst) {
        
        ByteArray nodeIdDst_new      = new ByteArray(nodeIdDst);
        Tuple     peer               = null;
        LinkedList<InetAddress> out  = null;
        
        //CHECK CACHE
        if(this.pcache != null)
            synchronized(this.pcache){
                peer = this.pcache.getPeer(nodeIdDst_new);
            }
        //GET OPTIMAL HOP
        if(peer != null){
            if(this.topo != null)
                synchronized(this.topo){
                    out = this.topo.getOptimal(peer);
                }
        }
        return out;
    }

    public LinkedList<InetAddress> getOrientUnic(LinkedList<InetAddress> usedPeers) {
        
        LinkedList<InetAddress> out  = null;
        
        if(this.topo != null)
                synchronized(this.topo){
                    out = this.topo.getOrientUnic(usedPeers, position);
                }
        
        return out;
    }

    public void addPeerCache(byte[] nodeId, Tuple tuple) {
        
        ByteArray nodeID = new ByteArray(nodeId);
        
        //CHECK CACHE
        if(this.pcache != null)
            synchronized(this.pcache){
                this.pcache.addCachePeer(nodeID, tuple);
            }
        
    }

    public ArrayList<byte[]> getReqCache(byte[] nodeId, boolean mode) {
        
        ArrayList<byte[]> out   = null;
        
        if(this.drqcache != null)
            synchronized(this.drqcache){
                out = this.drqcache.getReq(nodeId, mode);
            }
        
        return out;
    }

    public ArrayList<byte[]> getRepCache(byte[] nodeId, boolean mode) {
        
        ArrayList<byte[]> out   = null;
        
        if(this.drpcache != null)
            synchronized(this.drpcache){
                out = this.drpcache.getRep(nodeId, mode);
            }
        
        return out;
    }

    public boolean existsZonePeer(byte[] nodeId) {
        
        boolean out = false;
        ByteArray nodeID = new ByteArray(nodeId);
        
        if(this.topo != null)
                synchronized(this.topo){
                    out = this.topo.existsPeer(nodeID);
                }
        
        return out;
    }

    public void gcDataCache() {
        
        if(this.drqcache != null)
            synchronized(this.drqcache){
                this.drqcache.gcData();
            }
        
        if(this.drpcache != null)
            synchronized(this.drpcache){
                this.drpcache.gcData();
            }
    }

    public boolean existsDataReply(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        
        Boolean out = false;
        
        if(this.drpcache != null)
            synchronized(this.drpcache){
                out = this.drpcache.exists(nodeIdSrc, nodeIdDst, req_num);
            }
        
        return out;
    }

    public boolean existsDataRequest(byte[] nodeIdSrc, byte[] nodeIdDst, byte[] req_num) {
        
        Boolean out = false;
        
        if(this.drpcache != null)
            synchronized(this.drqcache){
                out = this.drqcache.exists(nodeIdSrc, nodeIdDst, req_num);
            }
        
        return out;
    }


    
}
    
    
