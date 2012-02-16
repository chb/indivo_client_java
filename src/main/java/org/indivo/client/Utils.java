package org.indivo.client;

// testing git origin
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.HttpResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.params.HttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.StatusLine;
import org.apache.http.HttpEntity;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.HttpRequestAdapter;

import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthExpectationFailedException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class Utils {

    private Log logger = null;
    private String defaultConsumerKey = null;
    private String defaultConsumerSecret = null;
    private String defaultBaseURL = null;

    private DocumentBuilderFactory documentBuilderFactory = null;
    private DocumentBuilder documentBuilder = null;

    private int defaultHttpTimeout = 7000;
    private ResponseTypeConversion defaultResponseTypeConversion = null;

    public Utils(String consumerKey, String consumerSecret, String baseURL,
            ResponseTypeConversion responseTypeConversion, Integer httpTimeout)
            throws IndivoClientException {
        logger = LogFactory.getLog(this.getClass());
        
        defaultConsumerKey = consumerKey;
        defaultConsumerSecret = consumerSecret;
        defaultBaseURL = baseURL;
        defaultResponseTypeConversion = responseTypeConversion;
        if (httpTimeout != null) {
            defaultHttpTimeout = httpTimeout;
        }

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new IndivoClientException(pce);
        }

    }

    String dataFromStream(InputStream inputStrm) throws IndivoClientException {
        String xstr = null;
        try {
            int xcc = inputStrm.read();
            StringBuffer xstrb = new StringBuffer();
            StringBuffer unexpectedBuffer = new StringBuffer();
            while (xcc != -1) {
                xstrb.append((char) xcc);
                xcc = inputStrm.read();
            }
            xstr = xstrb.toString();

            /*
            // indivo bug workaround  FIXME   remove this when data clean, UTF-8 default added to Pha and Admin
            if (xstr.startsWith("<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<")) {
                xstrb.replace(34,36,"8");
                logger.warn("replacing " + xstr.substring(0,50) + " with " + xstrb.substring(0,49));
                xstr = xstrb.toString();
            }*/


                    //logger.info("might have unexpected char: " + unexpectedBuffer);


        } catch (java.io.IOException ioe) {
            throw new IndivoClientException(ioe);
        }

        return xstr;
    }

    void signWithSignpost(
            HttpUriRequest hcRequest,
            String consumerKey0,
            String consumerSecret0,
            String token,
            String tokenSecret) throws IndivoClientException {

        logger.info("in signWithSignpost");
         String consumerKey = null;
         String consumerSecret = null;
         if (consumerKey0 == null) {
             consumerKey = defaultConsumerKey;
             consumerSecret = defaultConsumerSecret;
         } else {
             consumerKey = consumerKey0;
             consumerSecret = consumerSecret0;
         }

        logger.info("in signWithSignpost pre new DefaultOAuthConsumer " + consumerKey + " " + consumerSecret);
         OAuthConsumer oauthConsumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
        logger.info("in signWithSignpost post new DefaultOAuthConsumer");

         oauth.signpost.http.HttpRequest spRequest = new HttpRequestAdapter(hcRequest);
        logger.info("in signWithSignpost post new HttpRequestAdapter");
         if (token == null) {
             oauthConsumer.setTokenWithSecret(null, "");
         } else {
             oauthConsumer.setTokenWithSecret(token, tokenSecret);
         }
         try {
             String whatDebugWas = null;
/* un-comment to get SBS    */    //whatDebugWas = System.setProperty("debug", "true");
        logger.info("in signWithSignpost pre sign");
             oauthConsumer.sign(spRequest);
        logger.info("in signWithSignpost post sign");
/* un-comment this also when un-commenting the above    */  //if (whatDebugWas == null) { System.clearProperty("debug"); } else { System.setProperty("debug", whatDebugWas); }
         } catch (OAuthMessageSignerException omse) {
             throw new IndivoClientException(omse);
         } catch (OAuthExpectationFailedException oefe) {
             throw new IndivoClientException(oefe);
         } catch (oauth.signpost.exception.OAuthCommunicationException oce) {
             throw new IndivoClientException(oce);
         }
    }


        /**
    * General wrapper for all Indivo PHA REST calls, where
    * URL params are to be added.  Most applications will not
    * use this, the more specific methods are recommended.
    * This more general method might be useful, for example,
    * where requests of various types are generated dynamically.
    *
    * @param reqMeth POST, GET, or .....
    * @param reletivePath as documented at <code>http://wiki.chip.org/indivo</code>
    * @param queryString part of URL following '?', but not including '?'
    *     See <b>queryString param</b> note in the class description, above.
    * @param phaToken authorized request token
    * @param phaTokenSecret authorized request token secret
    * @param requestBody value to PUT or POST, not necessarily a Document
    * @param requestContentType content type may be specified, otherwise phaRequest will guess.
    * @param options possible options include: indivoInstallation; sockentTimeout; connectionTimeout
    */
    public Object indivoRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object requestBody,   // String or byte[]
            String requestContentType,
            Object responseContentType,
            Map<String,Object> options) throws IndivoClientException {

        if (! (responseContentType == null
                || responseContentType instanceof String || responseContentType instanceof String[])) {
            throw new IndivoClientException("responseContentType must be a String or String[], was: "
                    + responseContentType.getClass().getName() + "  --  " + responseContentType);
        }
        
        if (options == null) { options = new HashMap<String,Object>(); }

        HttpResponse response = phaRequestPart1(
            reqMeth, reletivePath, queryString, phaToken, phaTokenSecret, requestBody, requestContentType, options);


        StatusLine statusLine = response.getStatusLine();
        HttpEntity httpEntity = response.getEntity();

        int statusCode = statusLine.getStatusCode();
        if (statusCode == 404) {
            String errText = dataFromStream(getContent(httpEntity));
            throw new IndivoClientExceptionHttp404("response code from indivo 404: " + errText);
        } else if (statusCode != 200) {
            String errText = dataFromStream(getContent(httpEntity));
            throw new IndivoClientException(
                    "response code from indivo not 200, was: " + statusCode + "\n" + errText);
        }

        String contentTypeReceived = null;

        Header[] ctHeaders = response.getHeaders("Content-Type");
        if (ctHeaders.length == 0) {
            logger.warn("no Content-Type header found");
        } else {
            if (ctHeaders.length > 1) {
                String multHeaders = "";
                for (int ii = 0; ii < ctHeaders.length; ii++) {
                    multHeaders += "\n" + ctHeaders[ii].getValue();
                }
                logger.warn("more than one Content-Type header in response to "
                    + reqMeth + ".  Using first of:" + reletivePath + multHeaders );
            }

            contentTypeReceived = ctHeaders[0].getValue();
            logger.info("contentTypeReceived: " + contentTypeReceived);
        }

        HttpEntity entityToConvert = response.getEntity();
        if (responseContentType != null) {
            if (responseContentType instanceof String) {
                //"text/plain" "application/x-www-form-urlencoded"
                // application/x-www-form-urlencoded, received: text/plain
                if (responseContentType.equals("application/x-www-form-urlencoded")
                        && contentTypeReceived.startsWith("text/plain") ) {
                    // bug in IndivoX Alpha 2, returns text/plain when it means ...urlencoded
                    entityToConvert = new IndivoHttpEntity(entityToConvert);
                    ((IndivoHttpEntity) entityToConvert).setContentType(
                            "Content-Type", "application/x-www-form-urlencoded");
                } else if (! contentTypeReceived.startsWith((String) responseContentType))  {
                    throw new IndivoClientException("expected: " + responseContentType
                            + ", received: " + contentTypeReceived
                            + "\n" + dataFromStream(getContent(httpEntity)));
                }
            }
            else if (responseContentType instanceof String[] && ((String[])responseContentType).length > 0) {
                String[] responseContentTypeArr = (String[]) responseContentType;
                StringBuffer responseContentTypeMsg = new StringBuffer();
                int ii = 0;
                for (; ii < responseContentTypeArr.length; ii++) {
                    if (contentTypeReceived.startsWith(responseContentTypeArr[ii]))  { break; }
                    if (ii > 0) { responseContentTypeMsg.append(" OR "); }
                    responseContentTypeMsg.append(responseContentTypeArr[ii]);
                }
                if (ii == responseContentTypeArr.length) { // did not break
                   throw new IndivoClientException("expected: " + responseContentTypeMsg
                            + ", received: " + contentTypeReceived
                            + "\n" + dataFromStream(getContent(httpEntity)));
                }
            }
        }

        ResponseTypeConversion responseTypeConversion0 = (ResponseTypeConversion) options.get("responseTypeConversion");
        if (responseTypeConversion0 == null) { responseTypeConversion0 = defaultResponseTypeConversion; }
        return responseTypeConversion0.responseToObject(/*response.getEntity()*/ entityToConvert);
    }

    private InputStream getContent(HttpEntity httpEntity) throws IndivoClientException {
        InputStream istrm = null;
        try {
            istrm = httpEntity.getContent();
        } catch (IOException ioe) {
            throw new IndivoClientException(ioe);
        }
        return  istrm;
    }

    private HttpResponse phaRequestPart1(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object requestBody,   // String or byte[]
            String contentType,
            Map<String,Object> options) throws IndivoClientException {

        String consumerToken = null;
        String consumerSecret = null;
        String foreignURL = null;
        Object indivoInstallation = options.get("indivoInstallation");
        if (indivoInstallation != null) {
            if (! (indivoInstallation instanceof String[])) {
                throw new IndivoClientException(
                        "indivoInstallation option must be of type String[] with lenght == 3.  Was: "
                        + indivoInstallation.getClass().getName());
            }
            String[] indivoInstallation0 = (String[]) indivoInstallation;
            if (indivoInstallation0.length != 3) {
                throw new IndivoClientException(
                        "indivoInstallation option must be a String array with length 3. Length is: "
                        + indivoInstallation0.length);
            }
            foreignURL = indivoInstallation0[0];
            consumerToken = indivoInstallation0[1];
            consumerSecret = indivoInstallation0[2];
        }

        logger.info("consumerToken, consumerSecret, foreignURL: " + consumerToken + ", " + consumerSecret + ", " + foreignURL);
        String displayQS = "null";
        if (queryString != null) {
            displayQS = queryString.getClass().getName() + " " + queryString;
        };

        logger.info("reletivePath, queryString, requestXmlOrParams: "
                + reletivePath + ",  " + displayQS + '\n' + requestBody + "\n\n");
        String queryString0;
        if (queryString == null
                || ((queryString instanceof String) && ((String) queryString).length() == 0)
           ) {
            queryString0 = "";
        } else if (queryString instanceof String) {
            String qsString = (String) queryString;
            if (qsString.indexOf('=') < 1) {
                throw new IndivoClientException(
        	        "unexpected queryString, did not have any key/value delimiter of '=': " + queryString);
            }
        	queryString0 = qsString;
            logger.info("queryString0 = qsString = " + qsString);
        } else if (queryString instanceof Map) {
        	StringBuffer qsBuff = new StringBuffer();
            Map qsMap = (Map) queryString;
        	Iterator iter = qsMap.keySet().iterator();
        	while (iter.hasNext()) {
                if (qsBuff.length() > 0) { qsBuff.append('&'); }

                Object keyObj = iter.next();
                if (! (keyObj instanceof String)) {
                	throw new IndivoClientException("queryString map key of unexpected type: "
                		+ keyObj.getClass().getName() + " -- " + keyObj);
                }
                String key = (String) keyObj;

                Object valueObj = qsMap.get(key);
                try {
                    if (valueObj instanceof String) {
                        qsBuff.append(URLEncoder.encode(key,"UTF-8") + '=' + URLEncoder.encode((String) valueObj,"UTF-8"));
                    } else if (valueObj instanceof String[]) {
                    	String[] valueArr = (String[]) valueObj;
                    	for (int ii = 0; ii < valueArr.length; ii++) {
                           qsBuff.append(URLEncoder.encode(key,"UTF-8") + '=' + URLEncoder.encode(valueArr[ii],"UTF-8"));
                        }
                    } else {
                   	    throw new IndivoClientException("queryString map value of unexpected type: "
                   		    + valueObj.getClass().getName() + " -- " + valueObj);
                    }
                } catch (java.io.UnsupportedEncodingException uee) {
                    throw new IndivoClientException(uee);
                }
            }
            queryString0 = qsBuff.toString();
        } else {
            throw new IndivoClientException(
                "queryString not String or Map, type is: " + queryString.getClass().getName());
        }

        String baseURL0 = defaultBaseURL;
        if (foreignURL != null) {
            baseURL0 = foreignURL;
        }

        String consumerKey0 = defaultConsumerKey;
        String consumerSecret0 = defaultConsumerSecret;
        if (consumerToken != null) {
            consumerKey0 = consumerToken;
            consumerSecret0 = consumerSecret;
        }



        logger.info(" -- baseURL0: " + baseURL0 + " -- reletivePath: " + reletivePath + " -- queryString: " + queryString0);

        String phaURLString = baseURL0 + reletivePath;
        if (queryString0.length() > 0) { phaURLString += "?" + queryString0; }

/* FIXME temp for test*/ //System.out.println(phaURLString); if (requestBody != null) { System.out.println(requestBody); }


        if (requestBody != null) {
            if (requestBody instanceof String) {
                if (((String)requestBody).length() > 0) {
                    if (contentType == null || contentType.length() == 0) {
                        throw new IndivoClientException("contentType must be provided for request body");
                    }
                }
            }
            else if ( (requestBody instanceof Byte[] && ((Byte[]) requestBody).length > 0)
                     || (requestBody instanceof byte[] && ((byte[]) requestBody).length > 0) ) {
                if (contentType == null || contentType.length() == 0) {
                    throw new IndivoClientException("contentType must be provided for request body");
                }
            }
            else {
                throw new IndivoClientException("requestBody must be either String or Byte[] or byte[], was: " + requestBody.getClass().getName());
            }
        } else if (contentType != null && contentType.length() > 0) {
            throw new IndivoClientException("content type provided without requestBody: " + contentType);
        }

        HttpUriRequest hcRequest = null;

        //String requestXmlOrParams0 = null;
        if (requestBody == null) { requestBody = ""; }

        logger.info("reqMeth: " + reqMeth);
        try {
            if (reqMeth.equals("PUT") || reqMeth.equals("POST")) {
                if (reqMeth.equals("PUT")) {
                    hcRequest = new HttpPut(phaURLString);
                } else {
                    hcRequest = new HttpPost(phaURLString);
                }

                byte[] requestBodyB = null;
                if (requestBody instanceof String) {
                    String requestBodyStr = (String) requestBody;
                    if (requestBodyStr.startsWith("<?xml")) {
                        String[] parsedProlog = getEncoding(requestBodyStr);
                        if (parsedProlog.length == 3 && (! parsedProlog[1].toUpperCase().equals("UTF-8"))) {
                            requestBodyStr = parsedProlog[0] + "UTF-8" + parsedProlog[1];
                            logger.info("changing prolog from: " + parsedProlog[1] + " to: " + "UTF-8");
                            requestBodyStr = parsedProlog[0] + "UTF-8" + parsedProlog[2];
                        }
                    }

                    //System.out.println("requestBodyStr: " + requestBodyStr);
                    requestBodyB = requestBodyStr.getBytes("UTF-8");
                } else if (requestBody instanceof byte[]) {
                    requestBodyB = (byte[]) requestBody;
                } else {  // requestBody instanceof Byte[]
                    requestBodyB = new byte[((Byte[]) requestBody).length];
                    for (int ii = 0; ii < ((Byte[]) requestBody).length; ii++) {
                        requestBodyB[ii] = ((Byte[]) requestBody)[ii];
                    }
                }
                ByteArrayEntity bae = new ByteArrayEntity(requestBodyB);
                bae.setContentType(contentType);
                ((HttpEntityEnclosingRequestBase)hcRequest).setEntity(bae);
//                hcRequest.addHeader("Content-Type",contentType);
            } else if (reqMeth.equals("GET")) {
                hcRequest = new HttpGet(phaURLString);
            } else if (reqMeth.equals("DELETE")) {
                hcRequest = new HttpDelete(phaURLString);
            }
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new IndivoClientException(uee);
        }

        // in case of form-url-encoded, will signpost know to look at Content-Type header and entity??
    
    logger.info("pre signWithSignpost");
        signWithSignpost(hcRequest, consumerKey0, consumerSecret0, phaToken, phaTokenSecret);
    logger.info("post signWithSignpost");

        hcRequest.addHeader("Accept", "text/plain,application/xml");      // don't be mistaken for a browser
    logger.info("post signWithSignpost 1");

        AbstractHttpClient httpClient = new DefaultHttpClient();
        HttpParams httpParams0 = httpClient.getParams();
    logger.info("post signWithSignpost 2");

        Object connectionTimeout = options.get("connectionTimeout");
        Object socketTimeout = options.get("socketTimeout");
    logger.info("post signWithSignpost 3");
        if (connectionTimeout == null) {
            connectionTimeout = defaultHttpTimeout;
        }
        if (socketTimeout == null) {
            socketTimeout = defaultHttpTimeout;
        }
    logger.info("post signWithSignpost 4");


        if (! ((socketTimeout instanceof Integer) && (connectionTimeout instanceof Integer)) ) {
            throw new IndivoClientException("socketTimeout and connectionTimeout options must be ingeters. "
                    + "sockenTimeout was " + socketTimeout.getClass().getName()
                    + ", and connectionTimeout was " + connectionTimeout.getClass().getName());
        }
    logger.info("about to set CONNECTION_TIMEOUT");

        httpParams0 = httpParams0.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT , (Integer) connectionTimeout);
        httpParams0 = httpParams0.setIntParameter(CoreConnectionPNames.SO_TIMEOUT ,  (Integer) socketTimeout);
        httpClient.setParams(httpParams0);

        HttpResponse httpResponse = null;
