/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.TCP;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class ListenerTCP implements Runnable{

    ServerSocket s1;
    Socket ss;
    Scanner sc;
    PrintStream p;
    public ListenerTCP() throws SocketException {
       
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {

        try {
        String clientSentence;
        ServerSocket welcomeSocket = new ServerSocket(9999);

         while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            
            String News = "Noticias do Jornal Nacional Construtores dizem que são precisos mais 80 a 100 mil operários para suportar o acréscimo de produção de 4,5% previsto para este ano. Sindicatos reclamam melhores salários" + '\n';
            outToClient.writeBytes(News);
  }
        } catch (IOException ex) {
            Logger.getLogger(ListenerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
