package es.udc.redes.tutorial.tcp.server;
import java.net.*;
import java.io.*;

/** Thread that processes an echo server connection. */

public class ServerThread extends Thread {

    private Socket sc;

    ServerThread(Socket sc) {
        this.sc = sc;
        // Store the socket sc
    }

    @Override
    public void run() {
        
        BufferedReader sInput = null;
        PrintWriter sOutput = null;

        try {
            
                sInput = new BufferedReader(new InputStreamReader(
                    sc.getInputStream()));
            // Set the input channel
            
            sOutput = new PrintWriter(new OutputStreamWriter(
                    sc.getOutputStream()), true);
            // Set the output channel
            
            String mensaje = sInput.readLine(); //Se queda a la espera
                System.out.println("SERVER: Recieved " + mensaje);
            // Receive the message from the client
            
             sOutput.println(mensaje);               
            // Sent the echo message to the client

            sInput.close();
            sOutput.close();
            sc.close();
            // Close the streams

        } catch (SocketTimeoutException e) {
        System.err.println("Nothing received in 300 secs");
        } catch (IOException e) {
        System.err.println("Error: " + e.getMessage());
        } finally {
                try {
                    if(sc != null){
                    sc.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            
        // Close the socket
        }
    }
}