//        StatusLine statusLine = null;
//        InputStream istrm = null;
            org.apache.http.Header[] allheaders = hcRequest.getAllHeaders();
            StringBuffer allheadersSB = new StringBuffer("\nall request headers:");
            for (int ii = 0; ii < allheaders.length; ii++) {
                allheadersSB.append("\n" + allheaders[ii].getName() + " : " + allheaders[ii].getValue());
            }
            logger.info("request: " + hcRequest.getMethod() + " " + hcRequest.getURI() + allheadersSB);
        try {
            httpResponse = httpClient.execute(hcRequest);
        } catch (java.net.ConnectException conE) {
            conE.printStackTrace();
            logger.warn("connectionTimeout, socketTimeout: " + connectionTimeout + ", " + socketTimeout);
            throw new IndivoClientConnectException(
                    "connectionTimeout, socketTimeout: " + connectionTimeout + ", " + socketTimeout, conE);
        } catch (Exception excp) {
            excp.printStackTrace();
            logger.warn("phaRequestPart1 exception");
            throw new IndivoClientException("connectionTimeout, socketTimeout: " + connectionTimeout + ", " + socketTimeout, excp);
        }

        return httpResponse;
    }


//         '<?xml' VersionInfo  EncodingDecl? SDDecl? S? '?>'
//         VersionInfo ::= S 'version' Eq ("'" VersionNum "'" | '"' VersionNum '"')
//         S 'encoding' Eq ('"' EncName '"' | "'" EncName "'" )
//         Eq ::= S? '=' S?
//         S ::= (#x20 | #x9 | #xD | #xA)+
    public static String[] getEncoding(String xmlMayHaveProlog) throws IndivoClientException {
        String[] retVal = null;
        retVal = new String[1];
        retVal[0] = xmlMayHaveProlog;  // if no prolog or no encoding specified

        if (xmlMayHaveProlog.startsWith("<?xml")) {
            int xix = xmlMayHaveProlog.indexOf("version");
            if (xix == -1) { throw new IndivoClientException("invalid prolog of XML: " + xmlMayHaveProlog); }
            xix = xmlMayHaveProlog.indexOf('=', xix);
            if (xix == -1) { throw new IndivoClientException("invalid prolog of XML: " + xmlMayHaveProlog); }

            int sqix = xmlMayHaveProlog.indexOf('\'', xix);
            int dqix = xmlMayHaveProlog.indexOf('"', xix);
            if (sqix > -1 && (dqix == -1 || sqix < dqix)) { xix = sqix; }
            else if (dqix > -1 && (sqix == -1 || dqix < sqix)) { xix = dqix; }
            else { throw new IndivoClientException("invalid prolog of XML. xix, xml"  + xix + ", " + xmlMayHaveProlog); }
            char quote = xmlMayHaveProlog.charAt(xix);
            xix = xmlMayHaveProlog.indexOf(quote, xix +1);
            if (xix == -1) { throw new IndivoClientException("invalid prolog of XML: " + xmlMayHaveProlog); }
            xix = xmlMayHaveProlog.indexOf("encoding", xix +1);
            if (xix != -1) {
                xix = xmlMayHaveProlog.indexOf('=',xix);
                if (xix == -1) { throw new IndivoClientException("invalid prolog of XML: " + xmlMayHaveProlog); }

                sqix = xmlMayHaveProlog.indexOf('\'', xix);
                dqix = xmlMayHaveProlog.indexOf('"', xix);
                if (sqix > -1 && (dqix == -1 || sqix < dqix)) { xix = sqix; }
                else if (dqix > -1 && (sqix == -1 || dqix < sqix)) { xix = dqix; }
                else { throw new IndivoClientException("invalid prolog of XML: " + xmlMayHaveProlog); }
                quote = xmlMayHaveProlog.charAt(xix);
                int endQix = xmlMayHaveProlog.indexOf(quote, xix +1);
                if (xix == -1) { throw new IndivoClientException("invalid prolog of XML: " + xmlMayHaveProlog); }

                retVal = new String[3];
                retVal[0] = xmlMayHaveProlog.substring(0, xix +1);
                retVal[1] = xmlMayHaveProlog.substring(xix +1, endQix).toUpperCase();
                retVal[2] = xmlMayHaveProlog.substring(endQix);
            }
        }

        return retVal;
    }

    public Document docFromString(String msg) throws IndivoClientException {
        //System.out.println("in docFromString");
        Document docForContent = null;
        /*
        String encoding = "UTF-8";
        if (msg.startsWith("<?xml")) {  //<?xml version=\"1.0\" encoding=\"UTF-8\"?>
            int eix = msg.indexOf("?>");
            int encodeIx = msg.indexOf("encoding=");
            if (encodeIx != -1 && encodeIx < eix) {
                encodeIx += "encoding=".length();
                char quote = msg.charAt(encodeIx);
                if (quote != '\'' && quote != '"') {
                    throw new IndivoClientException("unexpected xml prolog: " + msg);
                }
                StringBuffer encodingSB = new StringBuffer();
                encodeIx++;
                while(encodeIx < eix && msg.charAt(encodeIx) != quote) {
                    encodingSB.append(msg.charAt(encodeIx));
                    encodeIx++;
                }
                encoding = encodingSB.toString();
                System.out.println("encoding from xml string: " + encoding + "\n" + msg + "\n");
            }
        }*/

        try { // get the content
            synchronized(documentBuilder) {
                    docForContent = documentBuilder.parse(new ByteArrayInputStream(msg.getBytes()));
            }
        } catch (org.xml.sax.SAXException sxe) {
            logger.error("\n" +  msg + "\nnot xml parsable stream");
            throw new IndivoClientException(sxe);
        } catch (IOException ioe) {
            throw new IndivoClientException(ioe);
        }
        Element docElem = docForContent.getDocumentElement();
        logger.info("docElem tag name b: " + docElem.getTagName() + "\n" + msg);

        return docForContent;
    }

    public static String domToString(Node theNode) {
        if (theNode instanceof org.w3c.dom.Document) {
            return domToString((Document) theNode, theNode);
        } else {
            return domToString(theNode.getOwnerDocument(), theNode);
        }
    }
    public static synchronized String domToString(Document theDoc, Node theNode) {
        DOMImplementation domI = theDoc.getImplementation();
        DOMImplementationLS domIls = (DOMImplementationLS) domI.getFeature("LS", "3.0");
        LSSerializer lss = domIls.createLSSerializer();
        String xmlstr = lss.writeToString(theNode);
        //<?xml version="1.0" encoding="UTF-16"?>
        return xmlstr;
    }

    public static String printForm(Map<String,String> aform) {
        StringBuffer retVal = new StringBuffer();
        Iterator<String> keysI = aform.keySet().iterator();
        while (keysI.hasNext()) {
            String aKey = keysI.next();
            retVal.append(aKey + '=' + aform.get(aKey) + "   ");
        }
        return retVal.toString();
    }



    public static String toHex(String instr) throws IndivoClientException {
       return toHex(instr, "UTF-8");
    }
    public static String toHex(String instr, String charset) throws IndivoClientException {
        StringBuffer ostr = new StringBuffer();
        byte[] ibytes = null;
        try {
            ibytes = instr.getBytes(charset);
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new IndivoClientException(uee);
        }

        for (int ii = 0; ii < ibytes.length; ii++) {
            byte abyte = ibytes[ii];
            int hig = abyte & 0xf0; hig = hig >>> 4;
            int low = abyte & 0x0f;
            ostr.append((char) abyte);
            toHex0(ostr, hig);
            toHex0(ostr, low);
            ostr.append(' ');
        }
        return ostr.toString();
    }
    private static void toHex0(StringBuffer ostr, int higlow) {
        if (higlow < 10) { ostr.append((char) ('0' + higlow));
        } else { ostr.append((char) ('a' + (higlow - 10))); }
    }

}

/** add setContentType method, part of alpha 2 release bug fix */
class IndivoHttpEntity implements HttpEntity {
    private HttpEntity httpEntity = null;
    private Header contentType = null;

    IndivoHttpEntity(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }

    void setContentType(String key, String value) {
        contentType = new org.apache.http.message.BasicHeader(key, value);
    }

    @Override
    public void consumeContent() throws IOException {
        httpEntity.consumeContent();
    }

    @Override
    public boolean isStreaming() { return httpEntity.isStreaming(); }

    @Override
    public boolean isChunked() { return httpEntity.isChunked(); }

    @Override
    public boolean isRepeatable() { return httpEntity.isRepeatable(); }

    @Override
    public void writeTo(java.io.OutputStream os) throws IOException {
        httpEntity.writeTo(os);
    }

    @Override
    public InputStream getContent() throws IOException {
        return httpEntity.getContent();
    }

    @Override
    public Header getContentEncoding() { return httpEntity.getContentEncoding(); }

    @Override
    public long getContentLength() { return httpEntity.getContentLength(); }

    @Override
    public Header getContentType() {
        if (contentType == null) {
            return httpEntity.getContentEncoding();
        } else {
            return contentType;
        }
    }



}