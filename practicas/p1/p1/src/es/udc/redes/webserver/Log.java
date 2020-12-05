package es.udc.redes.webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 *
 * @author Eduardo Pérez Fraguela
 */
public class Log {
   private String request;
   private InetAddress ipClient;
   private final Date date;
   private int codEstado;
   private long length;
   private String messageError;
   private PrintWriter pr = null;
   
   /**
    * Objective: Sets the request
    * @param request : String
    */
    public void setRequest(String request) {
        this.request = request;
    }
    
    /**
     * Objective : Puts the date
     * @throws UnknownHostException happens when the exception is unknown
     */
    public Log() throws UnknownHostException{
        date = new Date();
    }
    
    /**
     * Objective: Sets the IP of the client
     * @param ip
     * @throws UnknownHostException happens when the exception is unknown
     */
    public void setIpClient(InetAddress ip) throws UnknownHostException{
        ipClient =ip;
    }
    
    /**
     * Objective: Sets the cod
     * @param codEstado : int
     */
    public void setCodEstado(int codEstado) {
        this.codEstado = codEstado;
    }
    
    /**
     * Objectives: Sets the length
     * @param length : long
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Objective: Sets the error messages
     * @param messageError : String
     */
    public void setMessageError(String messageError) {
        this.messageError = messageError;
    }

    /**
     * Objective: Returns the IP client
     * @return : IP of the client
     */
    public InetAddress getIpClient() {
        return ipClient;
    }
    
    /**
     * Objective: It's a printer of the program, write errors, exceptions, etc
     * @param state: SepceialClass
     * @param archive: String
     * @throws IOException happens when there is a wrong input or output
     */
    public void write(SpecialClass state,String archive) throws IOException{
      File in = new File(archive);
      switch (state){
          case OK : 
              codEstado=200;length=in.length();
              writeAccess();
              break;
         case NOTMODIFIED :
                codEstado=304;length=in.length();
                writeAccess();
                return;
        case BADREQUEST :
                writeError("PETICION NO COMPRENDIDA");
                break;
        case FORDIBBEN : 
                writeError("PETICION NO ACEPTADA");
                break;
         case NOTFOUND : 
                writeError("ARCHIVO NO ENCONTRADO");
                break;
              
      }
        
    }
    
   /**
    * Objective: Writes the Errors
    * @param error: String
    * @throws FileNotFoundException happens when the exception is unknown
    * @throws IOException happens when there is a wrong input or output
    */
   public void writeError(String error) throws FileNotFoundException, IOException{
        pr = new PrintWriter(new FileOutputStream("errores.log",true));
        pr.println("\nLínea Peticion: "+request+"\n");
        pr.println("Dirección IP del cliente: "+ipClient+"\n");
        pr.println("Fecha: "+date+"\n");
        pr.println("Mensaje de Error: "+error+"\n");
        pr.flush();
   }
   
   /**
    * Objective: Writes the acces, IP, date, length,...
    * @throws FileNotFoundException happens when the exception is unknown
    */
   public void writeAccess() throws FileNotFoundException{
     pr = new PrintWriter(new FileOutputStream("accesos.log",true));
     pr.println("\nLínea Peticion: "+request+"\n");
     pr.println("Direccion IP del cliente: "+ipClient+"\n");
     pr.println("Fecha: "+date+"\n");
     pr.println("Codigo de estado: "+codEstado+"\n");
     pr.println("Tamaño del objeto: "+length);
     pr.flush();
   }
    
}
