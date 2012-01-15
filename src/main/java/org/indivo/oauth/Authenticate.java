package org.indivo.oauth;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.Date;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.security.Signature;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import javax.crypto.Mac;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.commons.codec.binary.Base64;

//import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.RequestParameters;
import oauth.signpost.signature.SignatureBaseString;


/**
*  Use this to ensure, via 2-legged OAuth, that a request comes from the
*  entity it says it comes from.
*/
public class Authenticate {

    private static final long serialVersionUID = 1L;
    private Log logger = null;

    /** keep track of latest request from each client to make sure timestamps are in sequence
    * and timstamp/nonce doesn't repeat
    */
    private Map<String,String[]> latestRequest = new HashMap<String,String[]>();

    private Base64 base64 = new Base64();
    //private oauth.signpost.signature.SignatureBaseString signatureBaseString =
    //        new oauth.signpost.signature.SignatureBaseString(null,null);

    /** main method strictly for testing */
    public static void main(String[] args) {
        Map<String,String[]> retVal = new HashMap<String,String[]>();
        String authHead = "OAuth realm=\"http://OSX1189.local/\"," +
            "oauth_consumer_key=\"1nd1v0K3y\"," +
            "oauth_signature_method=\"HMAC-SHA1\"," +
            "oauth_version=\"1.0\"," +
            "oauth_timestamp=\"1247764232\"," +
            "oauth_nonce=\"iBHJL1j3iX2rR04Y\"," +
            "oauth_signature=\"Bu6z0a9wFhl2LKzV8cCP8ibqPas=\"";
        Authenticate instance = new Authenticate();
        try {
            instance.getReqAndAuthorizParams0(retVal, authHead);
        } catch (Exception exe) {
            throw new RuntimeException(exe);
        }
        Iterator<String> keys = retVal.keySet().iterator();
        while (keys.hasNext()) {
            String akey = keys.next();
            String[] aval = retVal.get(akey);
            System.out.print(akey + '=');
            for (int ii = 0; ii < aval.length; ii++) {
                System.out.print("   " + aval[ii]);
            }
            System.out.println("");
        }
    }
    
    public Authenticate() {
        logger = LogFactory.getLog(this.getClass());
    }
    
    /**
    *  HMAC-SHA1 3-legged oauth authentication, final step of the dance,
    *  where access token and secret are already known to the consumer (Can also be used
    *  for 2-legged oauth (omit oauth_token from params and tokenSecret = null)
    *  @param req HttpServletRequest from the Servlet doGet() or doPost()
    *  @param consumerSecret consumer secret, as agreed to by service provider and consumer prior
    *  to starting oauth process.
    *  @param tokenSecret null if oauth_token not present in params and signature base string (two legged).
    *  token secret would have been provided by service provider along with oauth_token.
    *  @return true if authentication succeeds.
    */
    public boolean authenticate(HttpServletRequest req, String consumerSecret, String tokenSecret)
            throws GeneralSecurityException {
        boolean retVal = false;  // not authentic unless authenticated
        Map<String,String[]> apmap = getReqAndAuthorizParams(req);

        String signatureMethod = AuthUtils.getStringFromPmap(apmap,"oauth_signature_method");

        if (! signatureMethod.equals("HMAC-SHA1")) {
            throw new GeneralSecurityException("expected 'oauth_signature_method' of 'HMAC-SHA1'");
        }
        String sbs = startAuthenticate(req, apmap, null);
        logger.info("Authenticate.authenticate signature base string:\n" + sbs);
        if (sbs != null) {
            String oaSign = AuthUtils.getStringFromPmap(apmap, "oauth_signature");
            boolean tokenPresent = apmap.get("oauth_token") != null;
            String macSecret = consumerSecret + '&';
            if (tokenSecret != null) {
                if (! tokenPresent) {
                    throw new GeneralSecurityException(
                            "token_secret present, but no oauth_token param");
                }
                macSecret += tokenSecret;
            } else if (tokenPresent) {
                throw new GeneralSecurityException(
                        "token_secret abset, but oauth_token present");
            }
            
            Mac mac = AuthUtils.makeMac(macSecret);
            logger.info("makeMac(" + macSecret + ")");
            
            byte[] signature = mac.doFinal(sbs.getBytes());
            String sig64 = new String(base64.encode(signature));
            retVal = oaSign.equals(sig64);
        }
        return retVal;
    }

    /** convenience method for:
    * <code>authenticate(HttpServletRequest req, GetsPublicKey getsPublic, String providedSBSpart)</code>
    */
    public boolean authenticate(HttpServletRequest req, GetsPublicKey getsPublic)
            throws GeneralSecurityException {
        return authenticate(req, getsPublic, null);
    }

