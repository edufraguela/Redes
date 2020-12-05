package es.udc.redes.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;


/**
 *
 * @author Eduardo Pérez Fraguela
 */
public class ServerThread extends Thread {
    private final Socket socket;
    private final String server;
    private String  version;
    private final PrintWriter sSalida;
    private final BufferedReader entrada;
    private final OutputStream output;
    private final Log log;
    private String request;
    private final ArrayList<String> receive;
    private Date clientDate;
    private boolean dinamicRequest;
    private boolean get;
    
    /**
     * Objective: Is the Thread of the Server
     * @param s: Socket
     * @throws IOException happens when there is a wrong input or output
     */
    public ServerThread(Socket s) throws IOException{
        socket = s;
        //Preparamos el canal de entrada
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //Establecemos el canal de salida
        output = socket.getOutputStream();
        sSalida= new PrintWriter(output,true);
        //Establecemos el nombre del servidor
        server = "Apache/2.4.7 (Unix)";
        //Creamos la clase que gestiona los ficheros log
        log = new Log();
        //Establecemos la version
        version = "HTTP/1.0";
        //Creamos arrayList
        receive= new ArrayList<>();
        //Inicializamos la fecha del cliente
        clientDate=null;
    }
    
    /**
     * Objective: Gets the server
     * @return : The Server
     */
    public String getServer(){
        return server;
    }
    
    /**
     * Objective: Gets the version
     * @return : The version
     */
    public String getVersion(){
        return version+" ";
    }
    
    /**
     * Objective : Process the type an choose an archive.
     * @param type: String
     * @return: An archive 
     */
    public String processType(String type){
        if (type == null) {
            return "application/octet-stream"; //Así puede leer los archivos que no tienen extensión
        }
        switch (type){
            case "html": return "text/html";
            case "txt" : return "text/plain";
            case "gif" : return "image/gif";
            case "jpeg": return "image/jpeg";
            case "jpg" : return "image/jpg";
            case "png" : return "image/png";
            case "log" : return "text/plain";
            default : return "application/octet-stream";
            
        }
    }
    
    /**
     * Objective: Write the headers to be print
     * @param archive: String
     * @param type: String
     * @return : String
     */
    public String writeHeaders(String archive,String type){
        StringBuilder salida = new StringBuilder();
        File in = new File(archive);
        salida.append("Date: ").append(new Date()).append("\n");
        salida.append("Server: ").append(getServer()).append("\n");
        if (dinamicRequest)
            salida.append("Content-Length: ").append(archive.length()).append("\n");
        else
            salida.append("Content-Length: ").append(in.length()).append("\n");
        salida.append("Content-Type: ").append(processType(type)).append("\n");
        salida.append("Last-modified: ").append(new Date(in.lastModified()));
        salida.append("\n");
        return salida.toString();
    }
    
    /**
     * Objective: Is the response of the server, shows the content
     * @param state: SpecialClass
     * @param archive: String 
     * @throws FileNotFoundException happens when a file is not found
     * @throws IOException happens when there is a wrong input or output
     */
    public void response(SpecialClass state,String archive) throws FileNotFoundException, IOException{
        String[] parts = archive.split("\\.");
        StringBuilder salida = new StringBuilder();
        salida.append(getVersion());
        log.write(state,archive);
        switch (state) {
            case OK :
                salida.append(SpecialClass.OK.getState());
                if (dinamicRequest){
                    salida.append(writeHeaders(archive,"html"));
                    break;
                }
                
                salida.append(writeHeaders(archive,parts.length > 1 ? parts[1]: null)); //parts[1]
                break;
            case NOTMODIFIED :
                salida.append(SpecialClass.NOTMODIFIED.getState());
                salida.append("Date: ").append(new Date()).append("\n");
                salida.append("Server: ").append(getServer()).append("\n");
                sSalida.println(salida);
                return;
            case BADREQUEST :
                salida.append(SpecialClass.BADREQUEST.getState());
                archive="/Error400.html";
                salida.append(writeHeaders(archive,"html"));
                break;
            case FORDIBBEN :
                salida.append(SpecialClass.FORDIBBEN.getState());
                archive="/Error403.html";
                salida.append(writeHeaders(archive,"html"));
                break;
            case NOTFOUND :
                salida.append(SpecialClass.NOTFOUND.getState());
                archive="/Error404.html";
                salida.append(writeHeaders(archive,"html"));
                break;
        }
        sSalida.println(salida);
        if (get)
            sendFile(archive);
    }
    
    /**
     * Objective: Takes the link in html with the arch
     * @param list: String
     * @param arch: String
     * @return link
     * @throws IOException happens when there is a wrong input or output
     */
    public String takelinks(String[] list,String arch) throws IOException{
        String link="";
        link+= "<html>\n"
                +" <head>"
                + "<h1>Directory Index</h1><br><h2>" + arch + "</h2><br></head>\n"
                +"  <body>\n";
        for (String fich : list){
            if ("/".equals(arch)){
            link+="<a href=\"" + arch + fich+"\">"+fich+"</a>\n<br>"; 
            } else {
             
            link+="<a href=\"" + arch + "/" + fich+"\">"+fich+"</a>\n<br>";
        }
        }
        link+= "  </body>\n"
                +"</html>";
        return link;
        
    }
    
