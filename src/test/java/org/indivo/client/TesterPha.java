package org.indivo.client;

import java.util.Map;
import org.w3c.dom.Document;

class TesterPha {

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

    private String reason = null;
    private String status = null;
    private String relType = null;
    private String otherDocumentId = null;
    private String content = null;
    private String app_url = null;
    private String document_id = null;
    private String typeOfMinimalEgMedications = null;
    private String account_id = null;
    private String role_label = null;
    private String labCode = null;

    private String domToString(Document doc) {
        return null;
    }
    private String printForm(Map<String,String> fromFromIndivo) {
        return null;
    }

    private void hasTestCode() throws org.indivo.client.IndivoClientException {

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}
//    System.out.println("records_XGET\n" +
//        "GET /records/{record_id}");
//        testResultDoc = (Document) client.records_XGET(recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/
//    System.out.println("records_X_documents_GET\n" +
//        "GET /records/{record_id}/documents/");
//        testResultDoc = (Document) client.records_X_documents_GET(pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/documents/
//    System.out.println("carenets_X_documents_GET\n" +
//        "GET /carenets/{carenet_id}/documents/");
//        testResultDoc = (Document) client.carenets_X_documents_GET(pagingOrderingQuery, carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/?type={type_url}
//    System.out.println("records_X_documents_GET\n" +
//        "GET /records/{record_id}/documents/?type={type_url}");
//        testResultDoc = (Document) client.records_X_documents_GET(type, pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/documents/?type={type_url}
//    System.out.println("carenets_X_documents_GET\n" +
//        "GET /carenets/{carenet_id}/documents/?type={type_url}");
//        testResultDoc = (Document) client.carenets_X_documents_GET(type, pagingOrderingQuery, carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/{document_id}
//    System.out.println("records_X_documents_XGET\n" +
//        "GET /records/{record_id}/documents/{document_id}");
//        testResultObj = client.records_X_documents_XGET(recordId, documentId, accessToken, accessTokenSecret, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_document
//// GET /carenets/{carenet_id}/documents/{document_id}
//    System.out.println("carenets_X_documents_XGET\n" +
//        "GET /carenets/{carenet_id}/documents/{document_id}");
//        testResultObj = client.carenets_X_documents_XGET(carenetId, documentId, accessToken, accessTokenSecret, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
//// GET /records/{record_id}/documents/special/demographics
//    System.out.println("records_X_documents_special_demographicsGET\n" +
//        "GET /records/{record_id}/documents/special/demographics");
//        testResultDoc = (Document) client.records_X_documents_special_demographicsGET(recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/documents/special/demographics
//    System.out.println("carenets_X_documents_special_demographicsGET\n" +
//        "GET /carenets/{carenet_id}/documents/special/demographics");
//        testResultDoc = (Document) client.carenets_X_documents_special_demographicsGET(carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
//// GET /records/{record_id}/documents/special/contact
//    System.out.println("records_X_documents_special_contactGET\n" +
//        "GET /records/{record_id}/documents/special/contact");
//        testResultDoc = (Document) client.records_X_documents_special_contactGET(recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.machineapp_record_created_rule -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/documents/special/contact
//    System.out.println("carenets_X_documents_special_contactGET\n" +
//        "GET /carenets/{carenet_id}/documents/special/contact");
//        testResultDoc = (Document) client.carenets_X_documents_special_contactGET(carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/{document_id}/meta
//    System.out.println("records_X_documents_X_metaGET\n" +
//        "GET /records/{record_id}/documents/{document_id}/meta");
//        testResultDoc = (Document) client.records_X_documents_X_metaGET(recordId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /carenets/{carenet_id}/documents/{document_id}/meta
//    System.out.println("carenets_X_documents_X_metaGET\n" +
//        "GET /carenets/{carenet_id}/documents/{document_id}/meta");
//        testResultDoc = (Document) client.carenets_X_documents_X_metaGET(carenetId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/{document_id}/versions/
//    System.out.println("records_X_documents_X_versions_GET\n" +
//        "GET /records/{record_id}/documents/{document_id}/versions/");
//        testResultDoc = (Document) client.records_X_documents_X_versions_GET(pagingOrderingQuery, recordId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/external/{app_id}/{external_id}/meta
//    System.out.println("records_X_documents_external_X_X_metaGET\n" +
//        "GET /records/{record_id}/documents/external/{app_id}/{external_id}/meta");
//        testResultDoc = (Document) client.records_X_documents_external_X_X_metaGET(recordId, appId, externalId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: machapp_ruleset.no_external_id -- account: full_control
//// POST /records/{record_id}/documents/
//    System.out.println("records_X_documents_POST\n" +
//        "POST /records/{record_id}/documents/");
//        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/documents/external/{app_id}/{external_id}
//    System.out.println("records_X_documents_external_X_XPUT\n" +
//        "PUT /records/{record_id}/documents/external/{app_id}/{external_id}");
//        testResultDoc = (Document) client.records_X_documents_external_X_XPUT(recordId, appId, externalId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/documents/{document_id}/label
//    System.out.println("records_X_documents_X_labelPUT\n" +
//        "PUT /records/{record_id}/documents/{document_id}/label");
//        testResultDoc = (Document) client.records_X_documents_X_labelPUT(recordId, documentId, accessToken, accessTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/documents/external/{app_id}/{external_id}/label
//    System.out.println("records_X_documents_external_X_X_labelPUT\n" +
//        "PUT /records/{record_id}/documents/external/{app_id}/{external_id}/label");
//        testResultDoc = (Document) client.records_X_documents_external_X_X_labelPUT(recordId, appId, externalId, accessToken, accessTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
// POST /records/{record_id}/documents/{document_id}/replace
//    System.out.println("records_X_documents_X_replacePOST\n" +
//        "POST /records/{record_id}/documents/{document_id}/replace");
//        testResultDoc = (Document) client.records_X_documents_X_replacePOST(recordId, documentId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: full_control
//// PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}
//    System.out.println("records_X_documents_X_replace_external_X_XPUT\n" +
//        "PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}");
//        testResultDoc = (Document) client.records_X_documents_X_replace_external_X_XPUT(recordId, documentId, appId, externalId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// DELETE /records/{record_id}/documents/{document_id}
//    System.out.println("records_X_documents_XDELETE\n" +
//        "DELETE /records/{record_id}/documents/{document_id}");
//        testResultDoc = (Document) client.records_X_documents_XDELETE(recordId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// POST /records/{record_id}/documents/{document_id}/set-status
//    System.out.println("records_X_documents_X_setStatusPOST\n" +
//        "POST /records/{record_id}/documents/{document_id}/set-status");
//        testResultDoc = (Document) client.records_X_documents_X_setStatusPOST(reason, status, recordId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/{document_id}/status-history
//    System.out.println("records_X_documents_X_statusHistoryGET\n" +
//        "GET /records/{record_id}/documents/{document_id}/status-history");
//        testResultDoc = (Document) client.records_X_documents_X_statusHistoryGET(recordId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/{other_document_id}
//    System.out.println("records_X_documents_X_rels_X_XPUT\n" +
//            "PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/{other_document_id}");
//        testResultDoc = (Document) client.records_X_documents_X_rels_X_XPUT(
//                recordId, documentId, relType, otherDocumentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// POST /records/{record_id}/documents/{document_id}/rels/{rel_type}/
//    System.out.println("records_X_documents_X_rels_X_POST\n" +
//        "POST /records/{record_id}/documents/{document_id}/rels/{rel_type}/");
//        testResultDoc = (Document) client.records_X_documents_X_rels_X_POST(recordId, documentId, relType, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/external/{app_id}/{external_id}
//    System.out.println("records_X_documents_X_rels_X_external_X_XPUT\n" +
//        "PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/external/{app_id}/{external_id}");
//        testResultDoc = (Document) client.records_X_documents_X_rels_X_external_X_XPUT(recordId, documentId, relType, appId, externalId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/documents/{document_id}/rels/{rel_type}/
//    System.out.println("records_X_documents_X_rels_X_GET\n" +
//        "GET /records/{record_id}/documents/{document_id}/rels/{rel_type}/");
//        testResultDoc = (Document) client.records_X_documents_X_rels_X_GET(pagingOrderingQuery, recordId, documentId, relType, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: None
//// POST /records/{record_id}/notify
//    System.out.println("records_X_notifyPOST\n" +
//        "POST /records/{record_id}/notify");
//        testResultDoc = (Document) client.records_X_notifyPOST(content, app_url, document_id, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/apps/{app_id}/documents/
//    System.out.println("records_X_apps_X_documents_GET\n" +
//        "GET /records/{record_id}/apps/{app_id}/documents/");
//        testResultDoc = (Document) client.records_X_apps_X_documents_GET(pagingOrderingQuery, recordId, appId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/apps/{app_id}/documents/{document_id}
//    System.out.println("records_X_apps_X_documents_XGET\n" +
//        "GET /records/{record_id}/apps/{app_id}/documents/{document_id}");
//        testResultObj = client.records_X_apps_X_documents_XGET(recordId, appId, documentId, accessToken, accessTokenSecret, responseContentType, options);
//        System.out.println(testResultObj + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/apps/{app_id}/documents/{document_id}/meta
//    System.out.println("records_X_apps_X_documents_X_metaGET\n" +
//        "GET /records/{record_id}/apps/{app_id}/documents/{document_id}/meta");
//        testResultDoc = (Document) client.records_X_apps_X_documents_X_metaGET(recordId, appId, documentId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta
//    System.out.println("records_X_apps_X_documents_external_X_metaGET\n" +
//        "GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta");
//        testResultDoc = (Document) client.records_X_apps_X_documents_external_X_metaGET(recordId, appId, externalId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// POST /records/{record_id}/apps/{app_id}/documents/
//    System.out.println("records_X_apps_X_documents_POST\n" +
//        "POST /records/{record_id}/apps/{app_id}/documents/");
//        testResultDoc = (Document) client.records_X_apps_X_documents_POST(recordId, appId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/apps/{app_id}/documents/external/{external_id}
//    System.out.println("records_X_apps_X_documents_external_XPUT\n" +
//        "PUT /records/{record_id}/apps/{app_id}/documents/external/{external_id}");
//        testResultDoc = (Document) client.records_X_apps_X_documents_external_XPUT(recordId, appId, externalId, accessToken, accessTokenSecret, body, requestContentType, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: full_control
//// PUT /records/{record_id}/apps/{app_id}/documents/{document_id}/label
//    System.out.println("records_X_apps_X_documents_X_labelPUT\n" +
//        "PUT /records/{record_id}/apps/{app_id}/documents/{document_id}/label");
//        testResultDoc = (Document) client.records_X_apps_X_documents_X_labelPUT(recordId, appId, documentId, accessToken, accessTokenSecret, body, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /records/{record_id}/reports/minimal/measurements/{lab_code}/
//    System.out.println("records_X_reports_minimal_measurements_X_GET\n" +
//        "GET /records/{record_id}/reports/minimal/measurements/{lab_code}/");
//        testResultDoc = (Document) client.records_X_reports_minimal_measurements_X_GET(pagingOrderingQuery, recordId, labCode, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/reports/minimal/measurements/{lab_code}/
//    System.out.println("carenets_X_reports_minimal_measurements_X_GET\n" +
//        "GET /carenets/{carenet_id}/reports/minimal/measurements/{lab_code}/");
//        testResultDoc = (Document) client.carenets_X_reports_minimal_measurements_X_GET(pagingOrderingQuery, carenetId, labCode, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /records/{record_id}/reports/minimal/medications/
//    System.out.println("records_X_reports_minimal_X_GET\n" +
//        "GET /records/{record_id}/reports/minimal/medications/");
//        testResultDoc = (Document) client.records_X_reports_minimal_X_GET(pagingOrderingQuery, recordId, typeOfMinimalEgMedications, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/reports/minimal/medications/
//    System.out.println("carenets_X_reports_minimal_X_GET\n" +
//        "GET /carenets/{carenet_id}/reports/minimal/medications/");
//        testResultDoc = (Document) client.carenets_X_reports_minimal_X_GET(pagingOrderingQuery, carenetId, typeOfMinimalEgMedications, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/record
//    System.out.println("carenets_X_recordGET\n" +
//        "GET /carenets/{carenet_id}/record");
//        testResultDoc = (Document) client.carenets_X_recordGET(carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
//// GET /records/{record_id}/shares/
//    System.out.println("records_X_shares_GET\n" +
//        "GET /records/{record_id}/shares/");
//        testResultDoc = (Document) client.records_X_shares_GET(pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
//// POST /records/{record_id}/shares/
//    System.out.println("records_X_shares_POST\n" +
//        "POST /records/{record_id}/shares/");
//        testResultDoc = (Document) client.records_X_shares_POST(account_id, role_label, recordId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- machineapp: None -- account: account_ruleset.is_owner
//// POST /records/{record_id}/shares/{account_id}/delete
//    System.out.println("records_X_shares_X_deletePOST\n" +
//        "POST /records/{record_id}/shares/{account_id}/delete");
//        testResultDoc = (Document) client.records_X_shares_X_deletePOST(recordId, accountId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//// accesstoken: ar_share_record_or_pha -- account: accessrule_carenet_account
//// GET /carenets/{carenet_id}/accounts/{account_id}/permissions
//    System.out.println("carenets_X_accounts_X_permissionsGET\n" +
//        "GET /carenets/{carenet_id}/accounts/{account_id}/permissions");
//        testResultDoc = (Document) client.carenets_X_accounts_X_permissionsGET(carenetId, accountId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");


    }
}
