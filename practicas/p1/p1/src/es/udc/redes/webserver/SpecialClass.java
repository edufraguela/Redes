
package es.udc.redes.webserver;

/**
 *
 * @author Eduardo Pérez Fraguela
 */
public enum SpecialClass {
    
    OK("200 OK\n"),
    BADREQUEST("400 BAD REQUEST\n"),
    FORDIBBEN("403 FORDIBBEN\n"),
    NOTFOUND("404 NOT FOUND\n"),
    NOTMODIFIED("304 NOT MODIFIED\n");
    
    private final String state;
    SpecialClass(String state){
        this.state = state;
    }
    
    /**
     * Objective: Gets the state
     * @return : state
     */
    public String getState(){
        return state;
    }
    
}
