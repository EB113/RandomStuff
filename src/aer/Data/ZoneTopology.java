/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.Data;

// Class que contem informacao relativa a Topologia Local com tamanho N 
import org.apache.commons.math3.*;
        
import aer.miscelaneous.ByteArray;
import aer.miscelaneous.Config;
import aer.miscelaneous.Crypto;
import static aer.miscelaneous.Crypto.hexStringToByteArray;
import aer.miscelaneous.Tuple;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;

public class ZoneTopology {

    
    //Value Class
    class Info {
        
        //Identity
        InetAddress     hop_addr;
        int             timeRank;
        int             hop_dist;
        long            timestamp;
        
        //Position
        Tuple position;
        Tuple direction;
        Double speed;
        
        //Session
        int helloCounter;
        
        
        Info(InetAddress addr, int hop_dist, Tuple position, Tuple direction, Double speed, int counter) {
            
            // Identity
            this.hop_addr   = addr;
            this.timeRank   = 0;
            this.hop_dist   = hop_dist;
            this.timestamp  = System.currentTimeMillis();
            
            //Position
            this.position   = position;
            this.direction  = direction;
            this.speed      = speed;
            
            //Session
            this.helloCounter = counter;
        }
        
        public long getTimeStamp() {
            return this.timestamp;
        }

        public int getHop_dist() {
            return hop_dist;
        }   

        public InetAddress getHop_addr() {
            return hop_addr;
        }
        
    }
    
    //Congestion Parameters
    int         vehicles;
    double      reqTime;
    
    //Data Struct   
    HashMap <ByteArray, Info>   gem;
    PeerCache                   peerCache;
    
    //Configuration
    Config config;
    
    public ZoneTopology(Config config, PeerCache peerCache) {
        
       this.config    = config;
       
       this.gem      = new HashMap<ByteArray, Info>();
       this.peerCache = peerCache;
       vehicles = 0;
       reqTime  = 0.0;
    }
    
    public void addPeerZone(byte[] nodeId_old, InetAddress addr6, Tuple position, Tuple direction, Double speed) {
        
        Info        info        = null;
        ByteArray   nodeId      = new ByteArray(nodeId_old);
        
        //ADD HELLO OWNER
        if(!(this.gem.containsKey(nodeId)) && this.gem.size() < config.getZoneCacheSize()){
            
            info = new Info(addr6, 1, position, direction, speed, 1);
            
            this.gem.put(nodeId, info);
            this.peerCache.rmZonePeer(nodeId);
            
        } else if(this.gem.containsKey(nodeId)) {
            
            info = this.gem.get(nodeId);
            
            Info tmp = new Info(addr6, 1, position, direction, speed, ++info.helloCounter);
            this.gem.put(nodeId, tmp);
        }
        
        return;
    }
    
    /*
    public void removePeer(ByteArray nodeId) {
        this.gem.remove(nodeId);
    }*/
    
