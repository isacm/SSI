/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author isacm
 */
public class Server {
    public static final int PORT = 8765;
    
    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Servidor a executar...");
       
       try(ServerSocket ss = new ServerSocket(PORT)){
           //ActorRef<Wrap> service = new MailService().spawn();
           
           while(true){
               Socket client = ss.accept();
               
               InputStream in = client.getInputStream();
               OutputStream out = client.getOutputStream();
               
               Protocol stationToStation = new Protocol(in, out);
               stationToStation.spawn();
               
           }
       } 
       catch (IOException ex) {
           System.out.println(ex.getMessage());
       }
    }
}
