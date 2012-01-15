package org.indivo.client;

/**
 *  Use this to mark an IndivoClientException as caused by ConnectException
 *  This may be a transient problem and app may want to try again later.
 *
 * @author nate
 */
public class IndivoClientConnectException extends IndivoClientException {
    private static final long serialVersionUID = 1L;

    
    public IndivoClientConnectException(Throwable thrwbl) {
        super(thrwbl);
    }
    public IndivoClientConnectException(String msg, Throwable thrwbl) {
        super(msg, thrwbl);
    }
    public IndivoClientConnectException(String msg) {
        super(msg);
    }
}
