package org.indivo.client;


//import java.io.OutputStream;
//import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
* Convenience methods that wrap the Indivo REST api calls
* that are documented at <code>http://wiki.chip.org/indivo</code>
* plus a few utilites and a few higher-level methods.<br/><br/>
/*_JAVA_STYLE_ONLY*/
/*_SHELL_DROP*/ /**
* This class uses REST style method names.  For an equivalent class using python style method names, see org.indivo.client.Rest_py_client_style
*
* HERE ARE THE RULES FOR TRANSLATING A REST URL TO A JAVA API METHOD NAME IN org.indivo.client.Rest.
* <ul>
* <li>code REST '/' as Java '_', except omit the leading '/'</li>
* <li>where ever there is a variable, use 'X'</li>
* <li>When REST call includes a dash ("-"), remove the dash and upper-case the first character that followed
*   the dash.</li>
* <li>use request method (GET|PUT|POST|DELETE) as last part of method name.</li>
* </ul>
/*_END_JAVA_STYLE_ONLY*/
/*_SHELL_DROP*/ /**
/*_PYTHON_STYLE_ONLY*/
/*_SHELL_DROP*/ /**
* This class uses python style method names.  For an equivalent class using Rest style method names, see org.indivo.client.Rest
/*_END_PYTHON_STYLE_ONLY*/
/*_SHELL_DROP*/ /**
* HERE ARE THE RULES FOR PLACING QUERY PARAMETERS IN METHOD CALL PARAMETERS, org.indivo.client.Rest and org.indivo.client.Rest_py_client_style.
* <ul>
* <li>There is a specific method parameter for each required query parameter.
*  These are the first method parameters.
* <li>If there is any optional query parameter, there will be one "queryOptions" method parameter to contain
*   all optional query parameters.  These should be privided by the application in
*   url-encoded form, for example: <code>offset=200&limit=50</code>
*   the "queryOptions" parameter for optional URL query parameters immediately follows
*   any method parameters for required query parameters, and precedes all others.
* <p>
*     queryOptions can be a String or Map&lt;String,Object&gt;.
*     Where type is Map, each value must be of type String or String[].  Where one key
*     has multiple values, type String[] must be used for that key's value.
*     Where no queryOptions are provided, null, "", or empty Map will all work.
* </p>
* <pre>
*    Example of a String: a=x%3dy&b=z
*    Example of Map&lt;String,Object&gt;
*       Map&lt;String,Object&gt; query = new HashMap&lt;String,Object&gt;()
*       query.put("a","x=y");
*       String[] value = new String[2]; value[0] = "z"; value[1] = "w";
*       query.put("b",value);
* </pre>
* <li>variables in the REST url map to parameters in their respective sequence, following
*   any method parameters for URL query parameters.</li>
* <li>record token/secret are the last two parameters when there is no
*   HTTP content to be sent.  record token/secret are the last two paramters before
*   HTTP content when HTTP content present.  App specific, non record specific methods
*   do not have token/secret params.  Calls that can only be made by an Administrator
*   do not have token/secret params.  Calls that can be made by an Administrator but also
*   by a PHA app have token/secret params and they should be provided nulls in the case
*   of a call by and administrator.</li>
* <li>where content is to be supplied in the call, that is the last parameter before the
*     content type params (if present) and options map.</li>
* <li>each REST call of the form: <code>GET /records/{record_id}/reports/minimal/&lt;type_eg_medications&gt;/</code>
*   or of the form: <code>GET /carenets/{carenet_id}/reports/minimal/&lt;type_eg_medications&gt;/</code>
*   is combined into one of two methods as if the REST calls were documented as:
*   <code>GET /records/{record_id}/reports/minimal/{type_of_minimal_eg_medications}/</code> OR
*   <code>GET /carenets/{carenet_id}/reports/minimal/{type_of_minimal_eg_medications}/</code></li>
* <li>Higher level methods that are named as if they map to a REST call,
*   but that do more than map directly to a single
*   REST call, are named according to the fictional REST URL that would seem to do their function,
*   but the method name is given "_HL" as a suffix.</li>
* </ul>
*/
/*_JAVA_STYLE_ONLY*/
public class Rest_SHELL {
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
public class Rest_py_client_style_SHELL {
/*_END_PYTHON_STYLE_ONLY*/
	
