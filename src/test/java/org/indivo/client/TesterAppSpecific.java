package org.indivo.client;

import java.util.Map;
import org.w3c.dom.Document;

class TesterAppSpecific {

    private Document testResultDoc = null;

    private Rest client = null;

    private String documentId = null;
    private String recordId = null;
    private String carenetId = null;
    private String appId = null;
    private String accountId = null;
    private String externalId = null;
    private String pagingOrderingQuery = null;
    private String accessToken = null;
    private String accessTokenSecret = null;
    private String type = null;
    private String body = null;
    private String requestContentType = null;
    private String responseContentType = null;

    private Map<String,Object> options = null;

    private Map<String,String> testResultForm = null;
    private Object testResultObj = null;

    private String domToString(Document doc) {
        return null;
    }
    private String printForm(Map<String,String> fromFromIndivo) {
        return null;
    }

    private void hasTestCode() throws org.indivo.client.IndivoClientException {

// userapp: userapp_ruleset.userapp_documents -- machineapp: machapp_ruleset.appspecific_rule -- account: full_control
// GET /apps/{app_id}/documents/
//        testResultDoc = (Document) client.apps_X_documents_GET(pagingOrderingQuery, appId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// GET /apps/{app_id}/documents/{document_id}
//        testResultObj = client.apps_X_documents_XGET(appId, documentId, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// GET /apps/{app_id}/documents/{document_id}/meta
//        testResultDoc = (Document) client.apps_X_documents_X_metaGET(appId, documentId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// GET /apps/{app_id}/documents/external/{external_id}/meta
//        testResultDoc = (Document) client.apps_X_documents_external_X_metaGET(appId, externalId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// POST /apps/{app_id}/documents/
//        testResultDoc = (Document) client.apps_X_documents_POST(appId, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// PUT /apps/{app_id}/documents/external/{external_id}
//        testResultDoc = (Document) client.apps_X_documents_external_XPUT(appId, externalId, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");


    }
}
