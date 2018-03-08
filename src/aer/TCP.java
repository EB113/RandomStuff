/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 *
 * @author pedro
 */
public class TCP implements Runnable{
   Socket connectionSocket;
 
    public TCP(Socket s){
        try{
                System.out.println("Client Got Connected  " );
                connectionSocket=s;
        }catch(Exception e){e.printStackTrace();}
    }

    public void run(){
            try{
                    BufferedReader reader =
                                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    BufferedWriter writer= 
                                    new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

                    writer.write("*** Welcome to the Calculation Server (Addition Only) ***\r\n");            
                    writer.write("*** Please type in the first number and press Enter : \r\n");
                    writer.flush();
                    String data1 = reader.readLine().trim();

                    writer.write("*** Please type in the second number and press Enter : \r\n");
                    writer.flush();
                    String data2 = reader.readLine().trim();

                    int num1=Integer.parseInt(data1);
                    int num2=Integer.parseInt(data2);

                    int result=num1+num2;            
                    System.out.println("Addition operation done " );

                    writer.write("\r\n=== Result is  : "+result);
                    writer.flush();
                    connectionSocket.close();
            }catch(Exception e){e.printStackTrace();}
    } 
}
