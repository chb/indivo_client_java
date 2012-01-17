package org.indivo.client;

import java.util.Map;
import org.w3c.dom.Document;

class TesterChrome {

    private Document testResultDoc = null;

    private Rest chrome = null;

    private String documentId = null;
    private String recordId = null;
    private String carenetId = null;
    private String appId = null;
    private String accountId = null;
    private String externalId = null;
    private String pagingOrderingQuery = null;
    private String sessionToken = null;
    private String sessionTokenSecret = null;
    private String type = null;
    private String body = null;
    private String requestContentType = null;
    private String responseContentType = null;

    private Map<String,Object> options = null;

    private String reason = null;
    private String status = null;
    private String relType = null;
    private String otherDocumentId = null;
    private String messageId = null;
    private String labCode = null;
    private String typeOfMinimalEgMedications = null;
    private String old = null;
    private String newnew = null;
    private String requestToken = null;
    private String record_id = null;
    private String url = null;
    private String account_id = null;
    private String role_label = null;
    private String phaEmail = null;
    private String write = null;
 
    private Integer attachmentNum = null;

    private Map<String,String> testResultForm = null;
    private Object testResultObj = null;

    private String domToString(Document doc) {
        return null;
    }
    private String printForm(Map<String,String> fromFromIndivo) {
        return null;
    }

