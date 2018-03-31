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
    InetAddress ia;
    Socket s;
    Scanner sc1;
    PrintStream p;
    public EmitterTCP() throws SocketException {
       
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void run() {
        try {
        /*int number, temp;
        sc = new Scanner(System.in);
        ia = InetAddress.getLocalHost();
        s = new Socket(ia, 9999);
        sc1 = new Scanner(s.getInputStream());
        System.out.println("Enter any number");
        number = sc.nextInt();
        p = new PrintStream(s.getOutputStream());
        p.println(number);
        temp = sc1.nextInt();
        System.out.println(temp);*/
        String ipad;
        BufferedReader ip = new BufferedReader(new InputStreamReader(System.in));
        ipad = ip.readLine();
        
        String[] splited = ipad.split(" "); //separa os parametros de entrada

        if (splited[0].equals("GET_NEWS_FROM")){
        ia = InetAddress.getByName(splited[1]);// recebe nome de servidor
        System.out.println("Ligar a Server: " + splited[1]); 
        String sentence;
        String news;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            try (Socket clientSocket = new Socket(ia, 9999)) {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                sentence = "GET_NEWS";
                //sentence = inFromUser.readLine();
                outToServer.writeBytes(sentence);
                news = inFromServer.readLine();
                System.out.println("FROM SERVER: " + news);
            }
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
