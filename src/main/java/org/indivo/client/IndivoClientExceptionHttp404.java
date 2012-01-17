package org.indivo.client;

/**
*  Use this to mark an IndivoClientException as caused by HTTP 404 exception.
*  In some cases, 404 is among the possible expected results, not an error.
*/
public class IndivoClientExceptionHttp404 extends IndivoClientException {
    private static final long serialVersionUID = 1L;

    
    public IndivoClientExceptionHttp404(Throwable thrwbl) {
        super(thrwbl);
    }
    public IndivoClientExceptionHttp404(String msg, Throwable thrwbl) {
        super(msg, thrwbl);
    }
    public IndivoClientExceptionHttp404(String msg) {
        super(msg);
    }
}