    private void hasTestCode() throws org.indivo.client.IndivoClientException {

// account: account_ruleset.account_rule
// GET /accounts/{account_id}/records/
//        testResultDoc = (Document) chrome.accounts_X_records_GET(pagingOrderingQuery, accountId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}
//        testResultDoc = (Document) chrome.records_XGET(recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// account: full_control
// GET /records/{record_id}/apps/
//        testResultDoc = (Document) chrome.records_X_apps_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/
//        testResultDoc = (Document) chrome.records_X_documents_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/documents/
//        testResultDoc = (Document) chrome.carenets_X_documents_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/?type={type_url}
//        testResultDoc = (Document) chrome.records_X_documents_GET(type, pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/documents/?type={type_url}
//        testResultDoc = (Document) chrome.carenets_X_documents_GET(type, pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/{document_id}
//        testResultObj = chrome.records_X_documents_XGET(recordId, documentId, sessionToken, sessionTokenSecret, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_document
// GET /carenets/{carenet_id}/documents/{document_id}
//        testResultObj = chrome.carenets_X_documents_XGET(carenetId, documentId, sessionToken, sessionTokenSecret, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /records/{record_id}/documents/special/demographics
//        testResultDoc = (Document) chrome.records_X_documents_special_demographicsGET(recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/documents/special/demographics
//        testResultDoc = (Document) chrome.carenets_X_documents_special_demographicsGET(carenetId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: machapp_ruleset.machineapp_record_created_rule -- account: full_control
// PUT /records/{record_id}/documents/special/demographics
//        testResultDoc = (Document) chrome.records_X_documents_special_demographicsPUT(recordId, sessionToken, sessionTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /records/{record_id}/documents/special/contact
//        testResultDoc = (Document) chrome.records_X_documents_special_contactGET(recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/documents/special/contact
//        testResultDoc = (Document) chrome.carenets_X_documents_special_contactGET(carenetId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// machineapp: machapp_ruleset.machineapp_record_created_rule -- account: full_control
// PUT /records/{record_id}/documents/special/contact
//        testResultDoc = (Document) chrome.records_X_documents_special_contactPUT(recordId, sessionToken, sessionTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/{document_id}/meta
//        testResultDoc = (Document) chrome.records_X_documents_X_metaGET(recordId, documentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /carenets/{carenet_id}/documents/{document_id}/meta
//        testResultDoc = (Document) chrome.carenets_X_documents_X_metaGET(carenetId, documentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/{document_id}/versions/
//        testResultDoc = (Document) chrome.records_X_documents_X_versions_GET(pagingOrderingQuery, recordId, documentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/external/{app_id}/{external_id}/meta
//        testResultDoc = (Document) chrome.records_X_documents_external_X_X_metaGET(recordId, appId, externalId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.no_external_id -- account: full_control
// POST /records/{record_id}/documents/
//        testResultDoc = (Document) chrome.records_X_documents_POST(recordId, sessionToken, sessionTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/documents/external/{app_id}/{external_id}
//        testResultDoc = (Document) chrome.records_X_documents_external_X_XPUT(recordId, appId, externalId, sessionToken, sessionTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/documents/{document_id}/label
//        testResultDoc = (Document) chrome.records_X_documents_X_labelPUT(recordId, documentId, sessionToken, sessionTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/documents/external/{app_id}/{external_id}/label
        testResultDoc = (Document) chrome.records_X_documents_external_X_X_labelPUT(recordId, appId, externalId, sessionToken, sessionTokenSecret, body, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
// POST /records/{record_id}/documents/{document_id}/replace
//        testResultDoc = (Document) chrome.records_X_documents_X_replacePOST(recordId, documentId, sessionToken, sessionTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
// PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}
//        testResultDoc = (Document) chrome.records_X_documents_X_replace_external_X_XPUT(recordId, documentId, appId, externalId, sessionToken, sessionTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// DELETE /records/{record_id}/documents/{document_id}
//        testResultDoc = (Document) chrome.records_X_documents_XDELETE(recordId, documentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// POST /records/{record_id}/documents/{document_id}/set-status
//        testResultDoc = (Document) chrome.records_X_documents_X_setStatusPOST(reason, status, recordId, documentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/documents/{document_id}/status-history
//        testResultDoc = (Document) chrome.records_X_documents_X_statusHistoryGET(recordId, documentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/{other_document_id}
//        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_XPUT(recordId, documentId, relType, otherDocumentId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// POST /records/{record_id}/documents/{document_id}/rels/{rel_type}/
//        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_POST(recordId, documentId, relType, sessionToken, sessionTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/external/{app_id}/{external_id}
        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_external_X_XPUT(recordId, documentId, relType, appId, externalId, sessionToken, sessionTokenSecret, body, requestContentType, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: account_ruleset.account_rule
// GET /accounts/{account_id}/inbox/
//        testResultDoc = (Document) chrome.accounts_X_inbox_GET(pagingOrderingQuery, accountId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// account: account_ruleset.account_rule
// GET /accounts/{account_id}/inbox/{message_id}
        testResultDoc = (Document) chrome.accounts_X_inbox_XGET(accountId, messageId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: account_ruleset.account_rule
// POST /accounts/{account_id}/inbox/{message_id}/attachments/{attachment_num}/accept
        testResultDoc = (Document) chrome.accounts_X_inbox_X_attachments_X_acceptPOST(accountId, messageId, attachmentNum, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- machineapp: machapp_ruleset.appspecific_rule -- account: full_control
// GET /apps/{app_id}/documents/
//        testResultDoc = (Document) chrome.apps_X_documents_GET(pagingOrderingQuery, appId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// GET /apps/{app_id}/documents/{document_id}
//        testResultObj = chrome.apps_X_documents_XGET(appId, documentId, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// GET /apps/{app_id}/documents/{document_id}/meta
//        testResultDoc = (Document) chrome.apps_X_documents_X_metaGET(appId, documentId, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// GET /apps/{app_id}/documents/external/{external_id}/meta
        testResultDoc = (Document) chrome.apps_X_documents_external_X_metaGET(appId, externalId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// POST /apps/{app_id}/documents/
//        testResultDoc = (Document) chrome.apps_X_documents_POST(appId, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// userapp: userapp_ruleset.userapp_documents -- account: full_control
// PUT /apps/{app_id}/documents/external/{external_id}
        testResultDoc = (Document) chrome.apps_X_documents_external_XPUT(appId, externalId, body, requestContentType, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// PUT /apps/{app_id}/documents/{document_id}/label
//        testResultDoc = (Document) chrome.apps_X_documents_X_labelPUT(appId, documentId, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/apps/{app_id}/documents/
        testResultDoc = (Document) chrome.records_X_apps_X_documents_GET(pagingOrderingQuery, recordId, appId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/apps/{app_id}/documents/{document_id}
        testResultObj = chrome.records_X_apps_X_documents_XGET(recordId, appId, documentId, sessionToken, sessionTokenSecret, responseContentType, options);
        System.out.println(testResultObj + "\n\n");


        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/apps/{app_id}/documents/{document_id}/meta
        testResultDoc = (Document) chrome.records_X_apps_X_documents_X_metaGET(recordId, appId, documentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta
        testResultDoc = (Document) chrome.records_X_apps_X_documents_external_X_metaGET(recordId, appId, externalId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// POST /records/{record_id}/apps/{app_id}/documents/
//        testResultDoc = (Document) chrome.records_X_apps_X_documents_POST(recordId, appId, sessionToken, sessionTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/apps/{app_id}/documents/external/{external_id}
        testResultDoc = (Document) chrome.records_X_apps_X_documents_external_XPUT(recordId, appId, externalId, sessionToken, sessionTokenSecret, body, requestContentType, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: full_control
// PUT /records/{record_id}/apps/{app_id}/documents/{document_id}/label
        testResultDoc = (Document) chrome.records_X_apps_X_documents_X_labelPUT(recordId, appId, documentId, sessionToken, sessionTokenSecret, body, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /records/{record_id}/reports/minimal/measurements/{lab_code}/
//        testResultDoc = (Document) chrome.records_X_reports_minimal_measurements_X_GET(pagingOrderingQuery, recordId, labCode, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/reports/minimal/measurements/{lab_code}/
        testResultDoc = (Document) chrome.carenets_X_reports_minimal_measurements_X_GET(pagingOrderingQuery, carenetId, labCode, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /records/{record_id}/reports/minimal/medications/
        testResultDoc = (Document) chrome.records_X_reports_minimal_X_GET(pagingOrderingQuery, recordId, typeOfMinimalEgMedications, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/reports/minimal/medications/
        testResultDoc = (Document) chrome.carenets_X_reports_minimal_X_GET(pagingOrderingQuery, carenetId, typeOfMinimalEgMedications, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: account_ruleset.account_rule
// GET /accounts/{account_id}
        testResultDoc = (Document) chrome.accounts_XGET(accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: account_ruleset.account_rule
// POST /accounts/{account_id}/authsystems/password/change
        testResultDoc = (Document) chrome.accounts_X_authsystems_password_changePOST(old, newnew, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: full_control
// 
        testResultDoc = (Document) chrome.records_X_apps_XDELETE(recordId, appId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: None
// POST /oauth/internal/request_tokens/{request_token}/claim
        testResultDoc = (Document) chrome.oauth_internal_request_tokens_X_claimPOST(requestToken, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: account_ruleset.reqtoken_exists
// GET /oauth/internal/request_tokens/{request_token}/info
        testResultDoc = (Document) chrome.oauth_internal_request_tokens_X_infoGET(requestToken, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: account_ruleset.reqtoken_record, account_ruleset.reqtoken_carenet
// POST /oauth/internal/request_tokens/{request_token}/approve
        testResultForm = (Map<String,String>) chrome.oauth_internal_request_tokens_X_approvePOST(record_id, requestToken, sessionToken, sessionTokenSecret, options);
        System.out.println(printForm(testResultForm) + "\n\n");


        System.out.println("\n==================================================\n\n");

// account: None
// GET /oauth/internal/surl-verify?url={url}
        testResultDoc = (Document) chrome.oauth_internal_surlVerifyGET(url, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: full_control
// GET /records/{record_id}/carenets/
        testResultDoc = (Document) chrome.records_X_carenets_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/record
        testResultDoc = (Document) chrome.carenets_X_recordGET(carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
// GET /records/{record_id}/shares/
        testResultDoc = (Document) chrome.records_X_shares_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
// POST /records/{record_id}/shares/
//        testResultDoc = (Document) chrome.records_X_shares_POST(account_id, role_label, recordId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
// POST /records/{record_id}/shares/{account_id}/delete
        testResultDoc = (Document) chrome.records_X_shares_X_deletePOST(recordId, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// PUT /records/{record_id}/documents/{document_id}/carenets/{carenet_id}
//        testResultDoc = (Document) chrome.records_X_documents_X_carenets_XPUT(recordId, documentId, carenetId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// account: full_control
// DELETE /records/{record_id}/documents/{document_id}/carenets/{carenet_id}
        testResultDoc = (Document) chrome.records_X_documents_X_carenets_XDELETE(recordId, documentId, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// GET /records/{record_id}/documents/{document_id}/carenets/
        testResultDoc = (Document) chrome.records_X_documents_X_carenets_GET(pagingOrderingQuery, recordId, documentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// GET /records/{record_id}/autoshare/bytype/?type={indivo_document_type}
        testResultDoc = (Document) chrome.records_X_autoshare_bytype_GET(type, pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// machineapp: None -- account: full_control
// POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set
        testResultDoc = (Document) chrome.records_X_autoshare_carenets_X_bytype_setPOST(type, recordId, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/unset
        testResultDoc = (Document) chrome.records_X_autoshare_carenets_X_bytype_unsetPOST(type, recordId, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: accessrule_carenet_account
// GET /carenets/{carenet_id}/apps/
        testResultDoc = (Document) chrome.carenets_X_apps_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// PUT /carenets/{carenet_id}/apps/{pha_email}
        testResultDoc = (Document) chrome.carenets_X_apps_XPUT(carenetId, phaEmail, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// DELETE /carenets/{carenet_id}/apps/{pha_email}
        testResultDoc = (Document) chrome.carenets_X_apps_XDELETE(carenetId, phaEmail, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: accessrule_carenet_account
// GET /carenets/{carenet_id}/accounts/
        testResultDoc = (Document) chrome.carenets_X_accounts_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// POST /carenets/{carenet_id}/accounts/
        testResultDoc = (Document) chrome.carenets_X_accounts_POST(account_id, write, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// account: full_control
// DELETE /carenets/{carenet_id}/accounts/{account_id}
        testResultDoc = (Document) chrome.carenets_X_accounts_XDELETE(carenetId, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
// GET /carenets/{carenet_id}/accounts/{account_id}/permissions
        testResultDoc = (Document) chrome.carenets_X_accounts_X_permissionsGET(carenetId, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


    }
}