    private static final long serialVersionUID = 1L;
    
    public static List<String> allowedAuditQuery = null;
    {
      allowedAuditQuery = Arrays.asList(
    		  "document_id","external_id","request_date","function_name","principal_email","proxied_by_email");
//    #document_id: The document modified by the request. String
//    #external_id: The external id used to reference a resource in the request. String
//    #request_date: The date on which the request was made. Date
//    #function_name: The internal Indivo X view function called by the request. String
//    #principal_email: The email of the principal making the request. String
//    #proxied_by_email: The email of the principal proxied by the principal making the request (i.e., the email of the 
    }

    private String[] plainOrEncodedTemp = null;
    {
        plainOrEncodedTemp = new String[2];
        plainOrEncodedTemp[0] = "text/plain";
        plainOrEncodedTemp[1] = "application/x-www-form-urlencoded";
    }
    private final String[] plainOrEncoded = plainOrEncodedTemp;
    
    private Log logger = null;

    private DocumentBuilderFactory documentBuilderFactory = null;
    private final DocumentBuilder documentBuilder;
    
    private String instanceConsumerKey = null;
    private String instanceConsumerSecret = null;
    private String indivoBase = null;
    
    private Utils clientUtils = null;

    private ResponseTypeConversion responseTypeConversion = null;

    /** some methods need this internally, regardless of how caller wants to see output */
    private DefaultResponseTypeConversion internalResponseTypeConversion = null;

    int defaultHttpTimeout = 10000;

    private int testMode = 0;
    
    /**
    * Use this constructor when you will use the same installation,
    * as the same consumer, repeatedly.
    * @param oauthConsumerKey key your Indivo administrator assigned to
    *     the calling Personal Health Application.
    * @param oauthConsumerSecret secret your Indivo administrator assigned
    *     to the calling Personal Health Application.  Keep this secret.
    * @param baseURL server URL prior to documented REST API portion, for example
    *   most Indivo REST API locations start with "/records/...",
    *   <code>adminBase</code> is the part of the URL before "/records/...".
    * @param responseTypeConversion null to have default class used.
    * @param defaultHttpTimeout null to use default value (10000 milli-seconds), otherwise
    *   number of milli-seconds.  This becomes the default value that can be overrided
    *   per call in the options param.
    */
/*_JAVA_STYLE_ONLY*/
    public Rest_SHELL(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
    public Rest_py_client_style_SHELL(
/*_END_PYTHON_STYLE_ONLY*/
            String oauthConsumerKey,
            String oauthConsumerSecret,
            String baseURL,
            ResponseTypeConversion responseTypeConversion,
            Integer defaultHttpTimeout) throws IndivoClientException {
        logger = LogFactory.getLog(this.getClass());

        if (defaultHttpTimeout != null) {
            this.defaultHttpTimeout = defaultHttpTimeout;
        }

        this.instanceConsumerKey = oauthConsumerKey;
        this.instanceConsumerSecret = oauthConsumerSecret;
        this.indivoBase = baseURL;
                
        if (baseURL.length() > 0 && baseURL.charAt(baseURL.length() -1) != '/') {
            this.indivoBase += '/';
        }

        if (responseTypeConversion != null) {
            this.responseTypeConversion = responseTypeConversion;
        } else {
            this.responseTypeConversion = new DefaultResponseTypeConversion();
        }

        clientUtils = new Utils(instanceConsumerKey, instanceConsumerSecret, this.indivoBase,
                this.responseTypeConversion, this.defaultHttpTimeout);

        if (this.responseTypeConversion instanceof DefaultResponseTypeConversion) {
            internalResponseTypeConversion = (DefaultResponseTypeConversion) this.responseTypeConversion;
        } else {
            internalResponseTypeConversion = new DefaultResponseTypeConversion();
        }


        documentBuilderFactory = DocumentBuilderFactory.newInstance();
                try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new IndivoClientException(pce);
        }
    }

