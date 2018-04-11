/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.TCP;

import aer.miscelaneous.Controller;
import aer.miscelaneous.Crypto;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class EmitterTCP{

    
    public static void main(String argv[]) throws Exception
      {/*
          
        Socket s = new Socket(InetAddress.getByName("127.0.0.1"), 9999);
        System.out.println("Init Client!");
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.print("CMD:");
            String[] splited = inFromUser.readLine().split(" "); //separa os parametros de entrada

            if (splited[0].equals("GET")){

                DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                outToServer.writeBytes(splited[1] + "\n");
                String news = inFromServer.readLine();
                System.out.println("FROM SERVER: " + news);
            }else if(splited[0].equals("exit")){

                DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                outToServer.writeBytes(splited[0] + "\n");
                outToServer.close();
                System.out.println("Client Exiting...");
                return;
            }else{
                System.out.println("Comando Não encontrado");
            }
        }*/
      }
}
