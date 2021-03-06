/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.Data.Node;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import java.net.InetAddress;
import aer.PDU.Hello;
import aer.PDU.Data;
import aer.PDU.RErr;
import aer.PDU.RRep;
import aer.PDU.RReq;
/**
 *
 * @author pedro
 */
public class Interpreter implements Runnable{
    private Controller  control;
    private Node        id;
    private byte[]      pdu;
    private Boolean     bool;
    private InetAddress origin;
    
    public Interpreter(Controller control, Node id, byte[] pdu, InetAddress origin) {
        this.pdu        = pdu;
        this.control    = control;
        this.bool       = this.control.getUDPFlag().get();
        this.origin     = origin;
        this.id         = id;
    }

    
    @Override
    public void run() {
        
        
        switch(this.pdu[0]) {
            case 0x00:
                    //System.out.println("--->Hello");
                    Hello.load(this.pdu, this.id, this.origin);
                    //this.id.print();
                break;
            case 0x01:
                    System.out.println("--->Request");
                    RReq.load(this.pdu, this.id, this.origin, this.control);
                break;
            case 0x02:
                    System.out.println("--->Reply");              
                    RRep.load(this.pdu, this.id, this.origin, this.control);
                break;
            case 0x03:
                    System.out.println("--->Error");
                    RErr.load(this.pdu, this.id, this.origin, this.control);
                break;
            case 0x04:
                    System.out.println("--->Data");
                    Data.load(this.pdu, this.id, this.origin, this.control);
                break;
            default:
                System.out.println("WRONG PDU TYPE!");
                break;
        }
    }
    
}