    /**
     * Objective: Process the archives
     * @param archive: String
     * @throws FileNotFoundException
     * @throws IOException happens when there is a wrong input or output
     */
    public void processArchive(String archive) throws FileNotFoundException, IOException{
        File in = new File(WebServer.directory+archive);
        String arch2 = archive.substring(1);
        Date lastModified = new Date(in.lastModified());
        if (clientDate != null){
            if ((clientDate.after(lastModified))|| (clientDate.equals(lastModified))){
                response(SpecialClass.NOTMODIFIED,arch2);
                clientDate=null;
                return;
            }
        }
        if (in.exists()){
            if (in.isDirectory()){
                File in2 = new File(WebServer.directory+archive+WebServer.directory_Index);
                if (in2.exists()){
                    response(SpecialClass.OK,WebServer.directory_Index);
                }
                else{
                    if (WebServer.allow){
                        dinamicRequest=true;
                        response(SpecialClass.OK,takelinks(in.list(),archive));
                        dinamicRequest=false;
                    }
                    else
                        response(SpecialClass.FORDIBBEN,archive);
                }
            }
            else {
                response(SpecialClass.OK,arch2);
            }
        }
        else{
            response(SpecialClass.NOTFOUND,arch2);
        }
    }
    
    /**
     * Objective: Send files 
     * @param archive: String
     * @throws IOException happens when there is a wrong input or output
     */
    public void sendFile(String archive) throws  IOException{
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        File arch = new File(archive);
        sSalida.flush();
        
        try {
            if (dinamicRequest){
                sSalida.println(archive);
                sSalida.flush();
                return;
            }
            in = new BufferedInputStream(new FileInputStream(arch));
            out = new BufferedOutputStream(output);
            byte[] array = new byte[1024];
            int c;
            while ((c = in.read(array))!= -1){
                out.write(array, 0, c);
            }
        }finally{
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
            }
        }
    }
    
    /**
     * Objective: Checks de version
     * @param version: String
     * @return : boolean
     */
    public boolean checkVersion(String version){
        switch (version){
            case "HTTP/1.0" : return false;
            case "HTTP/1.1" : return false;
            default : return true;
        }
    }
    
    /**
     * Objective: Comprove the request
     * @param request: String
     * @return : boolean
     */
    public boolean comproveRequest(String[] request){
        get = true;
        if (request.length!= 3)
            return false;
        if (checkVersion(request[2]))
            return false;
        version = request[2];
        return true;
    }
    
    /**
     * Objective: process the dynamic request
     * @param request: String
     * @throws Exception in general
     */
    public void dynamicRequest(String request) throws Exception{
        StringTokenizer st = new StringTokenizer(request,"=");
        dinamicRequest=true;
        Map<String,String> parameters = new HashMap<>();
        parameters.put(st.nextToken(),st.nextToken("&").substring(1));
        parameters.put(st.nextToken("=").split("&")[1],st.nextToken("&").substring(1));
        parameters.put(st.nextToken("=").split("&")[1],st.nextToken("&").substring(1));
        response(SpecialClass.OK,es.udc.redes.webserver.ServerUtils.processDynRequest("es.udc.redes.webserver.MiServlet", parameters));
        dinamicRequest=false;
    }
    
    /**
     * Objetive: process the archive
     * @param archive: String
     * @throws IOException happens when there is a wrong input or output
     * @throws ParseException happensa when the file syntax is not correct
     * @throws Exception in general
     */
    public void processGet(String archive) throws IOException, ParseException, Exception{
        get = true;
        SimpleDateFormat aux;
        String[] parts = archive.split("\\?");
        if (parts[0].equals("/MiServlet.do")){
            dynamicRequest(parts[1]);
            return;
        }
        for(int i=0;i<receive.size();i++){
            parts = receive.get(i).split(": ");
            if (parts[0].equals("If-Modified-Since")){
                aux=new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", new Locale("english"));
                clientDate = aux.parse(parts[1]);
            }
        }
        processArchive(archive);
    }
    
    /**
     * Objective: Procees de request
     * @return: boolean
     * @throws IOException happens when there is a wrong input or output
     * @throws ParseException happensa when the file syntax is not correct
     * @throws Exception in general
     */
    public boolean processRequest() throws IOException, ParseException, Exception {
        
        String message;
        int i=0;
        //Recibimos el mensaje del cliente
        request = entrada.readLine();
        String leido= entrada.readLine();
        if (leido ==null)
            return false;
        log.setRequest(request);
        log.setIpClient(socket.getInetAddress());
        
        while (leido.length() > 0){
            receive.add(i, leido);
            leido=entrada.readLine();
            i++;
        }
        
        if (request.equals("")){
            response(SpecialClass.BADREQUEST,"/Error400.html");
            get=true;
            return true;
        }
        
        String[] parts = request.split(" ");
        if (!comproveRequest(parts)){
            response(SpecialClass.BADREQUEST,parts[1]);
            return true;
        }
        
        switch (parts[0]){
            case ("GET") :get=true;processGet(parts[1]);
            break;
            case ("HEAD"):get=false;processArchive(parts[1]);
            break;
            default : get=true;response(SpecialClass.BADREQUEST,parts[1]);
            break;
        }
        receive.clear();
        return true;
    }
    
    @Override
    public void run(){
        try {
          socket.setSoTimeout(60000);
           while(true){
              if(!processRequest())
                  break;
           }
            
        } catch (SocketTimeoutException e){
            System.err.println("60 segundos sin recibir nada.");
        } catch (Exception e){
            System.err.println("Error :"+ e.getMessage());
            e.printStackTrace();
        }finally {
            try {
            //Cerramos los flujos y el socket 
            entrada.close();
            sSalida.close();
            socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    
}
}  
