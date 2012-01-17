package org.indivo.client;

//
public class IndivoClientException extends Exception {
    private static final long serialVersionUID = 1L;

    
    public IndivoClientException(Throwable thrwbl) {
        super(thrwbl);
    }
    public IndivoClientException(String msg, Throwable thrwbl) {
        super(msg, thrwbl);
    }
    public IndivoClientException(String msg) {
        super(msg);
    }
}