    /**
    *  2-legged oauth to ensure client is who he says he is, RSA flavor.
    *
    * @param req request being authenticated (the request param of the servlet
    * doGet/doPost.... method.
    * @param getsPublic An instance of a class that can get the public key.
    * @return true if authenticated, or false if not
    */
    public boolean authenticate(HttpServletRequest req, GetsPublicKey getsPublic, String providedSBSpart)
            throws GeneralSecurityException {
        boolean retVal = false;  // not authentic unless authenticated
        Map<String,String[]> apmap = getReqAndAuthorizParams(req);
        String sbs = startAuthenticate(req, apmap, providedSBSpart); // return null if early problem detected
        
        logger.info("sbs: " + sbs);
        if (sbs != null) {
            String signatureMethod = AuthUtils.getStringFromPmap(apmap,"oauth_signature_method");
            String keyName = AuthUtils.getStringFromPmap(apmap, "xoauth_signature_publickey");
            String oaSign = AuthUtils.getStringFromPmap(apmap, "oauth_signature");
            String consumerKey = AuthUtils.getStringFromPmap(apmap,"oauth_consumer_key");
            /*
            FROM http://wiki.opensocial.org/index.php?title=Validating_Signed_Requests:
    
            Certificates should not be fetched each time you want
            to validate parameters - instead,
            implement a server side key cache indexed on the value of
            xoauth_signature_publickey, oauth_consumer_key, and oauth_signature_method.
            If these value change, you will need to pull a new certificate down
            and store it in your key cache.
            */
            Object stringOrPublicKey =
                    getsPublic.getPublicKey(consumerKey, keyName, signatureMethod);
            PublicKey pkey = null;
            if (stringOrPublicKey instanceof String) {
//            String key509 =
            // copied from net.oauth, THANKS!
                CertificateFactory fac = CertificateFactory.getInstance("X509");
                ByteArrayInputStream in =
                        new ByteArrayInputStream(((String) stringOrPublicKey).getBytes());
                X509Certificate cert = (X509Certificate)fac.generateCertificate(in);
                pkey = cert.getPublicKey();
            // end copied from net.oauth
            } else {
                pkey = (PublicKey) stringOrPublicKey;
            }


            retVal = validatePublicKey((PublicKey) pkey, oaSign, sbs);
                        
            logger.info("signatureMethod, keyName, oaSign, consumerKey, PKValid<br/>key509: "
                + signatureMethod + ", " + keyName + ", " + oaSign + ", " + consumerKey + ", " + retVal
                + "\n" + stringOrPublicKey);
        }
        return retVal;
    }
    
    /**
    * Do some validation checks, then return signature base string for further validation.
    * @param req http request object
    * @return signature base string, or null if already known to be invalid.
    */
    private String startAuthenticate(HttpServletRequest req, Map<String,String[]> apmap, String providedSBSpart)
            throws GeneralSecurityException {
        String sbs = null; // not authentic unless authenticated
        
        // oauth_version: OPTIONAL. If present, value MUST be 1.0
        boolean versionOK = false;
        String[] oauthVersionA =  apmap.get("oauth_version");
        if (oauthVersionA != null) {
            if (oauthVersionA.length != 1 || (! oauthVersionA[0].equals("1.0"))) {
                StringBuffer vstring = new StringBuffer();
                for (int ii = 0; ii < oauthVersionA.length; ii++) {
                    vstring.append(oauthVersionA[ii]);
                    if (ii < oauthVersionA.length -1) {
                        vstring.append(", ");
                    }
                }
                logger.warn("oauth_version != '1.0': " + vstring);
            } else {
                versionOK = true;
            }
        }

        boolean timestampNonceOK = false;
        if (versionOK) {
            String timestamp = AuthUtils.getStringFromPmap(apmap,"oauth_timestamp");
            String nonce = AuthUtils.getStringFromPmap(apmap, "oauth_nonce");
            String consumerKey = AuthUtils.getStringFromPmap(apmap,"oauth_consumer_key");
            timestampNonceOK = validateTimestampNonce(consumerKey, timestamp, nonce);
        }
        
        if (timestampNonceOK) {
            logger.info("contextPath: " + req.getContextPath()
                    + "  servletPath: " + req.getServletPath()
                    + "  pathInfo: " + req.getPathInfo());

            String requestUrl = getRequestUrl(req);
//            List<oauth.signpost.Parameter> signpostParams = paramsToSPParams(apmap);
            String selectedReqParams = null;
            
            if (providedSBSpart == null) {
                RequestParameters signpostParams = paramsToSPParams(apmap);
                SignatureBaseString signatureBaseString = new SignatureBaseString(null, signpostParams);
                try {
                    selectedReqParams = signatureBaseString.normalizeRequestParameters();
                    logger.info("using signpost: " + selectedReqParams);
                    
    //                getSelectedReqParams(apmap);
                } catch (IOException ioe) {
                    throw new GeneralSecurityException(ioe);
                }
            }
            else {
                selectedReqParams = providedSBSpart;
            }
            sbs = req.getMethod() + '&' +
                    AuthUtils.encode(requestUrl) + '&'
                    + AuthUtils.encode(selectedReqParams);
        }

        return sbs; // or null if request already known to be invalid
    }

