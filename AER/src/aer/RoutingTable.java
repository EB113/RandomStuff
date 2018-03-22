/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author pedro
 */
public class RoutingTable {
    public RoutingTable() {
      // Inserir Valores nos objetos
      TableData HashTable1 = new TableData("1","::1", 0, 20, 0.0, 1,"2");
      TableData HashTable2 = new TableData("2","::2", 0, 25, 0.0, 1,"21");
      TableData HashTable3 = new TableData("3","::3", 0, 40, 0.0, 1,"23");
      List<TableData> lista = new ArrayList<>();
      lista.add(HashTable1);
      lista.add(HashTable2);
      lista.add(HashTable3);
      Integer i;
     //print dos objetos
     HashTable1.setId("4");
     //HashTable1.id = "5"; //dá se os atributos estiverem publicos
     for (i=0; i < 3; i++){
     System.out.println(lista.get(i).getId() + "|" + lista.get(i).getIpv6() + "|" + lista.get(i).getNumHop() + "|" + lista.get(i).getTs() + "|" + lista.get(i).getMetrica()+ "|" + lista.get(i).getDistancia()+ "|" + lista.get(i).getOrigem());
    }
      // Put elements to the map
     /* HashTable.put("1", new TableData("::1", 0, 20, 0.0, 1,"2"));
      HashTable.put("2", new TableData("::2", 0, 20, 0.0, 1,"3"));
      HashTable.put("3", new TableData("::3", 0, 20, 0.0, 1,"4"));
      // Get a set of the entries
      Set set = HashTable.entrySet();
      
      // Get an iterator
      Iterator i = set.iterator();
      System.out.println(HashTable.get("1"));
      // Display elements
      while(i.hasNext()) {
         Map.Entry me = (Map.Entry)i.next();
         System.out.print(me.getKey() + ": ");
         System.out.println(me.getValue());
      }
      System.out.println();*/
   }
}