    /**
    * Use this constructor when there is not
    * a default http timeout
    */
/*_JAVA_STYLE_ONLY*/
    public Rest_SHELL(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
    public Rest_py_client_style_SHELL(
/*_END_PYTHON_STYLE_ONLY*/
            String oauthConsumerKey,
            String oauthConsumerSecret,
            String baseURL,
            ResponseTypeConversion responseTypeConversion) throws IndivoClientException {
        this(oauthConsumerKey, oauthConsumerSecret, baseURL, responseTypeConversion, null);
    }

    /**
    * Use this constructor when there is not to be any default installation
    * or default consumer or default http timeout
    */
/*_JAVA_STYLE_ONLY*/
    public Rest_SHELL(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
    public Rest_py_client_style_SHELL(
/*_END_PYTHON_STYLE_ONLY*/

            ResponseTypeConversion responseTypeConversion) throws IndivoClientException {
        this(null, null, null, responseTypeConversion, null);
    }
    
    /**
    * Use this constructor when there is not to be any default installation
    * or default consumer
    */
/*_JAVA_STYLE_ONLY*/
    public Rest_SHELL(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
    public Rest_py_client_style_SHELL(
/*_END_PYTHON_STYLE_ONLY*/
            ResponseTypeConversion responseTypeConversion, Integer httpDefaultTimeout) throws IndivoClientException {
        this(null, null, null, responseTypeConversion, httpDefaultTimeout);
    }


    /***START AUTO GENERATED FROM WIKI*/
    /***END AUTO GENERATED FROM WIKI*/


    public Object oauth_request_tokenPOST(
            String oauth_callback, String indivo_record_id, Map<String,Object> options)
            throws IndivoClientException {
        String iri = "";
        if (indivo_record_id != null && indivo_record_id.length() > 0) {
            iri = "&indivo_record_id=" + indivo_record_id;
        }
    return clientRequest("POST", "oauth/request_token",
            null,
            null, null,
            "oauth_callback=" + oauth_callback + iri,
            "application/x-www-form-urlencoded",
            "application/x-www-form-urlencoded", options);
    }


    public Object oauth_access_tokenPOST(
            String oauth_token, String oauth_secret, String oauth_verifier)
            throws IndivoClientException {
        return clientRequest("POST", "oauth/access_token",
                null,
                oauth_token, oauth_secret,
                "oauth_verifier=" + oauth_verifier,
                "application/x-www-form-urlencoded",
                "application/x-www-form-urlencoded", null);
    }

   /** get all app specific (not record specific) documents
   * see apps_X_documents_GET and apps_X_documents_XGET
   * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * param appId application id
   */
    public List<Object> apps_X_documents_GET_HL(String pagingOrderingQuery, String appId, Map<String,Object> options)
        throws IndivoClientException {
        Map<String,Object> options0 = null;
        if (options == null) { options0 = new HashMap<String,Object>(); }
        else { options0 = new HashMap<String,Object>(options); }
        // make sure DOM object returned
        options0.put("responseTypeConversion", internalResponseTypeConversion);

        List<Object> retVal = new ArrayList<Object>();
/*_JAVA_STYLE_ONLY*/
        Document xmlList = (Document) apps_X_documents_GET(pagingOrderingQuery, appId, options0);
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
        Document xmlList = (Document) read_app_documents(pagingOrderingQuery, appId, options0);
/*_END_PYTHON_STYLE_ONLY*/
        Element docRoot = xmlList.getDocumentElement();
        NodeList docMetas = docRoot.getElementsByTagName("Document");
        for (int ii = 0; ii < docMetas.getLength(); ii++) {
            Element aMeta = (Element) docMetas.item(ii);
            String tsId = aMeta.getAttribute("id");
/*_JAVA_STYLE_ONLY*/
            Object tsDoc = apps_X_documents_XGET(appId, tsId, null, options);
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
            Object tsDoc = read_app_document(appId, tsId, null, options);
/*_END_PYTHON_STYLE_ONLY*/
            retVal.add(tsDoc);
        }
        return retVal;
    }
    /*_SHELL_DROP*/ private Object apps_X_documents_GET(Object o1, Object o2, Object o5) { return null; }


    /**
    *  GET /records/{record_id}/documents/{document_id}/rels/{rel_type}/
    *  GET /records/{record_id}/documents/{document_id}
    * @param queryString becomes ?{query_string} at end of URL.
    *     See <b>queryString param</b> note in the class description, above.
    * @param recordId record ID
    * @param docId document ID of the older document in the relationship
    * @param relType type of relationship
    * @param accessToken OAuth token for access to record
    * @param accessTokenSecret OAuth token secret for access to record
    */
    public List<Object> records_X_documents_X_rels_X_GET_HL(
            String queryString,
            String recordId,
            String docId,
            String relType,
            String accessToken,
            String accessTokenSecret,
            String responseContentType,
            Map<String,Object> options) throws IndivoClientException {
        if (! (responseTypeConversion instanceof DefaultResponseTypeConversion)) {
            if (options == null) { options = new HashMap<String,Object>(); }
            // make sure DOM object returned
            options.put("responseTypeConversion", internalResponseTypeConversion);
        }
/*_JAVA_STYLE_ONLY*/
        Document hasMetas = (Document) records_X_documents_X_rels_X_GET(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
        Document hasMetas = (Document) get_document_relate(
/*_END_PYTHON_STYLE_ONLY*/
            queryString,
            recordId,
            docId,
            relType,
            accessToken,
            accessTokenSecret, options);
        options.remove("responseTypeConversion"); // return list objects of caller preferred class
        List<Object> retVal = new ArrayList<Object>();
        NodeList relNL = hasMetas.getDocumentElement().getElementsByTagName("Document");
        for (int ii = 0; ii < relNL.getLength(); ii++) {
            Element aDocMeta = (Element) relNL.item(ii);
            String aId = aDocMeta.getAttribute("id");
            String relReq = "records/" + recordId + "/documents/" + aId;
            Object fromPhaRelated = clientRequest("GET", relReq, null, accessToken, accessTokenSecret, responseContentType, options);
            retVal.add((Document) fromPhaRelated);
        }

        return retVal;
    }
/*_SHELL_DROP*/ private Object records_X_documents_X_rels_X_GET(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) { return null; }
        /**
    * See GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta
    * and GET /records/{record_id}/apps/{app_id}/documents/{document_id}<br/>
    * This method first gets the metadata of the app specfic stored document of the given
    * external ID, then gets the actual app specific stored document by its Indivo ID.
    * return null if no such document found.
    *
    * @param appId application ID
    * @param xId external ID (application supplied ID)
    * @param responseContentType of http response body (expected type to get back, or null).
    * @return the requested document, or null if no document of that
    *    external Id exists.
    */
    public Object apps_X_documents_external_XGET_HL(
            String appId,
            String xId,
            String responseContentType,
            Map<String,Object> options) throws IndivoClientException {
        //need meta first!!!
        Object ofId = null;
        Object xMeta = null;
        if (options == null) {
            options = new HashMap<String,Object>();
        }
        options.put("responseTypeConversion", internalResponseTypeConversion);
        try {
/*_JAVA_STYLE_ONLY*/
            xMeta = apps_X_documents_external_X_metaGET(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
            xMeta = read_app_document_ext_meta(
/*_END_PYTHON_STYLE_ONLY*/
                appId, xId, options);
        } catch (org.indivo.client.IndivoClientExceptionHttp404 notfoundex) {
            logger.info("turning not found exception into null", notfoundex);
            xMeta = null;
        }

        if (xMeta != null) {
            Element docEl = ((Document) xMeta).getDocumentElement();
            if (! docEl.getTagName().equals("Document")) {
                throw new IndivoClientException(
                        "getAppDocumentXtrnlMeta returned document element with tag != 'Document'");
            }
                String docId = docEl.getAttribute("id");
                options.remove("responseTypeConversion");
/*_JAVA_STYLE_ONLY*/
                ofId = apps_X_documents_XGET(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
                ofId = read_app_document(
/*_END_PYTHON_STYLE_ONLY*/
                        appId, docId, responseContentType, options);
        }

        return ofId;
    }
/*_SHELL_DROP*/ private Object apps_X_documents_external_X_metaGET(
/*_SHELL_DROP*/    Object o2, Object o3, Object o6)
/*_SHELL_DROP*/ throws org.indivo.client.IndivoClientExceptionHttp404 { return null; }
/*_SHELL_DROP*/ private Object apps_X_documents_XGET(Object o2, Object o3, Object o4, Object o9) { return null; }

        /**
    * See GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta
    * and GET /records/{record_id}/apps/{app_id}/documents/{document_id}<br/>
    * This method first gets the metadata of the app specfic stored document of the given
    * external ID, then gets the actual app specific stored document by its Indivo ID.
    * return null if no such document found.
    *
    * @param recordId record ID
    * @param appId application ID
    * @param xId external ID (application supplied ID)
    * @param accessToken OAuth token for access to record
    * @param accessTokenSecret OAuth token secret for access to record
    * @return the requested document, or null if no document of that
    *    external Id exists.
    */
    public Object records_X_apps_X_documents_external_XGET_HL(
            String recordId,
            String appId,
            String xId,
            String accessToken,
            String accessTokenSecret,
            String responseContentType,
            Map<String,Object> options) throws IndivoClientException {
        //need meta first!!!
        Object ofId = null;
        Object xMeta = null;
        if (options == null) {
            options = new HashMap<String,Object>();
        }
        options.put("responseTypeConversion", internalResponseTypeConversion);
        try {
/*_JAVA_STYLE_ONLY*/
            xMeta = records_X_apps_X_documents_external_X_metaGET(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
            xMeta = read_recapp_document_ext_meta(
/*_END_PYTHON_STYLE_ONLY*/
                recordId, appId, xId, accessToken, accessTokenSecret, options);
        } catch (org.indivo.client.IndivoClientExceptionHttp404 notfoundex) {
            logger.info("turning not found exception into null", notfoundex);
            xMeta = null;
        }

        if (xMeta != null) {
            Element docEl = ((Document) xMeta).getDocumentElement();
            if (! docEl.getTagName().equals("Document")) {
                throw new IndivoClientException(
                        "getAppDocumentXtrnlMeta returned document element with tag != 'Document'");
            }
                String docId = docEl.getAttribute("id");
                options.remove("responseTypeConversion");
/*_JAVA_STYLE_ONLY*/
                ofId = records_X_apps_X_documents_XGET(
/*_END_JAVA_STYLE_ONLY*/
/*_PYTHON_STYLE_ONLY*/
                ofId = get_recapp_document(
/*_END_PYTHON_STYLE_ONLY*/
                        recordId, appId, docId, accessToken, accessTokenSecret, responseContentType, options);
        }

        return ofId;
    }
/*_SHELL_DROP*/ private Object records_X_apps_X_documents_external_X_metaGET(
/*_SHELL_DROP*/    Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
/*_SHELL_DROP*/ throws org.indivo.client.IndivoClientExceptionHttp404 { return null; }
/*_SHELL_DROP*/ private Object records_X_apps_X_documents_XGET(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) { return null; }


	private void checkQueryOptions(Object present, List<String> allowed0, List<String> allowed1, Map<String, Class> vqf)
			throws IndivoClientException {
		checkQueryOptions(present, allowed0, allowed1, vqf, "query option");
	}
	private void checkQueryOptions(Object present, List<String> allowed0, List<String> allowed1, Map<String, Class> vqf, String errornote)
			throws IndivoClientException {
        if (present == null) { present = ""; }
        List<String> presentKeys = new ArrayList<String>();
		if (present instanceof String) {
			String[] presentA = null;
			if (((String)present).length() == 0) {
				presentA = new String[0];
			} else {
				presentA = ((String)present).split("&");
			}
			for (String prsnt0 : presentA) {
				int eix = prsnt0.indexOf("=");
				if (eix == -1) {
					throw new IndivoClientException("query option without '=': \"" + prsnt0 + "\" in " + present);				
				}
				presentKeys.add(prsnt0.substring(0, eix));
			}
		} else if (present instanceof Map) {
			List presentKeys0 = new ArrayList(((Map)present).keySet());
            for (Object pk0 : presentKeys0) {
                if (! (pk0 instanceof String)) {
                    throw new IndivoClientException(
                            "queryOptions, if a Map, must be Map<String,String> or Map<String,String[]>.  non-String key found:"
                            + pk0 + " -- " + pk0.getClass().getName());
                }
                Object pk0v = ((Map)present).get(pk0);
                if (! (pk0v instanceof String || pk0v instanceof String[])) {
                    throw new IndivoClientException(
                            "queryOptions, if a Map, must be Map<String,String> or Map<String,String[]>. "
                            + " Value found that was not String or String[]: "
                            + pk0v + " -- " + pk0v.getClass().getName());
                }
                presentKeys.add((String) pk0);
            }
		} else {
		    throw new IndivoClientException("Query options should be String or Map<String,String> or Map<String,String[]>");
		}

		if (allowed0 == null) { allowed0 = new ArrayList<String>(); }
		List<String> allowed = null;
		if (allowed1 == null) {
			allowed = allowed0;
		} else {
			allowed = new ArrayList<String>(allowed0);
			allowed.addAll(allowed1);
		}

        List<String> prsntL = new ArrayList<String>();
        for (String prsnt : presentKeys) {
			if (allowed.contains(prsnt)) {
				if (prsntL.contains(prsnt)) {
					throw new IndivoClientException("multiple occurances of " + errornote + " \"" + prsnt + "\" in " + present);
				}
				else {
					prsntL.add(prsnt);
				}
			}
			else if (vqf != null && vqf.get(prsnt) != null) {
				Class expectedClass = vqf.get(prsnt);
				if (expectedClass == Number.class) {
					try { new Float(prsnt); } catch(NumberFormatException nfe) { throw new IndivoClientException(nfe); }
				}
				else if (expectedClass == java.util.Date.class) {
					
				}
			}
			else {
				throw new IndivoClientException("unexpected " + errornote + ": " + prsnt);
			}
		}
	}


    /** convenience method for where no request body is sent
     * and the return type is "application/xml"
    */
    public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Map<String,Object> options) throws IndivoClientException {
        return clientRequest(reqMeth, reletivePath, queryString, phaToken, phaTokenSecret, null, null, "application/xml", options);
    }
    
    /** conveninece method for where no request body is sent */
    public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object responseContentType,
            Map<String,Object> options) throws IndivoClientException {
        return clientRequest(reqMeth, reletivePath, queryString, phaToken, phaTokenSecret, null, null, responseContentType, options);
    }

    /** conveninece method for where return type is "application/xml" */
    public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object requestBody,   // String or byte[]
            String requestContentType,
            Map<String,Object> options) throws IndivoClientException {
        return clientRequest(reqMeth, reletivePath, queryString,
                phaToken, phaTokenSecret, requestBody, requestContentType, "application/xml", options);
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
    * @param requestContentType content type may be specified, otherwise clientRequest will guess.
    * @param responseContentType response content type may be specified, otherwise anything goes.
    *    This can be either one specific contentType as a String, or a String[] of allowable contentTypes.
    * @param options possible options include: indivoInstallation; sockentTimeout; connectionTimeout
    */
    public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object requestBody,   // String or byte[]
            String requestContentType,
            Object responseContentType,
            Map<String,Object> options) throws IndivoClientException {

        return clientUtils.indivoRequest(
            reqMeth,
            reletivePath,
            queryString,
            phaToken,
            phaTokenSecret,
            requestBody,   // String or byte[]
            requestContentType,
            responseContentType,
            options);
    }
    
    


    /**
    *  Given a document containing a list of metadata ("Document")
    * elements, return a list of just the document Id-s.
    * @param hasMetas the document with document element tag of
    *     <code>&lt;Documents&gt;</code>
    */
    public List<String> utilDocIdsFromMetas(Document hasMetas) {
        List<String> retVal = new ArrayList<String>();
        Element docElem = hasMetas.getDocumentElement();
        NodeList deNL = docElem.getElementsByTagName("Document");
        for (int ii = 0; ii < deNL.getLength(); ii++) {
            Element docEl = (Element) deNL.item(ii);
            String docId = docEl.getAttribute("id");
            retVal.add(docId);
        }
        return retVal;
    }


    private String buildFormURLEnc(String[][] params) throws IndivoClientException {
        StringBuffer retVal = new StringBuffer();
        for (int ii = 0; ii < params.length; ii++) {
            String[] aparam = params[ii];
            if (aparam[1] != null) {
                if (retVal.length() > 0) {
                    retVal.append('&');
                }
                try {
                    retVal.append(aparam[0] + '=' + URLEncoder.encode(aparam[1],"UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    throw new IndivoClientException(uee);
                }
            }
        }
        //new String[][] {{ "primary_secret", primary_secret }, { "secondary_secret", secondary_secret } }
        return retVal.toString();
    }


    public Utils getUtils() { return clientUtils; }

    public String defaultURL() { return this.indivoBase; }

    @Override
    public String toString() {
        return  this.getClass().getName() + ": " + this.instanceConsumerKey
            + "; " + this.instanceConsumerSecret + "; " + this.indivoBase;
    }
}
