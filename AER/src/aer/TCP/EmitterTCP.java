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
public class EmitterTCP implements Runnable{
    Scanner sc;
    Socket s;
    Scanner sc1;
    PrintStream p;     
    String news;

    public EmitterTCP() throws SocketException {
        try {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            this.s = new Socket(InetAddress.getByName("127.0.0.1"), 9999);
        } catch (IOException ex) {
            Logger.getLogger(EmitterTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            String[] splited = inFromUser.readLine().split(" "); //separa os parametros de entrada

            if (splited[0].equals("GET_NEWS_FROM")){
                DataOutputStream outToServer = new DataOutputStream(this.s.getOutputStream());

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
                outToServer.writeBytes(splited[1] + "\n");
                news = inFromServer.readLine();
                System.out.println("FROM SERVER: " + news);
            }else{
                System.out.println("Comando Não encontrado");
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(EmitterTCP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmitterTCP.class.getName()).log(Level.SEVERE, null, ex);
        }


//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
