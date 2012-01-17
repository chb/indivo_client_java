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

