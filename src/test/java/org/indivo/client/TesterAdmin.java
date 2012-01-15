package org.indivo.client;

import java.util.Map;
import org.w3c.dom.Document;

class TesterAdmin {

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
    private Map<String,Object> options = null;

    private Map<String,String> testResultForm = null;

    String contactDoc =
    "<Contact  xmlns=\"Contact\">"
        + "<name><fullname>nameForTestDocument</fullname><givenName/><familyName/></name>"
        + "<email type=\"work\">nathan.finstein@childrens.harvard.edu</email>"
        + "<address type=\"home\"><streetAddress>1 one way</streetAddress>"
        + "<postalCode>12345</postalCode>"
        + "<locality>ames</locality>"
        + "<region>Alaska</region>"
        + "<country>USA</country></address>"
        + "<location type=\"home\"><latitude>90</latitude><longitude>90</longitude></location>"
        + "</Contact>";


    private String content = null;
    private String app_url = null;
    private String document_id = null;
    private String fullname = null;
    private String contact_email = null;
    private String account_id = null;
    private String full_name = null;
    private String primary_secret_p = null;
    private String secondary_secret_p = null;
    private String system = null;
    private String username = null;
    private String password = null;
    private String state = null;
    private String role_label = null;

    private String domToString(Document doc) {
        return null;
    }
    private String printForm(Map<String,String> fromFromIndivo) {
        return null;
    }

