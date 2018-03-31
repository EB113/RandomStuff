/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import java.net.InetAddress;

/**
 *
 * @author pedro
 */
/*
public class Tuple {

    public final InetAddress addr;
    public final byte[] pdu;

    public Tuple(InetAddress a, byte[] b) {
        this.addr = a;
        this.pdu = b;
    }
}
*/
public class Tuple<X, Y> { 
  public final X x; 
  public final Y y; 
  
  public Tuple(X x, Y y) { 
    this.x = x; 
    this.y = y; 
  } 
}