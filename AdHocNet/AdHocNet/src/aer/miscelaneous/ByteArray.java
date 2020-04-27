/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import java.util.Arrays;
/**
 *
 * @author pedro
 */
public class ByteArray {
    private byte[] data;

    public ByteArray(byte[] data) {
        this.data = Crypto.clone(data);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int hashCode() {
       return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
       if (this == obj)
          return true;
       if (obj == null)
          return false;
       if (getClass() != obj.getClass())
          return false;
       ByteArray other = (ByteArray) obj;
       if (!Arrays.equals(data, other.data))
          return false;
       return true;
    }

}
