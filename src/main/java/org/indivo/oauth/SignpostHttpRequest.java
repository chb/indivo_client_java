package org.indivo.oauth;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;

import oauth.signpost.basic.HttpURLConnectionRequestAdapter;

/**
* HttpRequestAdapter returns null from its getMessagePayload().
* use this if payload has params that need to go in the signature base string.
*/
public class SignpostHttpRequest extends HttpURLConnectionRequestAdapter {
    
    private String payload = null;

    /**
    * @param connection same as for super class constructor.
    * @param payload the form encoded params.  This class does nothing to
    *    actually send the payload, just allows its contents to be included
    *    in building the signature base string.  You must still send the payload
    *    after signing the request.
    */
    public SignpostHttpRequest(HttpURLConnection connection, String payload) {
        super(connection);
        this.payload = payload;
    }
    
    /** overrides getMessagePayload() of HttpRequestAdapter */
    public java.io.InputStream getMessagePayload() {
        return new ByteArrayInputStream(payload.getBytes());
    }
}
