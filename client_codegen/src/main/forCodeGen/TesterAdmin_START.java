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

