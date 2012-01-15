package org.indivo.oauth;   

import java.io.FileInputStream;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Iterator;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.MessageDigest;

import javax.crypto.Mac;    

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.commons.codec.binary.Base64;
import org.indivo.oauth.AuthUtils;
           
/**
* For OAuth request signing.
*/
public class SignRequest {

    private static final long serialVersionUID = 1L;
    private Log logger = null;
	private Random nonceRandom = null;

    private Mac defaultMac = null;
    private String defaultSecret = null;

    private Base64 base64 = new Base64();

    private MessageDigest messageDigestSha1 = null;
    
    private StringBuilder authhead = null;
    private List<String[]> siglist = null;
    private boolean prePercentEncoded = false;

    
    /** construct an instance without a default consumer secret.
    * convenience constructor for SignRequest(String encodedSecret)
    */
    public SignRequest() throws java.security.GeneralSecurityException {
        this(null);
    }

    /**
    * construct an instance with a default consumer secret.
    * @param encodedSecret signRequest(....) will use this encodedSecret
    * to generate the Mac key only, if the signRequest macSecret param is null.
    * encodedSecret should be %encoded(consumerSecret) + '&'.
    */
    public SignRequest(String encodedSecret) throws java.security.GeneralSecurityException {
        logger = LogFactory.getLog(this.getClass());
        if (encodedSecret != null) {
            defaultMac = AuthUtils.makeMac(encodedSecret);
            this.defaultSecret = encodedSecret;
        }

        try {
            messageDigestSha1 = MessageDigest.getInstance("SHA-1");
        } catch (java.security.NoSuchAlgorithmException nsae) {
            throw new GeneralSecurityException(nsae);
        }

        // use SecureRandom (more truly random) to generate a seed
        // SecureRandom probably Uses System hardware to seed the randoms
        // for nonce, use java.util.Random (Random has method to get an int
        // in an arbitrary range)
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] seed = secureRandom.generateSeed(8);
        // now turn the 8 bytes seed into one long primitive seed
        long randomLong = 0L;
        for (int ii = 0; ii < 8; ii++) {
            // new rightmost 8 bits OR-ed in after left shift 8 bits
            randomLong = (randomLong << 8) | (/*drop propogated sign bits*/ 255L & seed[ii]);
        }
        nonceRandom = new Random(randomLong);
    }

    /** return 16 characters, each of the 16 chosen randomly from a set of 62
    * displayable candidate characters (alphas upper and lower, and digits).
    */
    public String getNonce() {
        StringBuilder sbn = new StringBuilder();
        // make 16 random base64 digits
        for (int ii = 0; ii < 16; ii++) {
            int ndig = nonceRandom.nextInt(62);
            if (ndig < 26) {
                sbn.append((char) ('A' + ndig));
            } else if (ndig < 52) {
                sbn.append((char) ('a' + (ndig - 26)));
            } else if (ndig < 62) {
                sbn.append((char) ('0' + (ndig - 52)));
            }
        }
        return sbn.toString();
    }


    /**
    * place oauth params in "Authenticate:" header, not elsewhere.
    */
    private String removeOAuthParam(boolean required, String key, Map<String,String[]> inmap)
            throws GeneralSecurityException {
        String retVal = null;
        String[] value0 = inmap.remove(key);
        if (value0 == null || value0[0] == null) {
            if (required) {
                throw new GeneralSecurityException("missing key in params: " + key);
            }
        } else {
            retVal = value0[0];
        }
        
        return retVal;
    }

    
    /** place oauth params in both "Authentication:" header,
    * and in the signature base string.
    */
    private void headAndSiglist(String key, String val0) throws GeneralSecurityException {
        String val = val0;
        if (! prePercentEncoded) {
            val = AuthUtils.encode(val0);
            if (key.equals("oauth_body_hash")) {
                logger.info(key + ": " + val0 + "  --  " + val);
            }
        }
        
        if (authhead.length() > 0) {
            authhead.append(',');
        }
        authhead.append(key + "=\"" + val + "\"");
        
        String[] sigent = new String[2];
        sigent[0] = key; sigent[1] = val;
        siglist.add(sigent);
    }
    
    /** convenience method for signRequest(false, requestUrl, reqParams,
    *        consumerSecret, tokenSecret, rMeth, null).
    * @deprecated use signpost, see Nate for an example.
    */
    @Deprecated
    public String signRequest(
            String requestUrl,
            Map<String,String[]> reqParams,
            String consumerSecret,
            String tokenSecret,
            String rMeth) throws GeneralSecurityException {
        return signRequest(
                false,
                requestUrl,
                reqParams,
                consumerSecret,
                tokenSecret,
                rMeth,
                null);
    }

    /** convenience method for signRequest(false, requestUrl, reqParams,
    *        consumerSecret, tokenSecret, rMeth, reqBody).
    * @deprecated use signpost, see Nate for an example.
    */
    @Deprecated
    public String signRequest(
            String requestUrl,
            Map<String,String[]> reqParams,
            String consumerSecret,
            String tokenSecret,
            String rMeth,
            String reqBody) throws GeneralSecurityException {
        return signRequest(
                false,
                requestUrl,
                reqParams,
                consumerSecret,
                tokenSecret,
                rMeth,
                reqBody);
    }
    
    /**
    * For oauth, to ensure client is who he says he is,
    * return Authorization: header value (value excluding "Authorization: ").
    *
    <p>
    * <strong>Example:</strong><br/>
    * prePercentEncoded = <code>false</code>;<br/>
    * requestUrl = <code>'http://localhost/records/'</code>;<br/>
    * params = <code>{oauth_consumer_key:['consumerKey'], app_specific_param:[appSpecificValue]}</code>;<br/>
    * consumerSecret = <code>'noOneElseKnows&'</code>;<br/>
    * tokenSecret = <code>null</code>;<br/>
    * rMeth = <code>'POST'</code><br/>
    * reqBody = <code>null</code><br/>
    * The returned value will be: <code>OAuth realm="http://localhost/",oauth_consumer_key="consumerKey",oauth_signature_method="HMAC-SHA1",oauth_timestamp="1237837206",oauth_nonce="1Iy
OBeH9LaFtLD9y",oauth_version="1.0",oauth_signature="Y7QWvz7T6FdCe6eggq9HoHbno3c="
    * </p><p><strong>sample code:</strong><br/>
    * HttpURLConnection cnctn = &lt;however-you-create-HttpURLConnection-instance&gt;<br/>
    * <code>String sruReturn = signRequest(false, ...);<br/>
    * cnctn.setRequestProperty("Authorization", sruReturn);<br/>
    * Iterator<String> keys = signParams.keySet().iterator();<br/>
    * // remaining Map elements, those not used in Authorization header<br/>
    * while (keys.hasNext()) {<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;String akey = keys.next();<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;String[] vals = signParams.get(akey);<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;for (int ii = 0; ii < vals.length; ii++) {<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// <strong>this only if you are adding params to the URL</strong><br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cnctn.addRequestProperty(akey,vals[ii]);<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// <strong>this only if you are using application/x-www-form-urlencoded</strong><br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// later send body using cnctn's OutputStream<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (body.length > 0) { body += '&' }<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;body += AuthUtils.encode(akey) + '='
    * + AuthUtils.encode(vals[ii]);<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
    * }<br/>
    * </code>                                 
    * </p><p>
    * @param prePercentEncoded true if the param values in the reqParams param map are
    *       already encoded, otherwise false.<br/>
    *       &nbsp;&nbsp;&nbsp;<code>false</code> means reqParams Map values not
    *       encoded before calling
    *       signRequest AND reqParams Map values <strong>still NOT</strong>
    *       encoded when signRequest returns.  signRequest copies the values before
    *       incorporating them into the Authorization header and signature, and
    *       <strong>encodes the copies, but does
    *       NOT encode the original values</strong> that may remain
    *       in the reqParams map.  Caller may still need to encode before adding the remaining
    *       (those not swallowed up by the Authorization header) params to the request.
    *       </code>AuthUtils.encode(...)</code> may be used.<br/>
    *       &nbsp;&nbsp;&nbsp;<code>true</code> means the
    *       <strong>caller is responsible to encode ALL
    *       the parameter values within the reqParms Map BEFORE</strong>
    *       calling signRequest.
    * </p><p>
    * @param requestUrl URL of service.
    * </p><p>
    * @param reqParams request parameters<br/>&nbsp;&nbsp;&nbsp;for each key/value pair in the Map,
    *     key is parameter key and value is an array of, usually one but possibly
    *     zero or many, Strings
    *     <br/>&nbsp;&nbsp;&nbsp;<code>oauth_consumer_key'=[theKey]</code> must be present
    *     in params;  <code>oauth_signature</code> must be omitted as it is the job of this method
    *     to calculate the signature; Other oauth params are optional,
    *     default values will be supplied for absent required OAuth params;
    *     <br/>&nbsp;&nbsp;&nbsp;for 3-legged oauth, oauth_token must be supplied as signRequest()
    *     has no way to calculate a default.
    * <br/><plain>
    * defaults:</plain>
    * <ul>
    *    <li>oauth_signature_method="HMAC-SHA1"</li>
    *    <li>oauth_version="1.0"</li>
    *    <li>oauth_timestamp=&lt;value-derived-from-System-time&gt;</li>
    *    <li>oauth_nonce=&lt;string-of-16-base64-digits-randomly-selected&gt;</li>
    *    <li>realm=&lt;value-derived-from-local-host-name&gt;</li>
    *    <li>oauth_body_hash=&lt;value-derived-from-reqBody-param&gt; oauth_body_hash
    *       not added if reqBody==null</li>
    * </ul>
    *     <br/>&nbsp;&nbsp;&nbsp;Also include, in reqParams, application specific
    *     non-oauth params
    *     as signRequest() must use them in the signing process (so signature can prevent
    *     evil entity from making an undetected change to any param);
    *     <br/>&nbsp;&nbsp;&nbsp;signRequest() will
    *     remove from the map (mutate the map) all params it integrates
    *     into the returned Authorization value.  Caller is responsible for adding any
    *     params, still in the map when singRequest returns, to the request.
    * </p><p>
    * @param consumerSecret value pre-agreed by consumer and server, and known only to
    *     consumer and server, to prove consumer's identity.  This is used to make the
    *     Hmac key.  Caller should not % encode consumer secret, signRequest will do that.
    *     Caller should not append '&', signRequest will take care of appending '&'.
    *     This may be null if SignRequest was constructed with a macSecret.
    * @param tokenSecret value provided along with <code>oauth_token</code>.
    *     This is used along with consumerSecret to produce the
    *     Hmac key.    Caller should not % encode tokenSecret, signRequest will do that.
    *     This must be null or the empty String when reqParams does not include 
    *     <code>oauth_token</code>, or if consumerSecret is null.
    *     This must not be null and must not be empty when reqParams does include 
    *     <code>oauth_token</code> and consumerSecret is present.
    * </p><p>
    * @param rMeth usually 'POST' but can be 'GET' etc.
    * </p><p>
    * @param reqBody If there is to be an "oauth_body_hash", and the oauth_body_hash is not
    *       already in the reqParams map, this should be the request body to be hashed.
    *       signRequest will calculate the hash and include oauth_body_hash in the returned
    *       authorization header.  Use null if oauth_body_hash is already present or if
    *       it is not needed.
    * </p><p>
    * @return Authorization value; user can later do:
    *      <code>httpURLConnection.addRequestProperty("Authorization:", &lt;value-returned-from-signRequest&gt;)</code>.
    * </p><p>
    * @throws java.security.GeneralSecurityException when unable to produce an Authorization header.
    * </p>
    * @deprecated use signpost, see Nate for an example.
    */
    @Deprecated
    public String signRequest(
            boolean prePercentEncoded,
            String requestUrl,
            Map<String,String[]> reqParams,
            String consumerSecret,
            String tokenSecret,
            String rMeth,
            String reqBody) throws GeneralSecurityException {
        this.prePercentEncoded = prePercentEncoded;
        String macSecret = null;
        if (consumerSecret == null && defaultMac == null) {
            throw new GeneralSecurityException(
                "called with null consumer secret and SignRequest constructed without a "
                + "default consumer sercret. Either call signRequest(...) with "
                + "a macSecret string or construct SignRequest with the one-"
                + "arg constructor.");
        }
        if (tokenSecret == null || tokenSecret.length() == 0) {
            if (reqParams.get("oauth_token") != null) {
                throw new GeneralSecurityException("tokenSecret null or empty, but there is"
                        + " an 'oauth_token' in reqParams.");
            }
        } else {
            if (reqParams.get("oauth_token") == null) {
                throw new GeneralSecurityException("tokenSecret present, "
                        + "but there is no 'oauth_token' in reqParams.");
            }
        }

        if (consumerSecret == null) {
            if (tokenSecret != null) {
                macSecret = defaultSecret /*already has & */ + AuthUtils.encode(tokenSecret);
            }
        } else if (tokenSecret == null || tokenSecret.length() == 0) {
            macSecret = AuthUtils.encode(consumerSecret) + '&';
        } else {
            macSecret = AuthUtils.encode(consumerSecret)
                + '&' + AuthUtils.encode(tokenSecret);
        }
        
        authhead = new StringBuilder("OAuth ");
        String oauthRealm = removeOAuthParam(false, "realm", reqParams);
        if (oauthRealm == null) {
            // realm not provided, make one
            try {
                oauthRealm =
                        "http://" + java.net.InetAddress.getLocalHost().getHostName() + "/"; 
            } catch (java.io.IOException ioe) {
                throw new GeneralSecurityException(ioe);
            }
        }
        // realm doesn't go into signature, make it the first one
        authhead.append("realm=\"" + oauthRealm + "\"");

        siglist = new ArrayList<String[]>();

        String oauthConsumerKey = removeOAuthParam(true, "oauth_consumer_key", reqParams);
        headAndSiglist("oauth_consumer_key", oauthConsumerKey);

        String oauthSigMeth = removeOAuthParam(false, "oauth_signature_method", reqParams);
        if (oauthSigMeth == null) {
            oauthSigMeth = "HMAC-SHA1";  // this is the default
        } else if (! oauthSigMeth.equals("HMAC-SHA1")) {
            throw new GeneralSecurityException(
                    "oauth_signature_method != HMAC-SHA1. " + oauthSigMeth
                    + " not currently supported");
        }
        headAndSiglist("oauth_signature_method", oauthSigMeth);
        
        String oauthVers = removeOAuthParam(false, "oauth_version", reqParams);
        if (oauthVers == null) {
            oauthVers = "1.0";  // default
        } else if (! oauthVers.equals("1.0")) {
            throw new GeneralSecurityException(
                    "oauth_version != 1.0  " + oauthVers + " not supported.");
        }
        headAndSiglist("oauth_version", oauthVers);
        
        String oauthTS = removeOAuthParam(false, "oauth_timestamp", reqParams);
        if (oauthTS == null) {
            // timestamp not provided, make one
            oauthTS = new Long(Calendar.getInstance().getTimeInMillis() / 1000).toString();
        }
        headAndSiglist("oauth_timestamp", oauthTS);

        String oauthNonce = removeOAuthParam(false, "oauth_nonce", reqParams);
        if (oauthNonce == null) {
            // nonce not provided, make one
            oauthNonce = getNonce();
        }
        headAndSiglist("oauth_nonce", oauthNonce);

        String oauthToken = removeOAuthParam(false, "oauth_token", reqParams);
        if (oauthToken != null) {
            headAndSiglist("oauth_token", oauthToken);
        }

        String rqBodyHash = removeOAuthParam(false, "oauth_body_hash", reqParams);
        if (reqBody != null) {
            if (rqBodyHash != null) {
                throw new GeneralSecurityException("oauth_body_hash request param provided "
                        + "and reqBody param not null");
            }
            rqBodyHash = makeBodyHash(reqBody);
        }
        if (rqBodyHash != null) {
            headAndSiglist("oauth_body_hash", rqBodyHash);
        }
        
        // add remainders just to siglist
        Iterator<String> rqKeys = reqParams.keySet().iterator();
        while (rqKeys.hasNext()) {
            String akey = rqKeys.next();
            String[] aval = reqParams.get(akey);
            for (int ii = 0; ii < aval.length; ii++) {
                logger.info("encoding param. prePercentEncoded=" + prePercentEncoded
                        + " -- key,value: " + akey + ", " + aval[ii]);
                String[] sigentb = new String[2];
                if (! prePercentEncoded) {
                    sigentb[0] = AuthUtils.encode(akey);
                    sigentb[1] = AuthUtils.encode(aval[ii]);
                } else {
                    sigentb[0] = akey; sigentb[1] = aval[ii];
                }
                siglist.add(sigentb);
            }
        }

        // turn map into ampersand delimited string of key=value pairs
        String sigstring = AuthUtils.saListToAmpString(siglist);
        
        Mac mac = null;
        if (macSecret == null) {
            mac = defaultMac;
            logger.info("no makeMac, using defaultMac: " + defaultMac);
        } else {
            mac = AuthUtils.makeMac(macSecret);
            logger.info("makeMac(" + macSecret + ")");
        }
        
        int rurlColon = requestUrl.indexOf(':', 6);
        int rurlSlash = requestUrl.indexOf('/', 8);
        String requestUrl0 = requestUrl;  // do nothing if not explicit port 80
        if (rurlColon != -1 && rurlColon < rurlSlash
                && requestUrl.substring(rurlColon +1, rurlColon +4).equals("80/") ) {
            // explicit port 80, drop the ":80"
            requestUrl0 = requestUrl.substring(0, rurlColon) + requestUrl.substring(rurlColon + 3);
        } 
        
        String sigBase = rMeth + '&' + AuthUtils.encode(requestUrl0) + '&'
                    + AuthUtils.encode(sigstring);
        logger.info("sigBase: " + sigBase);
        byte[] signature = mac.doFinal(sigBase.getBytes());
        String sigBase64 = new String(base64.encode(signature));

        authhead.append(",oauth_signature=\"" + sigBase64 + "\"");
        return authhead.toString();

    }
    
    private String  makeBodyHash(String bodyIn) {
        messageDigestSha1.reset();  // in case prior user of messageDigestSha1 forgot
        messageDigestSha1.update(bodyIn.getBytes());
        byte[] retVal0 = messageDigestSha1.digest();
        messageDigestSha1.reset();
        byte[] retVal = base64.encode(retVal0);
        logger.info("messageDigest: " + new String(retVal) + " -- " + bodyIn);
        return new String(retVal);
    }

}
