/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author pedro
 */
public class SchedulerTCP implements Runnable{
    ServerSocket mysocket;
    
    public SchedulerTCP() throws IOException{
            mysocket = new ServerSocket(9999);
    }

    public void run(){
            try{
                   
         while(true)
         {
            Socket sock = mysocket.accept();
            TCP server=new TCP(sock);
 
            Thread serverThread=new Thread(server);
            serverThread.start();
 
         }  
            }catch(Exception e){e.printStackTrace();}
    } 
   
}
