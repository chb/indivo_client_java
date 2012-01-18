package org.indivo.client;


//import java.io.OutputStream;
//import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
* plus a few utilites and a few higher-level methods<br/><br/>
*
* HERE ARE THE RULES FOR TRANSLATING A REST URL TO A JAVA API METHOD NAME.
* <ul>
* <li>code REST '/' as Java '_', except omit the leading '/'</li>
* <li>where ever there is a variable, use 'X'</li>
* <li>When REST call includes a dash ("-"), remove the dash and upper-case the first character that followed
*   the dash.</li>
* <li>use request method (GET|PUT|POST|DELETE) as last part of method name.</li>
* <li>for REST calls where one or more ?queryString params are specifically documented,
*   the documented params are the first Java method params.</li>
* <li>for REST calls that can include a ?queryString, queryString is the first Java
*   method param, or first after a documented query params.</li>
* <li>variables in the REST url map to parameters in their respective sequence.</li>
* <li>record token/secret are the last two parameters when there is no
*   HTTP content to be sent.  record token/secret are the last two paramters before
*   HTTP content when HTTP content present.  App specific, non record specific methods
*   do not have token/secret params.  Calls that can only be made by an Administrator
*   DO have token/secret params though these would always be null (token/secret params
*   present in anticipation of future releases of Indivo which might have some changed
*   to what identities can make which calls).</li>
* <li>where content is to be supplied in the call, that is the last parameter before the
*     content type params (if present) and options map.</li>
* <li>each REST call of the form: <code>GET /records/{record_id}/reports/minimal/&lt;type_eg_medications&gt;/</code>
*   or of the form: <code>GET /carenets/{carenet_id}/reports/minimal/&lt;type_eg_medications&gt;/</code>
*   is combined into one of two methods as if the REST calls were documented as:
*   <code>GET /records/{record_id}/reports/minimal/{type_of_minimal_eg_medications}/ OR
*   <code>GET /carenets/{carenet_id}/reports/minimal/{type_of_minimal_eg_medications}/</li>
* <li>Higher level methods that are named as if they map to a REST call,
*   but that do more than map directly to a single
*   REST call, are named according to the fictional REST URL that would seem to do their function,
*   but the method name is given "_HL" as a suffix.</li>
* </ul>
* <h3>queryString param</h3>
* <p>For all methods that return a list and have a  generic query string that determines
*     paging and ordering of the results, a queryString parameter, queryString
*     represents that part of URL following '?', but not including '?'.
*     queryString can be a String or Map&lt;String,Object&gt;.
*     Where type is Map, each value must be of type String or String[].  Where one key
*     has multiple values, type String[] must be used for that key's value.
*     Where no queryString is needed, null, "", or Map will all work.
* </p>
* <pre>
*    Example of a String: a=x%3dy&b=z
*    Example of Map&lt;String,Object&gt;
*       Map&lt;String,Object&gt; query = new HashMap&lt;String,Object&gt;()
*       query.put("a","x=y");
*       String[] value = new String[2]; value[0] = "z"; value[1] = "w";
*       query.put("b",value);
* </pre>
*/
public class Rest {
    private static final long serialVersionUID = 1L;

    /** medical report name constant.  medical reports names other than those represented by constants
    *  might be valid.  Constants are provided for these known as of January 22, 2010.
    *  Follow measurements with "/{lab-code}/".
    */
    public static final String   // known medical reports
        MEASUREMENTS = "measurements";

    /** medical report name constant.  medical reports names other than those represented by constants
    *  might be valid.  Constants are provided for these known as of January 22, 2010.
    */
    public static final String   // known medical reports
    	MEDICATIONS = "medications",
    	ALLERGIES = "allergies",
    	EQUIPMENT = "equipment",
    	IMMUNIZATIONS = "immunizations",
    	PROCEDURES = "procedures",
    	PROBLEMS = "problems",
    	VITALS = "vitals";
    