    public void gcPeer() {
        
        long now  = System.currentTimeMillis();
        
        Iterator<Map.Entry<ByteArray, Info>> iter = this.gem.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<ByteArray, Info> entry = iter.next();
            Info info = entry.getValue();
            
            if((now - info.getTimeStamp() > config.getZoneTimeDelta())) {

                this.peerCache.addZonePeer(entry.getKey(), info);
                iter.remove();
            }
        }
        
    }
    
    ArrayList<byte[]> printPeers() {
        ArrayList<byte[]> out = new ArrayList<>();
        
        for(ByteArray peer : this.gem.keySet()) {
            System.out.print("|" + Crypto.toHex(peer.getData()) + "|");
        }
        
        System.out.println("");
        return out;
    }
    
    int getCount() {
        return this.vehicles;
    }
    
    
    LinkedList<InetAddress> getPeer(ByteArray nodeIdDst_new) {
        
        LinkedList<InetAddress> out = null;
                
        if(this.gem.containsKey(nodeIdDst_new)){
            out = new LinkedList<>();
            out.add(this.gem.get(nodeIdDst_new).hop_addr);
        }
        
        return out;
    }

    LinkedList<InetAddress> getOptimal(Tuple tuple) {
        
        LinkedList<InetAddress> out = new LinkedList<>();
        
        //TARGET INFO
        ArrayList<Double> posData = ((ArrayList<Double>)tuple.x);
        long timestamp = ((long)(tuple.y));
        
        Double positionX    = posData.get(0);
        Double positionY    = posData.get(1);
        Double directionX   = posData.get(2);
        Double directionY   = posData.get(3);
        
        Double lowestDist = 1000000000.0;
        InetAddress bestHop = null;
        for(Info info : this.gem.values()){
        
            
            //POINT DISTANCE

            Double[] resultX = {((Double)(info.position.x))-positionX,((Double)(info.direction.x))-directionX};
            Double[] resultY = {((Double)(info.position.x))-positionY,((Double)(info.direction.y))-directionY};

            Double[] quadResultX = {Math.pow(resultX[0],2),2*resultX[0]*resultX[1],Math.pow(resultX[1],2)};
            Double[] quadResultY = {Math.pow(resultY[0],2),2*resultY[0]*resultY[1],Math.pow(resultY[1],2)};

            Double[] inner = {quadResultX[0]+quadResultY[0], quadResultX[1]+quadResultY[1], quadResultX[2]+quadResultY[2]}; 

            Double[] innerDeriv = {inner[1], 2*inner[2]};

            Double[] finalDeriv = {(innerDeriv[0]*inner[0])/2, (innerDeriv[0]*inner[1])/2 + (innerDeriv[1]*inner[0])/2, 
                                       (innerDeriv[0]*inner[2])/2 + (innerDeriv[1]*inner[1])/2, (innerDeriv[1]*inner[2])/2};



            double[] coefficients = {finalDeriv[0], finalDeriv[1], finalDeriv[2], finalDeriv[3]};
            
            LaguerreSolver solver = new LaguerreSolver();
            Complex[] result = solver.solveAllComplex(coefficients, 0, 5000);

            Double[][] posSol = new Double[3][2];
            Double[] distSol  = new Double[3];

            int i = 0;


            if(result != null){
                for(Complex c : result) {
                    Double outValue = c.getReal();

                    posSol[i][0] = resultX[0] + resultX[1] * outValue;
                    posSol[i][1] = resultY[0] + resultY[1] * outValue;

                    distSol[i] = Math.sqrt(Math.pow(resultX[0] + (outValue * resultX[1]),2) + Math.pow(resultY[0] + (outValue * resultY[1]),2));
                }
            }
            
            if(distSol != null){
                
                for(Double tmp : distSol){ 
                    
                    if(tmp!= null && tmp<lowestDist && tmp >= 0){
                        lowestDist = tmp;
                        bestHop = info.hop_addr;
                    }
                }
            }
        }
        
        out.add(bestHop);
        
        if(out.size() == 0) return null;
        return out;
    }

    Double pointDist(Double pointAx, Double pointAy, Double pointBx, Double pointBy) {
    
        return Math.sqrt(Math.pow(pointAx-pointBx, 2) + Math.pow(pointAy-pointBy, 2));
    }
    
    Double vectorLen(Double dirX, Double dirY) {
        return Math.sqrt(Math.pow(dirX, 2) + Math.pow(dirY,2));
    }
    
    Double vectorMult(Double[] orient, Double dirX, Double dirY) {
        return (orient[0]*dirX)+(orient[1]*dirY);
    }
    
    Double vectorAngle(Double[] orient, Double dirX, Double dirY) {
        return Math.acos(vectorMult(orient, dirX, dirY)/(vectorLen(orient[0], orient[1])*vectorLen(dirX, dirY)));
    }
    
    Double crossProduct(Double[] orient, Double dirX, Double dirY) {
        return vectorLen(orient[0], orient[1])*vectorLen(dirX, dirY)*Math.sin(vectorAngle(orient, dirX, dirY));
    }
    
    int vectorOrientation(Double peerX, Double peerY, Double myX, Double myY) {
        /*
        Double[] upLeft = {-1.0, 1.0};
        Double[] upRight= {1.0, 1.0};
        Double[] downLeft = {-1.0, -1.0};
        Double[] downRight = {1.0, -1.0};
        */
        
        Double dirX = (peerX) - myX;
        Double dirY = (peerY) - myY;
        
        if(dirX >0.0){
        
            if(dirY > 0.0){
                return 0;
            }else{
                return 3;
            }
        }else{
        
            if(dirY > 0.0){
                return 1;
            }else{
                return 2;
            }
        }
        
    }
    
    LinkedList<InetAddress> getOrientUnic(LinkedList<InetAddress> usedPeers, Tuple mypos) {
        
        LinkedList<InetAddress> out = new LinkedList<>();
        
        Double dist = 0.0;
        
        Tuple[] hops = new Tuple[4];
        for(int i=0; i<4; i++){
            hops[i] = new Tuple(new Double(10000.0), null);
        }
                
        for(Info info : this.gem.values()){
            dist = pointDist(((Double)(info.position.x)), ((Double)(info.position.y)), ((Double)(mypos.x)), ((Double)(mypos.y)));
            
            switch(vectorOrientation(((Double)(info.position.x)), ((Double)(info.position.y)), ((Double)(mypos.x)), ((Double)(mypos.y)))) {
                
                //NORTHRight
                case 0:
                    System.out.println("NORTHRight");
                    if(dist>((Double)(hops[0].x)))
                        hops[0].x = dist;
                        hops[0].y = info.hop_addr;
                    break;
                //NORTHLeft
                case 1:
                    System.out.println("NORTHLeft");
                    if(dist>((Double)(hops[1].x)))
                        hops[1].x = dist;
                        hops[1].y = info.hop_addr;
                    break;
                //SOUTHLeft
                case 2:
                    System.out.println("SOUTHLeft");
                    if(dist>((Double)(hops[2].x)))
                        hops[2].x = dist;
                        hops[2].y = info.hop_addr;
                    break;
                //SOUTHRight
                case 3:
                    System.out.println("SOUTHRight");
                    if(dist>((Double)(hops[3].x)))
                        hops[3].x = dist;
                        hops[3].y = info.hop_addr;
                    break;
                default:
                    break;
            }
        }
        
        for(Tuple it : hops){
            if(it.y != null){
                out.add(((InetAddress)(it.y)));
            }
        }
        
        if(out.size() == 0) return null;
        else return out;
    }

    Tuple getPosPeer(ByteArray nodeIdDst_new) {
        
        Tuple tuple = null;
        ArrayList<Double> out = null;
        
        if(this.gem.containsKey(nodeIdDst_new)) {
        
            Info info = this.gem.get(nodeIdDst_new);
            
            out = new ArrayList<>(4);
            
            out.add(((Double)info.position.x));
            out.add(((Double)info.position.y));
            out.add(((Double)info.direction.x));
            out.add(((Double)info.direction.y));
            out.add(((Double)info.speed));
            
            tuple = new Tuple(out, info.timestamp);
        }
        
        return tuple;
    }

    boolean existsPeer(ByteArray nodeID) {
        return this.gem.containsKey(nodeID);
    }
    /*
    //ROUTES COM RANk
    //RETURN TUPLE <ADDR,DIST>
    public LinkedList<Tuple> getRankRoutes(int maxHops) {
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.gem.forEach((k1, v1) -> {
            ((HashMap<ByteArray, Info>)v1.y).forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(v2.hop_addr, v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    //ROUTES SEM RANK
    //RETURN TUPLE <ADDR,DIST>
    public LinkedList<Tuple> getRoutes(int maxHops) {
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.gem.forEach((k1, v1) -> {
            ((HashMap<ByteArray, Info>)v1.y).forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(v2.hop_addr, v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    //REUTRN TUPLE <BYTE[], DIST>
    public LinkedList<Tuple> getPeers(int maxHops) {
        
        LinkedList<Tuple> peers = new LinkedList<>();
        
        this.gem.forEach((k1, v1) -> {
            ((HashMap<ByteArray, Info>)v1.y).forEach((k2, v2) -> {
                if(v2.hop_dist <= maxHops) {
                    Tuple tuple = new Tuple(k1.getData(), v2.hop_dist);
                    peers.push(tuple);
                }
            });
        });
        
        return peers;
    }
    
    public Tuple getPeer(ByteArray nodeId) {
        Tuple tuple = null;
        
        if(this.gem.containsKey(nodeId)){
            InetAddress peer = null;
            int minDist = config.getZoneSize(); //MAX DIST is BORDER
            //HashMap<ByteArray, Info> routes = this.gem.get(nodeId);
            Tuple routes = this.gem.get(nodeId);
            
            for(Map.Entry<ByteArray, Info> pair : ((HashMap<ByteArray, Info>)(routes.y)).entrySet()) {
                if(pair.getValue().getHop_dist() <= minDist) {
                    peer    = pair.getValue().getHop_addr();
                    minDist = pair.getValue().getHop_dist();
                }
            }
            if(peer != null)    tuple = new Tuple(minDist, peer);
        }
        
        return tuple;
    }

    public byte[] getNodeId(InetAddress nodeHopAddr) {
        byte[] peerId = null;
        
        //for(Map.Entry<ByteArray, HashMap<ByteArray, Info>> pair1 : this.gem.entrySet()) {
        for(Map.Entry<ByteArray, Tuple> pair1 : this.gem.entrySet()) {
            
            for(Map.Entry<ByteArray, Info> pair2 : ((HashMap<ByteArray, Info>)(pair1.getValue().y)).entrySet()) {
                
                if(pair2.getValue().hop_addr.equals(nodeHopAddr)) {
                    return pair2.getKey().getData();
                }
            }
        }
        
        return peerId;
    }

    //NESTE MOMENTO ESTA PARA TODOS
    LinkedList<InetAddress> getReqRankPeers(InetAddress hopVAI, InetAddress hopVEM) {
        LinkedList<InetAddress> ip_list = null;
        LinkedList<Tuple> tuple_list = getRoutes(1);
        
        if(hopVAI != null && hopVEM != null){
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    if(!hopVAI.equals((InetAddress)tup.x) && !hopVEM.equals((InetAddress)tup.x)) ip_list.push((InetAddress)tup.x);
                }
            }
        }else if(hopVAI != null) {
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    if(!hopVAI.equals((InetAddress)tup.x)) ip_list.push((InetAddress)tup.x);
                }
            }
        }else {
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    ip_list.push((InetAddress)tup.x);
                }
            }
        }
        return ip_list;
    }
    
    
    
    //NESTE MOMENTO ESTA PARA TODOS
    LinkedList<InetAddress> getReqPeers(InetAddress hopAddr) {
        LinkedList<InetAddress> ip_list = null;
        LinkedList<Tuple> tuple_list = getRoutes(1);
        
        if(hopAddr != null){
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    if(!hopAddr.equals((InetAddress)tup.x)) ip_list.push((InetAddress)tup.x);
                }
            }
        }else {
            if(tuple_list.size() > 0) {
                ip_list = new LinkedList<>();

                for(Tuple tup : tuple_list) {
                    ip_list.push((InetAddress)tup.x);
                }
            }
        }
        return ip_list;
    }
*/
    
    
}
