/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

/**
 *
 * @author Eduardo Pérez Fraguela
 */
public class WebServer {
     static String directory_Index;
     static String directory;
     static boolean allow;
     static int puertoServidor;
     
     /**
      * Objective: Is the main program of the WebServer
      * @param argv : String
      */
    public static void main(String argv[]){
        InputStream in = null;
        ServerSocket socket = null;
        Properties config = new Properties();
        try {
            //Configuramos el servidor
            in = new FileInputStream("configuracion.properties");
            config.load(in);
            directory_Index = config.getProperty("Directory_index");
            directory = config.getProperty("Directory");
            allow = Boolean.valueOf(config.getProperty("Allow"));
             puertoServidor = Integer.parseInt(config.getProperty("Port"));
            //Creamos el socket del Servidor
            socket = new ServerSocket(puertoServidor);
            
            //Establecemos el tiempo de espera
            socket.setSoTimeout(300000);
            while (true){
                //Esperamos las posibles conexiones
                Socket socketCliente = socket.accept();
                
                
                ServerThread thread = new ServerThread(socketCliente);
                thread.start();
            }
            
        } catch (SocketTimeoutException e){
            System.err.println("300 segs sin recibir nada");
        }catch (Exception e){
            System.err.println("Error: "+e.getMessage());
        }finally {
            try {
                socket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    
    
}
