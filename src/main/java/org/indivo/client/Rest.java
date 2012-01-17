package org.indivo.client;


import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;


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


    /***START AUTO GENERATED FROM WIKI*/


    /** GET /accounts/{account_id}/records/
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_records_GET(
            String pagingOrderingQuery, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/" + accountId + "/records/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_XGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/apps/
    * ACCESSCONTROL   account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_GET(
            String pagingOrderingQuery, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/apps/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_GET(
            String pagingOrderingQuery, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_GET(
            String pagingOrderingQuery, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/types/{type}/ -- not implemented: indivo_server-v0.8.3.7
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param type XML schema datatype
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_types_X_GET(
            String pagingOrderingQuery, String recordId, String type, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/types/" + type + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/types/{type}/ -- not implemented: indivo_server-v0.8.3.7
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param type XML schema datatype
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_types_X_GET(
            String pagingOrderingQuery, String carenetId, String type, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/types/" + type + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/?type={type_url}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param type {type_url}
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_GET(
            String type, String pagingOrderingQuery, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "type", type } });
        if (pagingOrderingQuery != null && pagingOrderingQuery.length() > 0) { queryOut += "&" + pagingOrderingQuery; }
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/?type={type_url}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param type {type_url}
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_GET(
            String type, String pagingOrderingQuery, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "type", type } });
        if (pagingOrderingQuery != null && pagingOrderingQuery.length() > 0) { queryOut += "&" + pagingOrderingQuery; }
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/{document_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param responseContentType of http response body (expected type to get back, or null).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_XGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, String responseContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/" + documentId,
               queryOut, accessToken, accessTokenSecret, responseContentType, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/{document_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_document
    * @param carenetId carenetID for sharing
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param responseContentType of http response body (expected type to get back, or null).
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_XGET(
            String carenetId, String documentId, String accessToken, String accessTokenSecret, String responseContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/" + documentId,
               queryOut, accessToken, accessTokenSecret, responseContentType, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/special/demographics
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_special_demographicsGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/special/demographics",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/special/demographics
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_special_demographicsGET(
            String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/special/demographics",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/special/demographics
    * ACCESSCONTROL   machineapp: machapp_ruleset.machineapp_record_created_rule -- account: full_control
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param body body of http request (data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_special_demographicsPUT(
            String recordId, String accessToken, String accessTokenSecret, Object body, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/special/demographics",
               queryOut, accessToken, accessTokenSecret, body, "application/xml", options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/special/contact
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_special_contactGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/special/contact",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/special/contact
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_special_contactGET(
            String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/special/contact",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/special/contact
    * ACCESSCONTROL   machineapp: machapp_ruleset.machineapp_record_created_rule -- account: full_control
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param body body of http request (data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_special_contactPUT(
            String recordId, String accessToken, String accessTokenSecret, Object body, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/special/contact",
               queryOut, accessToken, accessTokenSecret, body, "application/xml", options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/{document_id}/meta
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_metaGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/" + documentId + "/meta",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/{document_id}/meta
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param carenetId carenetID for sharing
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_X_metaGET(
            String carenetId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/" + documentId + "/meta",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/{document_id}/versions/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_versions_GET(
            String pagingOrderingQuery, String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/" + documentId + "/versions/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/{document_id}/versions/ -- not implemented: indivo_server-v0.8.3.7
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_X_versions_GET(
            String pagingOrderingQuery, String carenetId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/" + documentId + "/versions/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/external/{app_id}/{external_id}/meta
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_external_X_X_metaGET(
            String recordId, String appId, String externalId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/external/" + appId + "/" + externalId + "/meta",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/external/{app_id}/{external_id}/meta -- not implemented: indivo_server-v0.8.3.7
    * @param carenetId carenetID for sharing
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_external_X_X_metaGET(
            String carenetId, String appId, String externalId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/external/" + appId + "/" + externalId + "/meta",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/documents/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.no_external_id -- account: full_control
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_POST(
            String recordId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/documents/",
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/external/{app_id}/{external_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_external_X_XPUT(
            String recordId, String appId, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/external/" + appId + "/" + externalId,
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/{document_id}/label
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_labelPUT(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Object body, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/" + documentId + "/label",
               queryOut, accessToken, accessTokenSecret, body, "text/plain", options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/external/{app_id}/{external_id}/label
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_external_X_X_labelPUT(
            String recordId, String appId, String externalId, String accessToken, String accessTokenSecret, Object body, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/external/" + appId + "/" + externalId + "/label",
               queryOut, accessToken, accessTokenSecret, body, "text/plain", options);
        return fromRequest;
    }


    /** POST /records/{record_id}/documents/{document_id}/replace
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_replacePOST(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/documents/" + documentId + "/replace",
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_replace_external_X_XPUT(
            String recordId, String documentId, String appId, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/" + documentId + "/replace/external/" + appId + "/" + externalId,
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** DELETE /records/{record_id}/documents/{document_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_XDELETE(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "records/" + recordId + "/documents/" + documentId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/documents/{document_id}/set-status
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param reason {reason}
    * @param status void
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_setStatusPOST(
            String reason, String status, String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/documents/" + documentId + "/set-status",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "reason", reason }, { "status", status } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/{document_id}/status-history
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_statusHistoryGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/" + documentId + "/status-history",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/{document_id}/status-history -- not implemented: indivo_server-v0.8.3.7
    * @param carenetId carenetID for sharing
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_X_statusHistoryGET(
            String carenetId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/" + documentId + "/status-history",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/{other_document_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param relType Relationship type, from a fixed list including: interpretation; annotation; followup
    * @param otherDocumentId ID of other document with which relationship is to be established
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_rels_X_XPUT(
            String recordId, String documentId, String relType, String otherDocumentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/" + documentId + "/rels/" + relType + "/" + otherDocumentId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/documents/{document_id}/rels/{rel_type}/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param relType Relationship type, from a fixed list including: interpretation; annotation; followup
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_rels_X_POST(
            String recordId, String documentId, String relType, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/documents/" + documentId + "/rels/" + relType + "/",
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/external/{app_id}/{external_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param relType Relationship type, from a fixed list including: interpretation; annotation; followup
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_rels_X_external_X_XPUT(
            String recordId, String documentId, String relType, String appId, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/" + documentId + "/rels/" + relType + "/external/" + appId + "/" + externalId,
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/{document_id}/rels/{rel_type}/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param relType Relationship type, from a fixed list including: interpretation; annotation; followup
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_rels_X_GET(
            String pagingOrderingQuery, String recordId, String documentId, String relType, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/" + documentId + "/rels/" + relType + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/documents/{document_id}/rels/{rel_type}/ -- not implemented: indivo_server-v0.8.3.7
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param documentId Indivo's ID of the document within the record
    * @param relType Relationship type, from a fixed list including: interpretation; annotation; followup
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_documents_X_rels_X_GET(
            String pagingOrderingQuery, String carenetId, String documentId, String relType, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/documents/" + documentId + "/rels/" + relType + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /accounts/{account_id}/inbox/?include_archive={1|0}
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param include_archive {1|0}
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_inbox_GET(
            String include_archive, String pagingOrderingQuery, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "include_archive", include_archive } });
        if (pagingOrderingQuery != null && pagingOrderingQuery.length() > 0) { queryOut += "&" + pagingOrderingQuery; }
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/" + accountId + "/inbox/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /accounts/{account_id}/inbox/{message_id}
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param accountId Indivo's account ID
    * @param messageId message ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_inbox_XGET(
            String accountId, String messageId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/" + accountId + "/inbox/" + messageId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/inbox/{message_id}/archive -- not implemented: indivo_server-v0.8.3.7
    * @param accountId Indivo's account ID
    * @param messageId message ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_inbox_X_archivePOST(
            String accountId, String messageId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/inbox/" + messageId + "/archive",
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/inbox/{message_id}/attachments/{attachment_num}/accept
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param accountId Indivo's account ID
    * @param messageId message ID
    * @param attachmentNum a 1-indexed integer that represents the order of the attachment
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_inbox_X_attachments_X_acceptPOST(
            String accountId, String messageId, Integer attachmentNum, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/inbox/" + messageId + "/attachments/" + attachmentNum + "/accept",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /accounts/{account_id}/inbox/{message_id} -- not implemented: indivo_server-v0.8.3.7
    * @param subject {subject}
    * @param body {body}
    * @param severity {severity}
    * @param accountId Indivo's account ID
    * @param messageId message ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_inbox_XPUT(
            String subject, String body, String severity, String accountId, String messageId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "accounts/" + accountId + "/inbox/" + messageId,
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "subject", subject }, { "body", body }, { "severity", severity } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/inbox/{message_id} -- not implemented: indivo_server-v0.8.3.7
    * @param subject {subject}
    * @param body {body}
    * @param severity {severity}
    * @param num_attachments {num_attachments}
    * @param recordId Indivo's record ID.
    * @param messageId message ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_inbox_XPUT(
            String subject, String body, String severity, String num_attachments, String recordId, String messageId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/inbox/" + messageId,
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "subject", subject }, { "body", body }, { "severity", severity }, { "num_attachments", num_attachments } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** contrary to Indivo API documentation, in version 0.9.3 this is POST not put.
     * PUT version retained because it is not yet decided whether this should be PUT or POST
     */
    public Object records_X_inbox_XPOST(
            String subject, String body, String severity, String num_attachments, String recordId, String messageId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/inbox/" + messageId,
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "subject", subject }, { "body", body }, { "severity", severity }, { "num_attachments", num_attachments } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/inbox/{message_id}/attachments/{attachment_num} -- not implemented: indivo_server-v0.8.3.7
    * @param recordId Indivo's record ID.
    * @param messageId message ID
    * @param attachmentNum a 1-indexed integer that represents the order of the attachment
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_inbox_X_attachments_XPUT(
            String recordId, String messageId, Integer attachmentNum, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/inbox/" + messageId + "/attachments/" + attachmentNum,
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }

    /** contrary to Indivo API documentation, in version 0.9.3 this is POST not put.
     * PUT version retained because it is not yet decided whether this should be PUT or POST
     */
    public Object records_X_inbox_X_attachments_XPOST(
            String recordId, String messageId, Integer attachmentNum, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/inbox/" + messageId + "/attachments/" + attachmentNum,
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/notify
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: None
    * @param content {notification_content}
    * @param app_url {relative_url}
    * @param document_id {document_id}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_notifyPOST(
            String content, String app_url, String document_id, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/notify",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "content", content }, { "app_url", app_url }, { "document_id", document_id } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/documents/
    * ACCESSCONTROL   userapp: userapp_ruleset.userapp_documents -- machineapp: machapp_ruleset.appspecific_rule -- account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param appId application ID
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_GET(
            String pagingOrderingQuery, String appId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/documents/",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/documents/{document_id}
    * ACCESSCONTROL   userapp: userapp_ruleset.userapp_documents -- account: full_control
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param responseContentType of http response body (expected type to get back, or null).
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_XGET(
            String appId, String documentId, String responseContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/documents/" + documentId,
               queryOut, null, null, responseContentType, options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/documents/{document_id}/meta
    * ACCESSCONTROL   userapp: userapp_ruleset.userapp_documents -- account: full_control
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_X_metaGET(
            String appId, String documentId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/documents/" + documentId + "/meta",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/documents/external/{external_id}/meta
    * ACCESSCONTROL   userapp: userapp_ruleset.userapp_documents -- account: full_control
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_external_X_metaGET(
            String appId, String externalId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/documents/external/" + externalId + "/meta",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** POST /apps/{app_id}/documents/
    * ACCESSCONTROL   userapp: userapp_ruleset.userapp_documents -- account: full_control
    * @param appId application ID
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_POST(
            String appId, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "apps/" + appId + "/documents/",
               queryOut, null, null, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /apps/{app_id}/documents/external/{external_id}
    * ACCESSCONTROL   userapp: userapp_ruleset.userapp_documents -- account: full_control
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_external_XPUT(
            String appId, String externalId, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "apps/" + appId + "/documents/external/" + externalId,
               queryOut, null, null, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /apps/{app_id}/documents/{document_id}/label
    * ACCESSCONTROL   account: full_control
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param body body of http request (data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_documents_X_labelPUT(
            String appId, String documentId, Object body, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "apps/" + appId + "/documents/" + documentId + "/label",
               queryOut, null, null, body, "text/plain", options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/inbox/ -- not implemented: indivo_server-v0.8.3.7
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param appId application ID
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_inbox_GET(
            String pagingOrderingQuery, String appId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/inbox/",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/inbox/{message_id} -- not implemented: indivo_server-v0.8.3.7
    * @param appId application ID
    * @param messageId message ID
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_inbox_XGET(
            String appId, String messageId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/inbox/" + messageId,
               queryOut, null, null, options);
        return fromRequest;
    }


    /** GET /apps/{app_id}/inbox/{message_id}/attachments/{attachment_num} -- not implemented: indivo_server-v0.8.3.7
    * @param appId application ID
    * @param messageId message ID
    * @param attachmentNum a 1-indexed integer that represents the order of the attachment
    * @param responseContentType of http response body (expected type to get back, or null).
    * @param options see <strong>options</strong> above.
    */
    public Object apps_X_inbox_X_attachments_XGET(
            String appId, String messageId, Integer attachmentNum, String responseContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "apps/" + appId + "/inbox/" + messageId + "/attachments/" + attachmentNum,
               queryOut, null, null, responseContentType, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/apps/{app_id}/documents/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_GET(
            String pagingOrderingQuery, String recordId, String appId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/apps/" + appId + "/documents/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/apps/{app_id}/documents/{document_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param responseContentType of http response body (expected type to get back, or null).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_XGET(
            String recordId, String appId, String documentId, String accessToken, String accessTokenSecret, String responseContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/apps/" + appId + "/documents/" + documentId,
               queryOut, accessToken, accessTokenSecret, responseContentType, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/apps/{app_id}/documents/{document_id}/meta
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_X_metaGET(
            String recordId, String appId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/apps/" + appId + "/documents/" + documentId + "/meta",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_external_X_metaGET(
            String recordId, String appId, String externalId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/apps/" + appId + "/documents/external/" + externalId + "/meta",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/apps/{app_id}/documents/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_POST(
            String recordId, String appId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/apps/" + appId + "/documents/",
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/apps/{app_id}/documents/external/{external_id}
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_external_XPUT(
            String recordId, String appId, String externalId, String accessToken, String accessTokenSecret, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/apps/" + appId + "/documents/external/" + externalId,
               queryOut, accessToken, accessTokenSecret, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/apps/{app_id}/documents/{document_id}/label
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param body body of http request (data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_X_labelPUT(
            String recordId, String appId, String documentId, String accessToken, String accessTokenSecret, Object body, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/apps/" + appId + "/documents/" + documentId + "/label",
               queryOut, accessToken, accessTokenSecret, body, "text/plain", options);
        return fromRequest;
    }


    /** DELETE /records/{record_id}/apps/{app_id}/documents/{document_id} -- not implemented: indivo_server-v0.8.3.7
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_documents_XDELETE(
            String recordId, String appId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "records/" + recordId + "/apps/" + appId + "/documents/" + documentId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/reports/minimal/measurements/{lab_code}/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param labCode lab code ('HBA1C' is one example)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_reports_minimal_measurements_X_GET(
            String pagingOrderingQuery, String recordId, String labCode, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/reports/minimal/measurements/" + labCode + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/reports/minimal/measurements/{lab_code}/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param labCode lab code ('HBA1C' is one example)
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_reports_minimal_measurements_X_GET(
            String pagingOrderingQuery, String carenetId, String labCode, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/reports/minimal/measurements/" + labCode + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/reports/minimal/medications/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param typeOfMinimalEgMedications Over time, new reports may be introduced. For now:
    *       medications; allergies; equipment; immunizations; procedures;
    *       problems; vitals; labs; simple-clinical-notes
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_reports_minimal_X_GET(
            String pagingOrderingQuery, String recordId, String typeOfMinimalEgMedications, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/reports/minimal/" + typeOfMinimalEgMedications + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/reports/minimal/medications/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param typeOfMinimalEgMedications Over time, new reports may be introduced. For now:
    *       medications; allergies; equipment; immunizations; procedures;
    *       problems; vitals; labs; simple-clinical-notes
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_reports_minimal_X_GET(
            String pagingOrderingQuery, String carenetId, String typeOfMinimalEgMedications, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/reports/minimal/" + typeOfMinimalEgMedications + "/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /codes/systems/
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object codes_systems_GET(
            String pagingOrderingQuery, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "codes/systems/",
               queryOut, accessToken, accessTokenSecret, "application/json", options);
        return fromRequest;
    }


    /** GET /codes/systems/{short_name}/query?q={query}
    * @param q {query}
    * @param shortName coding system short name, example: 'umls-snomed'
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object codes_systems_X_queryGET(
            String q, String shortName, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "q", q } });
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "codes/systems/" + shortName + "/query",
               queryOut, accessToken, accessTokenSecret, "application/json", options);
        return fromRequest;
    }


    /** GET /accounts/{account_id}
    * ACCESSCONTROL   machineapp: None -- account: account_ruleset.account_rule
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_XGET(
            String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/" + accountId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /accounts/search?fullname={fullname}&contact_email={contact_email}
    * ACCESSCONTROL   machineapp: None
    * @param fullname {fullname}
    * @param contact_email {contact_email}
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_searchGET(
            String fullname, String contact_email, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "fullname", fullname }, { "contact_email", contact_email } });
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/search",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** POST /accounts/
    * ACCESSCONTROL   machineapp: None
    * @param account_id {account_id}
    * @param contact_email {contact_email}
    * @param full_name {full_name}
    * @param primary_secret_p {0|1}
    * @param secondary_secret_p {0|1}
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_POST(
            String account_id, String contact_email, String full_name, String primary_secret_p, String secondary_secret_p, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/",
               queryOut, null, null, buildFormURLEnc(new String[][] {{ "account_id", account_id }, { "contact_email", contact_email }, { "full_name", full_name }, { "primary_secret_p", primary_secret_p }, { "secondary_secret_p", secondary_secret_p } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/authsystems/
    * ACCESSCONTROL   machineapp: None
    * @param system password
    * @param username {username}
    * @param password {password}
    * @param accountId Indivo's account ID
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_authsystems_POST(
            String system, String username, String password, String accountId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/authsystems/",
               queryOut, null, null, buildFormURLEnc(new String[][] {{ "system", system }, { "username", username }, { "password", password } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/secret-resend
    * ACCESSCONTROL   machineapp: None
    * @param accountId Indivo's account ID
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_secretResendPOST(
            String accountId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/secret-resend",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/reset
    * ACCESSCONTROL   machineapp: None
    * @param accountId Indivo's account ID
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_resetPOST(
            String accountId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/reset",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/initialize -- not implemented: indivo_server-v0.8.3.7
    * @param primary_secret {primary_secret}
    * @param secondary_secret {secondary_secret}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_initializePOST(
            String primary_secret, String secondary_secret, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/initialize",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "primary_secret", primary_secret }, { "secondary_secret", secondary_secret } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/set-state
    * ACCESSCONTROL   machineapp: None
    * @param state {new_state}
    * @param accountId Indivo's account ID
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_setStatePOST(
            String state, String accountId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/set-state",
               queryOut, null, null, buildFormURLEnc(new String[][] {{ "state", state } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/info-set
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param full_name {full_name}
    * @param contact_email {contact_email}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_infoSetPOST(
            String full_name, String contact_email, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/info-set",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "full_name", full_name }, { "contact_email", contact_email } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/authsystems/password/set-username
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param username {username}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_authsystems_password_setUsernamePOST(
            String username, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/authsystems/password/set-username",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "username", username } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/authsystems/password/set
    * ACCESSCONTROL   machineapp: None
    * @param password {password}
    * @param accountId Indivo's account ID
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_authsystems_password_setPOST(
            String password, String accountId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/authsystems/password/set",
               queryOut, null, null, buildFormURLEnc(new String[][] {{ "password", password } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/authsystems/password/change
    * ACCESSCONTROL   account: account_ruleset.account_rule
    * @param old {old_password]
    * @param newnew {new_password}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_authsystems_password_changePOST(
            String old, String newnew, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/authsystems/password/change",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "old", old }, { "new", newnew } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** GET /accounts/{account_id}/primary-secret
    * ACCESSCONTROL   machineapp: None
    * @param accountId Indivo's account ID
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_primarySecretGET(
            String accountId, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/" + accountId + "/primary-secret",
               queryOut, null, null, options);
        return fromRequest;
    }


    /** POST /records/
    * ACCESSCONTROL   machineapp: machapp_ruleset.principal_email_matches_principal
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_POST(
            Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/",
               queryOut, null, null, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/external/{app_id}/{external_id}
    * ACCESSCONTROL   machineapp: machapp_ruleset.principal_email_matches_principal
    * @param appId application ID
    * @param externalId external ID (scoped within appID)
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_external_X_XPUT(
            String appId, String externalId, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/external/" + appId + "/" + externalId,
               queryOut, null, null, body, requestContentType, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/owner
    * ACCESSCONTROL   machineapp: None
    * @param recordId Indivo's record ID.
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_ownerPUT(
            String recordId, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/owner",
               queryOut, null, null, body, requestContentType, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/apps/{app_id}/setup
    * ACCESSCONTROL   machineapp: None
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param body body of http request (data to send).
    * @param requestContentType of http request body (type of data to send).
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_X_setupPOST(
            String recordId, String appId, Object body, String requestContentType, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/apps/" + appId + "/setup",
               queryOut, null, null, body, requestContentType, new String[]{"text/plain", "application/x-www-form-urlencoded"}, options);
        return fromRequest;
    }


    /** DELETE /records/{record_id}/apps/{app_id}
    * ACCESSCONTROL   machineapp: None -- account: full_control
    * @param recordId Indivo's record ID.
    * @param appId application ID
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_apps_XDELETE(
            String recordId, String appId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "records/" + recordId + "/apps/" + appId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /oauth/internal/session_create
    * @param username {username}
    * @param password {password}
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object oauth_internal_session_createPOST(
            String username, String password, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "oauth/internal/session_create",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "username", username }, { "password", password } }), "application/x-www-form-urlencoded", new String[]{"text/plain", "application/x-www-form-urlencoded"}, options);
        return fromRequest;
    }


    /** POST /oauth/internal/request_tokens/{request_token}/claim
    * ACCESSCONTROL   account: None
    * @param requestToken oauth request token
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object oauth_internal_request_tokens_X_claimPOST(
            String requestToken, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "oauth/internal/request_tokens/" + requestToken + "/claim",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /oauth/internal/request_tokens/{request_token}/info
    * ACCESSCONTROL   account: account_ruleset.reqtoken_exists
    * @param requestToken oauth request token
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object oauth_internal_request_tokens_X_infoGET(
            String requestToken, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "oauth/internal/request_tokens/" + requestToken + "/info",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /oauth/internal/request_tokens/{request_token}/approve
    * ACCESSCONTROL   account: account_ruleset.reqtoken_record, account_ruleset.reqtoken_carenet
    * @param record_id {indivo_record_id}
    * @param requestToken oauth request token
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object oauth_internal_request_tokens_X_approvePOST(
            String record_id, String requestToken, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "oauth/internal/request_tokens/" + requestToken + "/approve",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "record_id", record_id } }), "application/x-www-form-urlencoded", new String[]{"text/plain", "application/x-www-form-urlencoded"}, options);
        return fromRequest;
    }


    /** GET /accounts/{account_id}/check-primary-secret/{primary_secret} -- not implemented: indivo_server-v0.8.3.7
    * @param accountId Indivo's account ID
    * @param primarySecret oauth primary secret
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_checkPrimarySecret_XGET(
            String accountId, String primarySecret, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "accounts/" + accountId + "/check-primary-secret/" + primarySecret,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /accounts/{account_id}/initialize -- not implemented: indivo_server-v0.8.3.7
    * @param primary_secret {primary_secret]
    * @param secondary_secret {secondary_secret}
    * @param password {password}
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object accounts_X_initializePOST(
            String primary_secret, String secondary_secret, String password, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "accounts/" + accountId + "/initialize",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "primary_secret", primary_secret }, { "secondary_secret", secondary_secret }, { "password", password } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** GET /oauth/internal/surl-verify?url={url}
    * ACCESSCONTROL   account: None
    * @param url {url}
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object oauth_internal_surlVerifyGET(
            String url, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "url", url } });
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "oauth/internal/surl-verify",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/carenets/
    * ACCESSCONTROL   machineapp: None -- account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_carenets_GET(
            String pagingOrderingQuery, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/carenets/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/record
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_recordGET(
            String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/record",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/shares/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_shares_GET(
            String pagingOrderingQuery, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/shares/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/shares/
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
    * @param account_id {account_id}
    * @param role_label {role_label}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_shares_POST(
            String account_id, String role_label, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/shares/",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "account_id", account_id }, { "role_label", role_label } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /records/{record_id}/shares/{account_id}/delete
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
    * @param recordId Indivo's record ID.
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_shares_X_deletePOST(
            String recordId, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/shares/" + accountId + "/delete",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/{document_id}/carenets/{carenet_id}
    * ACCESSCONTROL   account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_carenets_XPUT(
            String recordId, String documentId, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/" + documentId + "/carenets/" + carenetId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** DELETE /records/{record_id}/documents/{document_id}/carenets/{carenet_id}
    * ACCESSCONTROL   account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_carenets_XDELETE(
            String recordId, String documentId, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "records/" + recordId + "/documents/" + documentId + "/carenets/" + carenetId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/documents/{document_id}/carenets/{carenet_id}/autoshare-revert
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_carenets_X_autoshareRevertPOST(
            String recordId, String documentId, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/documents/" + documentId + "/carenets/" + carenetId + "/autoshare-revert",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/documents/{document_id}/carenets/
    * ACCESSCONTROL   account: full_control
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_carenets_GET(
            String pagingOrderingQuery, String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/documents/" + documentId + "/carenets/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /records/{record_id}/documents/{document_id}/nevershare
    * ACCESSCONTROL   account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_neversharePUT(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "records/" + recordId + "/documents/" + documentId + "/nevershare",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** DELETE /records/{record_id}/documents/{document_id}/nevershare
    * ACCESSCONTROL   account: full_control
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_documents_X_nevershareDELETE(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "records/" + recordId + "/documents/" + documentId + "/nevershare",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/autoshare/bytype/?type={indivo_document_type}
    * ACCESSCONTROL   account: full_control
    * @param type {indivo_document_type}
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_autoshare_bytype_GET(
            String type, String pagingOrderingQuery, String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = buildFormURLEnc(new String[][] {{ "type", type } });
        if (pagingOrderingQuery != null && pagingOrderingQuery.length() > 0) { queryOut += "&" + pagingOrderingQuery; }
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/autoshare/bytype/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/autoshare/bytype/all
    * ACCESSCONTROL   account: full_control
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_autoshare_bytype_allGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/autoshare/bytype/all",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set
    * ACCESSCONTROL   machineapp: None -- account: full_control
    * @param type {indivo_document_type}
    * @param recordId Indivo's record ID.
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.  null if from admin app.
    * @param accessTokenSecret OAuth secret.  null if from admin app.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_autoshare_carenets_X_bytype_setPOST(
            String type, String recordId, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/autoshare/carenets/" + carenetId + "/bytype/set",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "type", type } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/unset
    * ACCESSCONTROL   account: full_control
    * @param type {indivo_document_type}
    * @param recordId Indivo's record ID.
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_autoshare_carenets_X_bytype_unsetPOST(
            String type, String recordId, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "records/" + recordId + "/autoshare/carenets/" + carenetId + "/bytype/unset",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "type", type } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/apps/
    * ACCESSCONTROL   account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_apps_GET(
            String pagingOrderingQuery, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/apps/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** PUT /carenets/{carenet_id}/apps/{pha_email}
    * ACCESSCONTROL   account: full_control
    * @param carenetId carenetID for sharing
    * @param phaEmail application ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_apps_XPUT(
            String carenetId, String phaEmail, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "PUT", "carenets/" + carenetId + "/apps/" + phaEmail,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** DELETE /carenets/{carenet_id}/apps/{pha_email}
    * ACCESSCONTROL   account: full_control
    * @param carenetId carenetID for sharing
    * @param phaEmail application ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_apps_XDELETE(
            String carenetId, String phaEmail, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "carenets/" + carenetId + "/apps/" + phaEmail,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/accounts/
    * ACCESSCONTROL   account: accessrule_carenet_account
    * @param pagingOrderingQuery offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_accounts_GET(
            String pagingOrderingQuery, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut = pagingOrderingQuery;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/accounts/",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** POST /carenets/{carenet_id}/accounts/
    * ACCESSCONTROL   account: full_control
    * @param account_id {account_id}
    * @param write {false|true}
    * @param carenetId carenetID for sharing
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_accounts_POST(
            String account_id, String write, String carenetId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "POST", "carenets/" + carenetId + "/accounts/",
               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "account_id", account_id }, { "write", write } }), "application/x-www-form-urlencoded", options);
        return fromRequest;
    }


    /** DELETE /carenets/{carenet_id}/accounts/{account_id}
    * ACCESSCONTROL   account: full_control
    * @param carenetId carenetID for sharing
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_accounts_XDELETE(
            String carenetId, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "DELETE", "carenets/" + carenetId + "/accounts/" + accountId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /carenets/{carenet_id}/accounts/{account_id}/permissions
    * ACCESSCONTROL   accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
    * @param carenetId carenetID for sharing
    * @param accountId Indivo's account ID
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object carenets_X_accounts_X_permissionsGET(
            String carenetId, String accountId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "carenets/" + carenetId + "/accounts/" + accountId + "/permissions",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/audits -- not implemented: indivo_server-v0.8.3.7
    * @param recordId Indivo's record ID.
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_auditsGET(
            String recordId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/audits",
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/audits/documents/{document_id} -- not implemented: indivo_server-v0.8.3.7
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_audits_documents_XGET(
            String recordId, String documentId, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/audits/documents/" + documentId,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }


    /** GET /records/{record_id}/audits/documents/{document_id}/functions/{function_name} -- not implemented: indivo_server-v0.8.3.7
    * @param recordId Indivo's record ID.
    * @param documentId Indivo's ID of the document within the record
    * @param functionName name of audited function on which to get report
    * @param accessToken OAuth token.
    * @param accessTokenSecret OAuth secret.
    * @param options see <strong>options</strong> above.
    */
    public Object records_X_audits_documents_X_functions_XGET(
            String recordId, String documentId, String functionName, String accessToken, String accessTokenSecret, Map<String,Object> options)
            throws IndivoClientException {
        String queryOut =  null;
        Object fromRequest = null;
        fromRequest = clientRequest(
               "GET", "records/" + recordId + "/audits/documents/" + documentId + "/functions/" + functionName,
               queryOut, accessToken, accessTokenSecret, options);
        return fromRequest;
    }
    /***END AUTO GENERATED FROM WIKI*/



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
    public List apps_X_documents_GET_HL(String pagingOrderingQuery, String appId, Map<String,Object> options)
        throws IndivoClientException {
        Map<String,Object> options0 = null;
        if (options == null) { options0 = new HashMap<String,Object>(); }
        else { options0 = new HashMap(options); }
        // make sure DOM object returned
        options0.put("responseTypeConversion", internalResponseTypeConversion);

        List retVal = new ArrayList();
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
