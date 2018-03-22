/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;
/**
 *
 * @author toms
 */
public class TableData {
    private
    String id;
    String ipv6;
    Integer numHop;
    Integer ts;
    Double metrica;
    Integer distancia;
    String origem;
public TableData(String id, String ipv6, Integer numHop, Integer ts, Double metrica, Integer distancia, String origem) { 
     this.id = id;
     this.ipv6 = ipv6;
     this.numHop = numHop;
     this.ts = ts;
     this.metrica = metrica;
     this.distancia = distancia;
     this.origem = origem;
     }   
    public String getId(){
        return id;
    }
    public String getIpv6(){
        return ipv6;
    }
    public Integer getNumHop(){
        return numHop;
    }
    public Integer getTs(){
        return ts;
    }
    public Double getMetrica(){
        return metrica;
    }
    public Integer getDistancia(){
        return distancia;
    }
    public String getOrigem(){
        return origem;
    }
    public void setId(String id){
       this.id = id;
    }
    public void setIpv6(String ipv6){
     this.ipv6 = ipv6;
    }
    public void setNumHop(Integer numhop){
     this.numHop = numhop;
    }
    public void setTs(Integer ts){
        this.ts = ts;
    }
    public void setMetrica(Double metrica){
        this.metrica = metrica;
    }
    public void setDistancia(Integer distancia){
        this.distancia = distancia;
    }
    public void setOrigem(String origem){
        this.origem = origem;
    }
}