/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.PDU;

import aer.Data.Node;
import aer.miscelaneous.Controller;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author pedro
 */
public class Data {

    public static void load(byte[] raw, Node id, InetAddress origin, Controller control) {
        int totalSize           = 0, hopCount = 0; //Obtained Variables
        int counter             = 1, limit = 1, it = 0; //Auxiliary variables
        
        byte secure             = 0x00;
        byte[] nodeIdSrc        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        byte[] nodeIdDst        = new byte[32]; //Estatico para reduzir trabalho e tamanho de PDU mas teria que ser feito
        
        
        
        //GET SECURITY BYTE
        secure  =   raw[1];
        
        
        /*
        //GET PDU TOTAL SIZE
        limit+=4;
        for(;counter<limit; counter++) tmp[it++] = raw[counter];
        ByteBuffer wrapped = ByteBuffer.wrap(tmp);
        totalSize = wrapped.getInt();
        it = 0;*/
        
        
        
    }
    
    
    
}