    private RequestParameters paramsToSPParams(Map<String,String[]> pmap)
            throws GeneralSecurityException {
        RequestParameters retVal = new RequestParameters();
        Iterator<String> keyI = pmap.keySet().iterator();
        while (keyI.hasNext()) {
            String akey = keyI.next();
            String[] vals = pmap.get(akey);
            for (int ii = 0; ii < vals.length; ii++) {
                retVal.put(akey, vals[ii]);
            }
        }
        return retVal;
    }
    
    /*
    private List<oauth.signpost.Parameter> paramsToSPParams(Map<String,String[]> pmap) {
        List<oauth.signpost.Parameter> retVal = new ArrayList<oauth.signpost.Parameter>();
        Iterator<String> keyI = (Iterator<String>) pmap.keySet().iterator();
        while (keyI.hasNext()) {
            String akey = keyI.next();
            String[] vals = pmap.get(akey);
            for (int ii = 0; ii < vals.length; ii++) {
                if (! akey.equals("realm")) {
                    retVal.add(new oauth.signpost.Parameter(akey, vals[ii]));
                }
            }
        }
        return retVal;
    }*/
    
    /**
    * The timestamp value MUST be a positive integer and MUST be equal or greater than the
    * timestamp used in previous requests.
    */
    private boolean validateTimestampNonce(
            String consumerKey, String oauth_timestamp, String oauth_nonce) {
        long timestamp = 0;
        boolean goodNumber = true;
        boolean current = true;
        boolean insequence = true;
        boolean unique = true;
        
        try {
            timestamp = Long.parseLong(oauth_timestamp) * 1000L;
        } catch (NumberFormatException nfe) {
            logger.warn("NumberFormatException when parsing timestamp: " + timestamp);
            goodNumber = false;
        }
        
        // also make sure this is reasonably close to NOW
        long now = new Date().getTime();
        long min = now - (5 * 60 * 1000); // 5 minutes
        long max = now + (5 * 60 * 1000); // 5 minutes
        if (timestamp < min || max < timestamp) {
            logger.warn("timestamp far from now. timestamp: " + timestamp + ", now: " + now);
            current = false;
        }
        
        // a bit unclear what "...used in previous requests" means. Here we assume
        // this refers to previous requests from the same consumer
        String[] lrts = latestRequest.get(consumerKey);
        if (lrts != null) {
            long lastTS = Long.parseLong(lrts[0]);
            if (lastTS > timestamp) {
                logger.warn("timestamp older than prior timestamp from same consumer. "
                    + "consumer_key: " + consumerKey + ", prior timestamp: " + lrts
                    + ", this timestamp: " + timestamp);
                insequence = false;
            } else if (lastTS == timestamp) {
                // also unclear what "...unique for all requests with that timestamp." means
                // assume unique for all requests from that consumer with that timestamp
                if (lrts[1].equals(oauth_nonce)) {
                    unique = false;
                }
            }
        }
        String[] lrts0 = new String[2];
        lrts0[0] = oauth_timestamp;
        lrts0[1] = oauth_nonce;
        latestRequest.put(consumerKey, lrts0);
        
        if (goodNumber && current && insequence && unique) {
            return true;
        }
        else {
            return false;
        }
    }


    /** use java.security package and the consumer's public key to authenticate
    */
    private boolean validatePublicKey(
            PublicKey pkey,
            String oaSign,
            String sbs) throws GeneralSecurityException {
        boolean retVal = false;
            
        Signature sha1withrsa = Signature.getInstance("SHA1withRSA");
        sha1withrsa.initVerify(pkey);   // init with public key
        sha1withrsa.update(sbs.getBytes());
        retVal = sha1withrsa.verify(base64.decode(oaSign.getBytes()));
        return retVal;
    }

    
    /**
    * COPIED FROM: http://wiki.opensocial.org/index.php?title=Validating_Signed_Requests
    * Constructs and returns the full URL associated with the passed request
    * object.
    * 
    * @param  request Servlet request object with methods for retrieving the
    *         various components of the request URL
    */
    public static String getRequestUrl(HttpServletRequest request) {
        StringBuilder requestUrl = new StringBuilder();
        String scheme = request.getScheme().toLowerCase();
        int port = request.getLocalPort();
    
        requestUrl.append(scheme);
        requestUrl.append("://");
        requestUrl.append(request.getServerName().toLowerCase());
    
        if ((! (scheme.equals("http") && port == 80))
            && (! (scheme.equals("https") && port != 443)) ) {
          requestUrl.append(":");
          requestUrl.append(port);
        }
    
        requestUrl.append(request.getContextPath());
        requestUrl.append(request.getServletPath());
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            requestUrl.append(pathInfo);
        }
    
