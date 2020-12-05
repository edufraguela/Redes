package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements an UDP Echo Server.
 */
public class UdpServer {

    private static DatagramSocket DataSock;

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }
        
        final int PUERTO;   //Se declara la variable Puerto
        
        try {
            
            PUERTO = Integer.parseInt(argv[0]);
            DataSock = new DatagramSocket(PUERTO);  
            // Create a server socket
            
            DataSock.setSoTimeout(30000);
            byte[] paquete = new byte[1024];    //En redes se envia todo en paquetes de bytes.
            // Set max. timeout to 300 secs
            
            while (true) {
                
                DatagramPacket rdp = new DatagramPacket(paquete, paquete.length);
                // Prepare datagram for reception
                
                DataSock.receive(rdp);
                // Receive the message
                
                String mensaje = new String(rdp.getData()); //Guarda en string el datagrama.
                System.out.println(mensaje);    //Printea el mensaje 
                
                DatagramPacket sdp = new DatagramPacket(rdp.getData(), rdp.getLength(),
                    rdp.getAddress(), rdp.getPort());
                // Prepare datagram to send response
                
                DataSock.send(sdp);
                // Send response
                
            }
          
        // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
           System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if(DataSock != null)
                DataSock.close();
// Close the socket
        }
    }
}