    private void hasTestCode() throws org.indivo.client.IndivoClientException {

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /records/{record_id}/documents/special/demographics
//    System.out.println("records_X_documents_special_demographicsGET\n" +
//        "GET /records/{record_id}/documents/special/demographics");
//        testResultDoc = (Document) client.records_X_documents_special_demographicsGET(recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/documents/special/demographics
//    System.out.println("carenets_X_documents_special_demographicsGET\n" +
//        "GET /carenets/{carenet_id}/documents/special/demographics");
//        testResultDoc = (Document) client.carenets_X_documents_special_demographicsGET(carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: machapp_ruleset.machineapp_record_created_rule -- account: full_control
// PUT /records/{record_id}/documents/special/demographics
//    System.out.println("records_X_documents_special_demographicsPUT\n" +
//        "PUT /records/{record_id}/documents/special/demographics");
//        testResultDoc = (Document) client.records_X_documents_special_demographicsPUT(recordId, accessToken, accessTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /records/{record_id}/documents/special/contact
//    System.out.println("records_X_documents_special_contactGET\n" +
//        "GET /records/{record_id}/documents/special/contact");
//        testResultDoc = (Document) client.records_X_documents_special_contactGET(recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/documents/special/contact
//    System.out.println("carenets_X_documents_special_contactGET\n" +
//        "GET /carenets/{carenet_id}/documents/special/contact");
//        testResultDoc = (Document) client.carenets_X_documents_special_contactGET(carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: machapp_ruleset.machineapp_record_created_rule -- account: full_control
// PUT /records/{record_id}/documents/special/contact
//    System.out.println("records_X_documents_special_contactPUT\n" +
//        "PUT /records/{record_id}/documents/special/contact");
//        testResultDoc = (Document) client.records_X_documents_special_contactPUT(recordId, accessToken, accessTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.no_external_id -- account: full_control
// POST /records/{record_id}/documents/
//    System.out.println("records_X_documents_POST\n" +
//        "POST /records/{record_id}/documents/");
//        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
// POST /records/{record_id}/documents/{document_id}/replace
//    System.out.println("records_X_documents_X_replacePOST\n" +
//        "POST /records/{record_id}/documents/{document_id}/replace");
//        testResultDoc = (Document) client.records_X_documents_X_replacePOST(recordId, documentId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
// PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}
//    System.out.println("records_X_documents_X_replace_external_X_XPUT\n" +
//        "PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}");
//        testResultDoc = (Document) client.records_X_documents_X_replace_external_X_XPUT(recordId, documentId, appId, externalId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None
// POST /records/{record_id}/notify
//    System.out.println("records_X_notifyPOST\n" +
//        "POST /records/{record_id}/notify");
//        testResultDoc = (Document) client.records_X_notifyPOST(content, app_url, document_id, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- machineapp: machapp_ruleset.appspecific_rule -- account: full_control
// GET /apps/{app_id}/documents/
//    System.out.println("apps_X_documents_GET\n" +
//        "GET /apps/{app_id}/documents/");
//        testResultDoc = (Document) client.apps_X_documents_GET(pagingOrderingQuery, appId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: account_ruleset.account_rule
// GET /accounts/{account_id}
//    System.out.println("accounts_XGET\n" +
//        "GET /accounts/{account_id}");
//        testResultDoc = (Document) client.accounts_XGET(accountId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// GET /accounts/search?fullname={fullname}&contact_email={contact_email}
//    System.out.println("accounts_searchGET\n" +
//        "GET /accounts/search?fullname={fullname}&contact_email={contact_email}");
//        testResultDoc = (Document) client.accounts_searchGET(fullname, contact_email, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /accounts/
//    System.out.println("accounts_POST\n" +
//        "POST /accounts/");
//        testResultDoc = (Document) client.accounts_POST(account_id, contact_email, full_name, primary_secret_p, secondary_secret_p, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /accounts/{account_id}/authsystems/
//    System.out.println("accounts_X_authsystems_POST\n" +
//        "POST /accounts/{account_id}/authsystems/");
//        testResultDoc = (Document) client.accounts_X_authsystems_POST(system, username, password, accountId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /accounts/{account_id}/secret-resend
//    System.out.println("accounts_X_secretResendPOST\n" +
//        "POST /accounts/{account_id}/secret-resend");
//        testResultDoc = (Document) client.accounts_X_secretResendPOST(accountId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /accounts/{account_id}/reset
//    System.out.println("accounts_X_resetPOST\n" +
//        "POST /accounts/{account_id}/reset");
//        testResultDoc = (Document) client.accounts_X_resetPOST(accountId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /accounts/{account_id}/set-state
//    System.out.println("accounts_X_setStatePOST\n" +
//        "POST /accounts/{account_id}/set-state");
//        testResultDoc = (Document) client.accounts_X_setStatePOST(state, accountId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /accounts/{account_id}/authsystems/password/set
//    System.out.println("accounts_X_authsystems_password_setPOST\n" +
//        "POST /accounts/{account_id}/authsystems/password/set");
//        testResultDoc = (Document) client.accounts_X_authsystems_password_setPOST(password, accountId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// GET /accounts/{account_id}/primary-secret
//    System.out.println("accounts_X_primarySecretGET\n" +
//        "GET /accounts/{account_id}/primary-secret");
//        testResultDoc = (Document) client.accounts_X_primarySecretGET(accountId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: machapp_ruleset.principal_email_matches_principal
// POST /records/
//    System.out.println("records_POST\n" +
//        "POST /records/");
//        testResultDoc = (Document) client.records_POST(body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: machapp_ruleset.principal_email_matches_principal
// PUT /records/external/{app_id}/{external_id}
//    System.out.println("records_external_X_XPUT\n" +
//        "PUT /records/external/{app_id}/{external_id}");
//        testResultDoc = (Document) client.records_external_X_XPUT(appId, externalId, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// PUT /records/{record_id}/owner
//    System.out.println("records_X_ownerPUT\n" +
//        "PUT /records/{record_id}/owner");
//        testResultDoc = (Document) client.records_X_ownerPUT(recordId, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None
// POST /records/{record_id}/apps/{app_id}/setup
//    System.out.println("records_X_apps_X_setupPOST\n" +
//        "POST /records/{record_id}/apps/{app_id}/setup");
//        testResultForm = (Map<String,String>) client.records_X_apps_X_setupPOST(recordId, appId, body, requestContentType, options);
//        System.out.println(printForm(testResultForm) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: full_control
// DELETE /records/{record_id}/apps/{app_id}
//    System.out.println("records_X_apps_XDELETE\n" +
//        "DELETE /records/{record_id}/apps/{app_id}");
//        testResultDoc = (Document) client.records_X_apps_XDELETE(recordId, appId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: full_control
// GET /records/{record_id}/carenets/
//    System.out.println("records_X_carenets_GET\n" +
//        "GET /records/{record_id}/carenets/");
//        testResultDoc = (Document) client.records_X_carenets_GET(pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
// GET /records/{record_id}/shares/
//    System.out.println("records_X_shares_GET\n" +
//        "GET /records/{record_id}/shares/");
//        testResultDoc = (Document) client.records_X_shares_GET(pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
// POST /records/{record_id}/shares/
//    System.out.println("records_X_shares_POST\n" +
//        "POST /records/{record_id}/shares/");
//        testResultDoc = (Document) client.records_X_shares_POST(account_id, role_label, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
// POST /records/{record_id}/shares/{account_id}/delete
//    System.out.println("records_X_shares_X_deletePOST\n" +
//        "POST /records/{record_id}/shares/{account_id}/delete");
//        testResultDoc = (Document) client.records_X_shares_X_deletePOST(recordId, accountId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: full_control
// POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set
//    System.out.println("records_X_autoshare_carenets_X_bytype_setPOST\n" +
//        "POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set");
//        testResultDoc = (Document) client.records_X_autoshare_carenets_X_bytype_setPOST(type, recordId, carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");


    }
}