        return requestUrl.toString();
    }
      
                 
    /**
    * see "http://oauth.net/core/1.0"  section: "9.1.  Signature Base String"
    *
    * @param req request param from the HttpServelt doGet/doPost.... method.
    * @return parameters portion of signature base string.
    */
    public Map<String,String[]> getReqAndAuthorizParams(HttpServletRequest req)
            throws GeneralSecurityException {
        // get authorization header params first, excluding "realm"
        Map<String,String[]> retVal = new HashMap<String,String[]>();

        Enumeration<String> authHeads =
                (Enumeration<String>) req.getHeaders("Authorization");
        if (authHeads == null) {
            throw new GeneralSecurityException("unable to access 'Authorization' header");
        }
        while (authHeads.hasMoreElements()) {
            String authHead = authHeads.nextElement().trim();
            getReqAndAuthorizParams0(retVal, authHead);
        }

          
        // now get post body and get url params
        Map<String,String[]> pmap = (Map<String,String[]>) req.getParameterMap();
        Iterator<String> keys = pmap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String[] valueA = pmap.get(key);
            for (int ii = 0; ii < valueA.length; ii++) {
                addToMapStringStringA(retVal, key,valueA[ii]);
            }
        }
        
        return retVal;
    }


    /**
    * mutate retVal
    */
    private void getReqAndAuthorizParams0(Map<String,String[]> retVal, String authHead)
            throws GeneralSecurityException {
        if (authHead.startsWith("OAuth ")) {
            logger.info("authHead: " + authHead);
            String oahWork = authHead.substring(6).trim();  // skip over 'OAuth'
            while (oahWork.length() > 0) {
                int eix = oahWork.indexOf('=');
                if (eix == -1) {
                    throw new GeneralSecurityException(
                        "unexpected Authorization header format:" + oahWork);
                }
                String key = oahWork.substring(0, eix);
                String tail = oahWork.substring(eix +1);
                if (tail.charAt(0) != '"') {
                    throw new GeneralSecurityException(
                            "Authorization header value not quoted: " + oahWork);
                }
                int qix = tail.indexOf('"', 1);
                if (qix == -1) {
                    throw new GeneralSecurityException("close quote not found: " + oahWork);
                }
                String value = tail.substring(1, qix);
                addToMapStringStringA(retVal, key,value);
                oahWork = tail.substring(qix +1).trim();
                if (oahWork.length() > 0 && oahWork.charAt(0) == ',') {
                    oahWork = oahWork.substring(1).trim();
                    if (oahWork.length() == 0) {
                        throw new GeneralSecurityException("trailing comma in OAuth header");
                    }
                } else if (oahWork.length() > 0) {
                    throw new GeneralSecurityException("missing delimiter: " + oahWork);
                }
            }
        }
    }

/*
    private String getSelectedReqParams(Map<String,String[]> apmap) 
            throws GeneralSecurityException {
        List<String[]> siglist = new ArrayList<String[]>();
        Iterator<String> keys = apmap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (! (key.equals("realm") || key.equals("oauth_signature")) ) {
                String[] valueA = apmap.get(key);
                for (int ii = 0; ii < valueA.length; ii++) {
                    String[] sla = new String[2];
                    sla[0] = AuthUtils.encode(key); sla[1] = AuthUtils.encode(valueA[ii]);
                    siglist.add(sla);
                    logger.info("selected req param: " + sla[0] + '=' + sla[1]);
                }
            }
        }
        logger.info("alternate SBS: " + AuthUtils.saListToAmpString(siglist));
        return AuthUtils.saListToAmpString(siglist);
    }
*/    
    private void addToMapStringStringA(Map<String,String[]> toMutate, String key, String value) {
        String[] oldVal = toMutate.get(key);
        String[] newVal = null;
        if (oldVal != null) {
            newVal = new String[oldVal.length +1];
            for (int ii = 0; ii < oldVal.length; ii++) {
                newVal[ii] = oldVal[ii];
            }
            newVal[oldVal.length] = value;
        } else {
            newVal = new String[1];
            newVal[0] = value;
        }
        toMutate.put(key, newVal);            
    }
}