    public static List<String> auditQueryField = null;
    {
    	Arrays.asList("document_id","external_id","request_date","function_name","principal_email","proxied_by_email");
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
    public Rest(
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
    public Rest(
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
    public Rest(
            ResponseTypeConversion responseTypeConversion) throws IndivoClientException {
        this(null, null, null, responseTypeConversion, null);
    }
    
    /**
    * Use this constructor when there is not to be any default installation
    * or default consumer
    */
    public Rest(
            ResponseTypeConversion responseTypeConversion, Integer httpDefaultTimeout) throws IndivoClientException {
        this(null, null, null, responseTypeConversion, httpDefaultTimeout);
    }


private Map<String, List<String>> commonOptionsMap = new HashMap<String,List<String>>();
{
    commonOptionsMap.put("solo", Arrays.asList("limit", "offset", "order_by", "status"));
    commonOptionsMap.put("olo", Arrays.asList("limit", "offset", "order_by"));
    commonOptionsMap.put("soaddglo", Arrays.asList("aggregate_by", "date_group", "date_range", "group_by", "limit", "offset", "order_by", "status"));
    commonOptionsMap.put("stolo", Arrays.asList("limit", "offset", "order_by", "status", "type"));
    }

private Map<String, List<String>> validQueryFields = new HashMap<String,List<String>>();
private Map<String, Class> queryFieldType = new HashMap<String, Class>();
{
    List<String> vqfs = null;
    vqfs = Arrays.asList("medication_name", "medication_brand_name", "date_started", "date_stopped");
    validQueryFields.put("medication", vqfs);
    vqfs = Arrays.asList("specialty", "provider_name", "date_of_visit");
    validQueryFields.put("simple", vqfs);
    vqfs = Arrays.asList("date_diagnosed", "allergen_type", "allergen_name");
    validQueryFields.put("allergy", vqfs);
    vqfs = Arrays.asList("lab_type", "date_measured", "lab_test_name");
    validQueryFields.put("lab", vqfs);
    vqfs = Arrays.asList("date_started", "date_stopped", "equipment_name", "equipment_vendor");
    validQueryFields.put("equipment", vqfs);
    vqfs = Arrays.asList("vaccine_type", "date_administered");
    validQueryFields.put("immunization", vqfs);
    vqfs = Arrays.asList("category", "value", "date_measured");
    validQueryFields.put("vital", vqfs);
    vqfs = Arrays.asList("lab_code", "value", "date_measured");
    validQueryFields.put("measurement", vqfs);
    vqfs = Arrays.asList("problem_name", "date_onset", "date_resolution");
    validQueryFields.put("problem", vqfs);
    vqfs = Arrays.asList("procedure_name", "date_performed");
    validQueryFields.put("procedure", vqfs);

    queryFieldType.put("medication_name", "String.class");
    queryFieldType.put("lab_code", "String.class");
    queryFieldType.put("lab_type", "String.class");
    queryFieldType.put("date_resolution", "Date.class");
    queryFieldType.put("allergen_type", "String.class");
    queryFieldType.put("date_performed", "Date.class");
    queryFieldType.put("value", "Number.class");
    queryFieldType.put("category", "String.class");
    queryFieldType.put("date_stopped", "Date.class");
    queryFieldType.put("equipment_name", "String.class");
    queryFieldType.put("provider_name", "String.class");
    queryFieldType.put("procedure_name", "String.class");
    queryFieldType.put("date_measured", "Date.class");
    queryFieldType.put("specialty", "String.class");
    queryFieldType.put("date_of_visit", "Date.class");
    queryFieldType.put("vaccine_type", "String.class");
    queryFieldType.put("date_started", "Date.class");
    queryFieldType.put("date_diagnosed", "Date.class");
    queryFieldType.put("problem_name", "String.class");
    queryFieldType.put("date_administered", "Date.class");
    queryFieldType.put("allergen_name", "String.class");
    queryFieldType.put("medication_brand_name", "String.class");
    queryFieldType.put("equipment_vendor", "String.class");
    queryFieldType.put("date_onset", "Date.class");
    queryFieldType.put("lab_test_name", "String.class");
}

    /**
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_POST(
            Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/", "", null, null, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_searchGET(
            String queryOptions, Map<String, Object> options) {
        List<String> optional = Arrays.asList("contact_email", "fullname");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "accounts/search", queryOptions, null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_XGET(
            String accountEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_authsystems_POST(
            String accountEmail, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/authsystems/", "", null, null, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_authsystems_password_changePOST(
            String accountEmail, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/authsystems/password/change", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_authsystems_password_setPOST(
            String accountEmail, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/authsystems/password/set", "", null, null, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_authsystems_password_setUsernamePOST(
            String accountEmail, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/authsystems/password/set-username", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param primarySecret A confirmation string sent securely to the patient from Indivo
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_checkSecrets_XGET(
            String queryOptions, String accountEmail, String primarySecret, Map<String, Object> options) {
        List<String> optional = Arrays.asList("secondary_secret");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/check-secrets/" + primarySecret, queryOptions, null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_forgotPasswordPOST(
            String accountEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/forgot-password", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_inbox_GET(
            String queryOptions, String accountEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        List<String> optional = Arrays.asList("include_archive", "limit", "offset", "order_by", "status");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/inbox/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_inbox_POST(
            String accountEmail, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/inbox/", "", null, null, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param messageId The unique identifier of the Indivo Message
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_inbox_XGET(
            String accountEmail, String messageId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/inbox/" + messageId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param messageId The unique identifier of the Indivo Message
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_inbox_X_archivePOST(
            String accountEmail, String messageId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/inbox/" + messageId + "/archive", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param messageId The unique identifier of the Indivo Message
     * @param attachmentNum The 1-indexed number corresponding to the message attachment
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_inbox_X_attachments_X_acceptPOST(
            String accountEmail, String messageId, String attachmentNum, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/inbox/" + messageId + "/attachments/" + attachmentNum + "/accept", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_infoSetPOST(
            String accountEmail, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/info-set", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param primarySecret A confirmation string sent securely to the patient from Indivo
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_initialize_XPOST(
            String accountEmail, String primarySecret, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/initialize/" + primarySecret, "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_notifications_GET(
            String queryOptions, String accountEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("solo"), null);
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/notifications/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_permissions_GET(
            String accountEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/permissions/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_primarySecretGET(
            String accountEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/primary-secret", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_records_GET(
            String queryOptions, String accountEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("solo"), null);
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/records/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_resetPOST(
            String accountEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/reset", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_secretGET(
            String accountEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "accounts/" + accountEmail + "/secret", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_secretResendPOST(
            String accountEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/secret-resend", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param accountEmail The email identifier of the Indivo account
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object accounts_X_setStatePOST(
            String accountEmail, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "accounts/" + accountEmail + "/set-state", "", null, null, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_GET(
            Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "apps/", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_XDELETE(
            String phaEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "apps/" + phaEmail, "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_XGET(
            String phaEmail, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "apps/" + phaEmail, "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_GET(
            String queryOptions, String phaEmail, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("stolo"), null);
        Object fromRequest = clientRequest(
                "GET", "apps/" + phaEmail + "/documents/", queryOptions, null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_POST(
            String phaEmail, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "apps/" + phaEmail + "/documents/", "", null, null, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_external_XPUT(
            String phaEmail, String externalId, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "apps/" + phaEmail + "/documents/external/" + externalId, "", null, null, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_external_X_metaGET(
            String phaEmail, String externalId, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "apps/" + phaEmail + "/documents/external/" + externalId + "/meta", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_XDELETE(
            String phaEmail, String documentId, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "apps/" + phaEmail + "/documents/" + documentId, "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_XGET(
            String phaEmail, String documentId, Object responseContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "apps/" + phaEmail + "/documents/" + documentId, "", null, null, reponseContentType);
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_XPUT(
            String phaEmail, String documentId, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "apps/" + phaEmail + "/documents/" + documentId, "", null, null, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_X_labelPUT(
            String phaEmail, String documentId, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "apps/" + phaEmail + "/documents/" + documentId + "/label", "", null, null, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object apps_X_documents_X_metaGET(
            String phaEmail, String documentId, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "apps/" + phaEmail + "/documents/" + documentId + "/meta", "", null, null, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_XDELETE(
            String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "carenets/" + carenetId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_accounts_GET(
            String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/accounts/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_accounts_POST(
            String carenetId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "carenets/" + carenetId + "/accounts/", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accountId The email identifier of the Indivo account
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_accounts_XDELETE(
            String carenetId, String accountId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "carenets/" + carenetId + "/accounts/" + accountId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accountId The email identifier of the Indivo account
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_accounts_X_permissionsGET(
            String carenetId, String accountId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/accounts/" + accountId + "/permissions", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_apps_GET(
            String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/apps/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_apps_XDELETE(
            String carenetId, String phaEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "carenets/" + carenetId + "/apps/" + phaEmail, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_apps_XPUT(
            String carenetId, String phaEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "carenets/" + carenetId + "/apps/" + phaEmail, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_apps_X_permissionsGET(
            String carenetId, String phaEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/apps/" + phaEmail + "/permissions", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_documents_GET(
            String queryOptions, String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        List<String> optional = Arrays.asList("type");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/documents/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param specialDocument The type of special document to access. Options are ``demographics``, ``contact``
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_documents_special_XGET(
            String carenetId, String specialDocument, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/documents/special/" + specialDocument, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_documents_XGET(
            String carenetId, String documentId, String accessToken, String accessTokenSecret, Object responseContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/documents/" + documentId, "", accessToken, accessTokenSecret, reponseContentType);
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_documents_X_metaGET(
            String carenetId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/documents/" + documentId + "/meta", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_recordGET(
            String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/record", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_renamePOST(
            String carenetId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "carenets/" + carenetId + "/rename", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param reportFlavor one of: allergy, equipment, immunization, lab, medication, problem, procedure, simple, vital
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_reports_minimal_X_GET(
            String queryOptions, String carenetId, String reportFlavor, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), validQueryFields.get(reportFlavor));
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/reports/minimal/" + reportFlavor + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param labCode The identifier corresponding to the measurement being made.
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_reports_minimal_measurements_X_GET(
            String queryOptions, String carenetId, String labCode, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), null);
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/reports/minimal/measurements/" + labCode + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param carenetId The id string associated with the Indivo carenet
     * @param category The category of vital sign, i.e. ``weight``, ``Blood_Pressure_Systolic``
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object carenets_X_reports_minimal_vitals_XGET(
            String queryOptions, String carenetId, String category, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), null);
        Object fromRequest = clientRequest(
                "GET", "carenets/" + carenetId + "/reports/minimal/vitals/" + category, queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object codes_systems_GET(
            String accessToken, String accessTokenSecret, Object responseContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "codes/systems/", "", accessToken, accessTokenSecret, reponseContentType);
        return fromRequest;
    }

    /**
     * @param systemShortName 
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object codes_systems_X_queryGET(
            String queryOptions, String systemShortName, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        List<String> optional = Arrays.asList("q");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "codes/systems/" + systemShortName + "/query", queryOptions, accessToken, accessTokenSecret, "application/json");
        return fromRequest;
    }

    /**
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_access_tokenPOST(
            Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "oauth/access_token", "", null, null, "application/x-www-form-urlencoded");
        return fromRequest;
    }

    /**
     * @param reqtokenId 
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_internal_request_tokens_X_approvePOST(
            String reqtokenId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "oauth/internal/request_tokens/" + reqtokenId + "/approve", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/x-www-form-urlencoded");
        return fromRequest;
    }

    /**
     * @param reqtokenId 
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_internal_request_tokens_X_claimPOST(
            String reqtokenId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "oauth/internal/request_tokens/" + reqtokenId + "/claim", "", accessToken, accessTokenSecret, "text/plain");
        return fromRequest;
    }

    /**
     * @param reqtokenId 
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_internal_request_tokens_X_infoGET(
            String reqtokenId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "oauth/internal/request_tokens/" + reqtokenId + "/info", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_internal_session_createPOST(
            String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "oauth/internal/session_create", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/x-www-form-urlencoded");
        return fromRequest;
    }

    /**
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_internal_surlVerifyGET(
            String queryOptions, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        List<String> optional = Arrays.asList("surl_sig", "surl_timestamp", "surl_token");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "oauth/internal/surl-verify", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object oauth_request_tokenPOST(
            String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "oauth/request_token", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/x-www-form-urlencoded");
        return fromRequest;
    }

    /**
     * @param body data to send, must be in application/xml form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_POST(
            Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/", "", null, null, body, "application/xml", "application/xml");
        return fromRequest;
    }

    /**
     * @param principalEmail The email with which to scope an external id.
     * @param externalId The external identifier of the desired resource
     * @param body data to send, must be in application/xml form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_external_X_XPUT(
            String principalEmail, String externalId, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/external/" + principalEmail + '/' + externalId, "", null, null, body, "application/xml", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_XGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_GET(
            String queryOptions, String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        List<String> optional = Arrays.asList("type");
        checkQueryOptions(queryOptions, optional, null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/apps/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_XDELETE(
            String recordId, String phaEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "records/" + recordId + "/apps/" + phaEmail, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_XGET(
            String recordId, String phaEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/apps/" + phaEmail, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_GET(
            String queryOptions, String recordId, String phaEmail, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("stolo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/apps/" + phaEmail + "/documents/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_POST(
            String recordId, String phaEmail, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/apps/" + phaEmail + "/documents/", "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_external_XPOST(
            String recordId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/apps/" + phaEmail + "/documents/external/" + externalId, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_external_XPUT(
            String recordId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/apps/" + phaEmail + "/documents/external/" + externalId, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_external_X_metaGET(
            String recordId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/apps/" + phaEmail + "/documents/external/" + externalId + "/meta", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_XDELETE(
            String recordId, String phaEmail, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "records/" + recordId + "/apps/" + phaEmail + "/documents/" + documentId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_XGET(
            String recordId, String phaEmail, String documentId, String accessToken, String accessTokenSecret, Object responseContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/apps/" + phaEmail + "/documents/" + documentId, "", accessToken, accessTokenSecret, reponseContentType);
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_X_labelPUT(
            String recordId, String phaEmail, String documentId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/apps/" + phaEmail + "/documents/" + documentId + "/label", "", accessToken, accessTokenSecret, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_documents_X_metaGET(
            String recordId, String phaEmail, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/apps/" + phaEmail + "/documents/" + documentId + "/meta", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_apps_X_setupPOST(
            String recordId, String phaEmail, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/apps/" + phaEmail + "/setup", "", null, null, body, requestContentType, "application/x-www-form-urlencoded");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_audits_GET(
            String queryOptions, String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("olo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/audits/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_audits_documents_X_GET(
            String queryOptions, String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("olo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/audits/documents/" + documentId + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param functionName The internal Indivo function name called by the API request
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_audits_documents_X_functions_X_GET(
            String queryOptions, String recordId, String documentId, String functionName, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("olo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/audits/documents/" + documentId + "/functions/" + functionName + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_audits_query_GET(
            String queryOptions, String recordId, String auditQueryField, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), auditQueryFields);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/audits/query/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_autoshare_bytype_GET(
            String type, String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/autoshare/bytype/", type, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_autoshare_bytype_allGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/autoshare/bytype/all", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_autoshare_carenets_X_bytype_setPOST(
            String recordId, String carenetId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/autoshare/carenets/" + carenetId + "/bytype/set", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_autoshare_carenets_X_bytype_unsetPOST(
            String recordId, String carenetId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/autoshare/carenets/" + carenetId + "/bytype/unset", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_carenets_GET(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/carenets/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_carenets_POST(
            String recordId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/carenets/", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_DELETE(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "records/" + recordId + "/documents/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_GET(
            String queryOptions, String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("stolo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_POST(
            String recordId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/", "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_external_X_XPUT(
            String recordId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/external/" + phaEmail + '/' + externalId, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_external_X_X_labelPUT(
            String recordId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/external/" + phaEmail + '/' + externalId + "/label", "", accessToken, accessTokenSecret, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_external_X_X_metaGET(
            String recordId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/external/" + phaEmail + '/' + externalId + "/meta", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param specialDocument The type of special document to access. Options are ``demographics``, ``contact``
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_special_XGET(
            String recordId, String specialDocument, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/special/" + specialDocument, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param specialDocument The type of special document to access. Options are ``demographics``, ``contact``
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_special_XPOST(
            String recordId, String specialDocument, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/special/" + specialDocument, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param specialDocument The type of special document to access. Options are ``demographics``, ``contact``
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_special_XPUT(
            String recordId, String specialDocument, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/special/" + specialDocument, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId0 The id of the document that is the object of the relationship, i.e. DOCUMENT_ID_0 *is annotated by* DOCUMENT_ID_1
     * @param rel The type of relationship between the documents, i.e. ``annotation``, ``interpretation``
     * @param documentId1 The id of the document that is the subject of the relationship, i.e. DOCUMENT_ID_1 *annotates* DOCUMENT_ID_0
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_rels_X_XPUT(
            String recordId, String documentId0, String rel, String documentId1, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId0 + "/rels/" + rel + '/' + documentId1, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_XGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Object responseContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/" + documentId, "", accessToken, accessTokenSecret, reponseContentType);
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_carenets_GET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/" + documentId + "/carenets/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_carenets_XDELETE(
            String recordId, String documentId, String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "records/" + recordId + "/documents/" + documentId + "/carenets/" + carenetId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_carenets_XPUT(
            String recordId, String documentId, String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId + "/carenets/" + carenetId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param carenetId The id string associated with the Indivo carenet
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_carenets_X_autoshareRevertPOST(
            String recordId, String documentId, String carenetId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/" + documentId + "/carenets/" + carenetId + "/autoshare-revert", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_labelPUT(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId + "/label", "", accessToken, accessTokenSecret, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_metaGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/" + documentId + "/meta", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_metaPUT(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId + "/meta", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_nevershareDELETE(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "records/" + recordId + "/documents/" + documentId + "/nevershare", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_neversharePUT(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId + "/nevershare", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param rel The type of relationship between the documents, i.e. ``annotation``, ``interpretation``
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_rels_X_GET(
            String queryOptions, String recordId, String documentId, String rel, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("solo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/" + documentId + "/rels/" + rel + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param rel The type of relationship between the documents, i.e. ``annotation``, ``interpretation``
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_rels_X_POST(
            String recordId, String documentId, String rel, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/" + documentId + "/rels/" + rel + "/", "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param rel The type of relationship between the documents, i.e. ``annotation``, ``interpretation``
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_rels_X_external_X_XPOST(
            String recordId, String documentId, String rel, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/" + documentId + "/rels/" + rel + "/external/" + phaEmail + '/' + externalId, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param rel The type of relationship between the documents, i.e. ``annotation``, ``interpretation``
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_rels_X_external_X_XPUT(
            String recordId, String documentId, String rel, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId + "/rels/" + rel + "/external/" + phaEmail + '/' + externalId, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_replacePOST(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/" + documentId + "/replace", "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param phaEmail The email identifier of the Indivo user app
     * @param externalId The external identifier of the desired resource
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send
     * @param requestContentType mime type of body.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_replace_external_X_XPUT(
            String recordId, String documentId, String phaEmail, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/documents/" + documentId + "/replace/external/" + phaEmail + '/' + externalId, "", accessToken, accessTokenSecret, body, requestContentType, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_setStatusPOST(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/documents/" + documentId + "/set-status", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_statusHistoryGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/" + documentId + "/status-history", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param documentId The unique identifier of the Indivo document
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_documents_X_versions_GET(
            String queryOptions, String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("solo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/documents/" + documentId + "/versions/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param messageId The unique identifier of the Indivo Message
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_inbox_XPOST(
            String recordId, String messageId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/inbox/" + messageId, "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param messageId The unique identifier of the Indivo Message
     * @param attachmentNum The 1-indexed number corresponding to the message attachment
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_inbox_X_attachments_XPOST(
            String recordId, String messageId, String attachmentNum, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/inbox/" + messageId + "/attachments/" + attachmentNum, "", accessToken, accessTokenSecret, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_notifications_POST(
            String recordId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/notifications/", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_notifyPOST(
            String recordId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/notify", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_ownerGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/owner", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_ownerPOST(
            String recordId, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/owner", "", null, null, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param body data to send, must be in text/plain form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_ownerPUT(
            String recordId, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "PUT", "records/" + recordId + "/owner", "", null, null, body, "text/plain", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_reports_experimental_ccrGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/reports/experimental/ccr", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param reportFlavor one of: allergy, equipment, immunization, lab, medication, problem, procedure, simple, vital
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_reports_minimal_X_GET(
            String queryOptions, String recordId, String reportFlavor, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), validQueryFields.get(reportFlavor));
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/reports/minimal/" + reportFlavor + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param labCode The identifier corresponding to the measurement being made.
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_reports_minimal_measurements_X_GET(
            String queryOptions, String recordId, String labCode, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/reports/minimal/measurements/" + labCode + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param category The category of vital sign, i.e. ``weight``, ``Blood_Pressure_Systolic``
     * @param accessToken OAuth token.
     * @param accessTokenSecret OAuth secret.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_reports_minimal_vitals_X_GET(
            String queryOptions, String recordId, String category, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        checkQueryOptions(queryOptions, commonOptionsMap.get("soaddglo"), null);
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/reports/minimal/vitals/" + category + "/", queryOptions, accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_shares_GET(
            String recordId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "records/" + recordId + "/shares/", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param body data to send, must be in url_encoded form.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_shares_POST(
            String recordId, String accessToken, String accessTokenSecret, Object body, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/shares/", "", accessToken, accessTokenSecret, body, "application/x-www-form-urlencoded", "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param otherAccountId The email identifier of the Indivo account to share with
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_shares_XDELETE(
            String recordId, String otherAccountId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "DELETE", "records/" + recordId + "/shares/" + otherAccountId, "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param recordId The id string associated with the Indivo record
     * @param otherAccountId The email identifier of the Indivo account to share with
     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object records_X_shares_X_deletePOST(
            String recordId, String otherAccountId, String accessToken, String accessTokenSecret, Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "POST", "records/" + recordId + "/shares/" + otherAccountId + "/delete", "", accessToken, accessTokenSecret, "application/xml");
        return fromRequest;
    }

    /**
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
    */
    public Object versionGET(
            Map<String, Object> options) {
        Object fromRequest = clientRequest(
                "GET", "version", "", null, null, "application/xml");
        return fromRequest;
    }




    /*
    /++ conveninece method for where no request body is sent +/
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
*/
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
        else { options0 = new HashMap(options); }
        // make sure DOM object returned
        options0.put("responseTypeConversion", internalResponseTypeConversion);

        List<Object> retVal = new ArrayList<Object>();
        Document xmlList = (Document) apps_X_documents_GET(pagingOrderingQuery, appId, options0);
        Element docRoot = xmlList.getDocumentElement();
        NodeList docMetas = docRoot.getElementsByTagName("Document");
        for (int ii = 0; ii < docMetas.getLength(); ii++) {
            Element aMeta = (Element) docMetas.item(ii);
            String tsId = aMeta.getAttribute("id");
            Object tsDoc = apps_X_documents_XGET(appId, tsId, null, options);
            retVal.add(tsDoc);
        }
        return retVal;
    }


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
        Document hasMetas = (Document) records_X_documents_X_rels_X_GET(
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
            xMeta = apps_X_documents_external_X_metaGET(
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
                ofId = apps_X_documents_XGET(
                        appId, docId, responseContentType, options);
        }

        return ofId;
    }

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
            xMeta = records_X_apps_X_documents_external_X_metaGET(
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
                ofId = records_X_apps_X_documents_XGET(
                        recordId, appId, docId, accessToken, accessTokenSecret, responseContentType, options);
        }

        return ofId;
    }


	private void checkQueryOptions(String present, List<String> allowed, Map<String, Class> vqf) throws IndivoClientException {
		String[] presentA = present.split("&");
		List<String> prsntL = new ArrayList<String>();
		for (String prsnt : presentA) {
			if (allowed.contains(prsnt)) {
				if (prsntL.contains(prsnt)) {
					throw new IndivoClientException("multiple occurances of query option \"" + prsnt + "\" in " + present);
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
				throw new IndivoClientException("unexpected qurey option: " + prsnt);
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
