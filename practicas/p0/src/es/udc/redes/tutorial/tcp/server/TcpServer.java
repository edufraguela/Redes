package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monothread TCP echo server.
 */
public class TcpServer {

    public static void main(String argv[]) throws IOException {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.TcpServer <port>");
            System.exit(-1);
        }
        
        ServerSocket servidor = null;
        final int PUERTO;
            
        try {
            
            PUERTO = Integer.parseInt(argv[0]);
            
            servidor = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado");
            // Create a server socket
            
            servidor.setSoTimeout(30000);
            System.out.println("Servidor iniciado");
            System.out.println("IP:" + servidor.getInetAddress());
            System.out.println("PUERTO:" + PUERTO + '\n');
            // Set a timeout of 300 secs
            
            while (true) {
                
                ServerThread st = new ServerThread(servidor.accept());
                // Wait for connections
                //Create a ServerThread object, with the new connection as parameter
                
                st.start();
                // Initiate thread using the start() method
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
                if(servidor != null){
                    try{
                    servidor.close();
                        System.out.println("Servidor cerrado");
                    }catch(IOException ex){
                        Logger.getLogger(TcpServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Close the socket
            }
        }
    }
}
