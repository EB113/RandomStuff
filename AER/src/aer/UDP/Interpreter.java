/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.UDP;

import aer.Data.Node;
import aer.miscelaneous.Controller;
import aer.miscelaneous.Datagram;
import java.net.InetAddress;
import java.util.ArrayList;

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
                    Datagram.loadHello(this.pdu, this.id, this.origin);
                break;
            case 0x01:
                    Datagram.loadRReq(this.pdu);
                break;
            case 0x02:
                    Datagram.loadRRep(this.pdu);
                break;
            case 0x03:
                    Datagram.loadRErr(this.pdu);
                break;
            case 0x04:
                    Datagram.loadData(this.pdu);
                break;
            default:
                System.out.println("WRONG PDU TYPE!");
                break;
        }
    }
    
}
