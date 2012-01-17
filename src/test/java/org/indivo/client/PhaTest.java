/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.indivo.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

/**
 *
 * @author nate
 */
public class PhaTest {

    //private boolean skipnone = false;

    private Map<String, Object> options = null;
    private String recordId = null;
    private String accessToken = null;
    private String accessTokenSecret = null;
//    private String problemApp = "problems@apps.indivo.org";
    private String allergyApp = "allergies@apps.indivo.org";
    private Properties properties = null;
    private String pagingOrderingQuery = null;
    private String extrnlRandom = null;

    private XPath xpath = null;

    private DatatypeFactory dtf = null;
    private XMLGregorianCalendar gregCal = null;
    private Rest client = null;
    private Rest chrome = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws
            IndivoClientException, IOException, XPathExpressionException, javax.xml.datatype.DatatypeConfigurationException {
        // TODO code application logic here
        PhaTest instance = new PhaTest();
        //instance.appIdEnc = URLEncoder.encode(instance.appId, "UTF-8");
        Properties props = new Properties();
        instance.properties = props;
        try {
            InputStream xmlStream =
                    instance.getClass().getResourceAsStream("/forPhaProperties.xml");
            if( xmlStream == null ) {
                throw new RuntimeException("unable to find resource: forPhaProperties.xml");
            }
            props.loadFromXML(xmlStream);
        } catch (IOException exception) {
            throw new RuntimeException("unable to find resource: forPhaProperties.xml");
        }
        instance.xpath = XPathFactory.newInstance().newXPath();
        instance.extrnlRandom = Integer.toString(new Random().nextInt(1000000));

        instance.dtf = DatatypeFactory.newInstance();

        instance.doTest();
    }

    private void reportKnownError(Exception ice) {
        String errId = Integer.toString(new Random().nextInt(1000000));
        System.err.println("\n\nerror id: " + errId);
//        System.err.println(ice.getMessage());
        ice.printStackTrace();
        System.err.println("==============================================\n");
        System.out.println("Known exception thrown, stack trace in error stream, see: " + errId + "\n");
//        System.out.println(ice.getMessage() + "\n");
    }

    private void doTest() throws IndivoClientException, XPathExpressionException, UnsupportedEncodingException {
        String testDocRecApp = "<Testing123>strictly for testing - record specific app specific blah blah</Testing123>";

        String testDocApp = "<Testing123>strictly for testing - app specific NOT record specific</Testing123>";

        String testDocExtrnl = "<Testing123>strictly for testing - external from pha</Testing123>";
        String allergyDoc =
"<Allergy xmlns=\"http://indivo.org/vocab/xml/documents#\">\n" +
"  <dateDiagnosed>2009-05-16</dateDiagnosed>\n" +
"  <diagnosedBy>Children's Hospital Boston</diagnosedBy>\n" +
"  <allergen>\n" +
"    <type type=\"http://codes.indivo.org/codes/allergentypes/\" value=\"drugs\">Drugs</type>\n" +
"    <name type=\"http://codes.indivo.org/codes/allergens/\" value=\"penicillin\">Penicillin</name>\n" +
"  </allergen>\n\n" +
"  <reaction>blue rash</reaction>\n" +
"  <specifics>this only happens on weekends - pha test</specifics>\n" +
"</Allergy>";

        String xmlDateTime = null;
        gregCal = dtf.newXMLGregorianCalendar(new GregorianCalendar());
        xmlDateTime = gregCal.toXMLFormat();

        String hba1cDoc = "<HBA1C xmlns=\"http://indivo.org/vocab#\" value=\"63.63\""
            + " unit=\"mg/dL\" datetime=\"" + xmlDateTime + "\" />";    // 2009-07-16T13:10:00


        /*<consumer_key>allergies@apps.indivo.org</consumer_key>
          <secret>allergies</secret>*/
        client = new Rest("allergies@apps.indivo.org", "allergies", "http://localhost:8000", null);
        String recordId_token = properties.getProperty("recordId_token");
        String[] recordId_tokenA = recordId_token.split(" ");
        if (recordId_tokenA.length != 3) {
            throw new RuntimeException("unexpected property value for recordId_token: " + recordId_token);
        }
        recordId = recordId_tokenA[0];
        accessToken = recordId_tokenA[1];
        accessTokenSecret = recordId_tokenA[2];

        System.out.println("records_XGET\n" + "GET /records/{record_id}");
        Document testResultDoc = (Document) client.records_XGET(recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_special_demographicsGET\n" +
        "GET /records/{record_id}/documents/special/demographics");
        testResultDoc = (Document) client.records_X_documents_special_demographicsGET(recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_special_contactGET\n" +
            "GET /records/{record_id}/documents/special/contact");
        testResultDoc = (Document) client.records_X_documents_special_contactGET(recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_POST\n" +
            "POST /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                allergyDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_1 = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_external_X_XPUT\n" +
        "PUT /records/{record_id}/documents/external/{app_id}/{external_id}");
        testResultDoc = (Document) client.records_X_documents_external_X_XPUT(recordId, allergyApp,
                "externalId_pha" + extrnlRandom, accessToken, accessTokenSecret,
                testDocExtrnl, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_1x = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n==================================================\n\n");

//        List<String> docsList = listAllDocs(recordId);
        System.out.println("records_X_documents_X_metaGET\n" +
            "GET /records/{record_id}/documents/{document_id}/meta");
        testResultDoc = (Document) client.records_X_documents_X_metaGET(recordId, docId_1,
                accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_external_X_X_metaGET\n" +
            "GET /records/{record_id}/documents/external/{app_id}/{external_id}/meta");
        testResultDoc = (Document) client.records_X_documents_external_X_X_metaGET(
                recordId, allergyApp, "externalId_pha" + extrnlRandom, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_X_labelPUT\n" +
            "PUT /records/{record_id}/documents/{document_id}/label");
        testResultDoc = (Document) client.records_X_documents_X_labelPUT(recordId, docId_1,
                accessToken, accessTokenSecret, "sneeze", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_external_X_X_labelPUT\n" +
        "PUT /records/{record_id}/documents/external/{app_id}/{external_id}/label");
        testResultDoc = (Document) client.records_X_documents_external_X_X_labelPUT(
                recordId, allergyApp, "externalId_pha" + extrnlRandom,
                accessToken, accessTokenSecret, "cough", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_POST\n" +
            "POST /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                allergyDoc.replace("blue rash", "green rash"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_replaceMe = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_documents_X_replacePOST\n" +
            "POST /records/{record_id}/documents/{document_id}/replace");
        testResultDoc = (Document) client.records_X_documents_X_replacePOST(recordId, docId_replaceMe,
                accessToken, accessTokenSecret,
                allergyDoc.replace("blue rash", "red rash"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------replacement---------------------------\n\n");

        System.out.println("records_X_documents_X_versions_GET\n" +
            "GET /records/{record_id}/documents/{document_id}/versions/");
        testResultDoc = (Document) client.records_X_documents_X_versions_GET(pagingOrderingQuery, recordId, docId_replaceMe,
                accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

//        System.out.println("records_X_documents_external_X_XPUT\n" +
//        "PUT /records/{record_id}/documents/external/{app_id}/{external_id}");
//        testResultDoc = (Document) client.records_X_documents_external_X_XPUT(recordId, allergyApp,
//                "externalId_toReplace" + extrnlRandom, accessToken, accessTokenSecret,
//                testDocExtrnl.replace("strictly","mostly"), "application/xml", options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        //String docId_x_replaceMe = xpath.evaluate("/Document/@id", testResultDoc);
//        System.out.println("\n---------------------------------------\n\n");

        System.out.println("records_X_documents_POST\n" +
            "POST /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                testDocExtrnl.replace("strictly", "partly"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_x_replaceWithExternal = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_documents_X_replace_external_X_XPUT\n" +
        "PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}");
        testResultDoc = (Document) client.records_X_documents_X_replace_external_X_XPUT(
                recordId, docId_x_replaceWithExternal, allergyApp, "externalId_replacement" + extrnlRandom,
                accessToken, accessTokenSecret,
                testDocExtrnl.replace("strictly","loosely"),"application/xml",  options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        testResultDoc = (Document) client.records_X_documents_external_X_X_metaGET(
                recordId, allergyApp, "externalId_replacement" + extrnlRandom, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_x_replaced = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n--------------------------------------------------\n\n");

        System.out.println("records_X_documents_X_metaGET\n" +
            "GET /records/{record_id}/documents/{document_id}/meta");
        testResultDoc = (Document) client.records_X_documents_X_metaGET(recordId, docId_x_replaced,
                accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n=partly===============================================\n\n");

        System.out.println("records_X_documents_POST\n" +
            "POST /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                allergyDoc.replace("blue rash", "green rash"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_deleteMe = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_documents_XGET\n" +
            "GET /records/{record_id}/documents/{document_id}");
        testResultDoc = (Document) client.records_X_documents_XGET(recordId, docId_deleteMe,
                accessToken, accessTokenSecret, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_XDELETE\n" +
            "DELETE /records/{record_id}/documents/{document_id}");
        testResultDoc = (Document) client.records_X_documents_XDELETE(recordId, docId_deleteMe,
                accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------\n\n");

        try {
            testResultDoc = (Document) client.records_X_documents_X_metaGET(recordId, docId_deleteMe,
                    accessToken, accessTokenSecret, options);
            throw new RuntimeException("should have thrown 404 above");
        } catch (IndivoClientExceptionHttp404 ice4) {
            System.out.println(ice4.toString());
        }
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n= 404 ===============================================\n\n");

        System.out.println("records_X_documents_POST\n" +
            "POST /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                allergyDoc.replace("blue rash", "orange rash"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_status = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_documents_X_setStatusPOST\n" +
            "POST /records/{record_id}/documents/{document_id}/set-status");
        testResultDoc = (Document) client.records_X_documents_X_setStatusPOST(
                "why not", "void", recordId, docId_status, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("records_X_documents_X_statusHistoryGET\n" +
            "GET /records/{record_id}/documents/{document_id}/status-history");
        testResultDoc = (Document) client.records_X_documents_X_statusHistoryGET(
                recordId, docId_status, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                allergyDoc.replace("blue rash", "yellow rash"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_rel_A = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n---------------------------------------------------\n\n");

        String testDocRelate_B = "<Testing123>strictly for testing Relate annotation: a golden yellow/Testing123>";
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                testDocRelate_B, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_rel_B = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_documents_X_rels_X_XPUT\n" +
            "PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/{other_document_id}");
        testResultDoc = (Document) client.records_X_documents_X_rels_X_XPUT(
                recordId, docId_rel_A, "annotation", docId_rel_B, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n---------------------------------------------------\n\n");
        
        System.out.println("records_X_documents_X_rels_X_POST\n" +
            "POST /records/{record_id}/documents/{document_id}/rels/{rel_type}/");
        testResultDoc = (Document) client.records_X_documents_X_rels_X_POST(recordId, docId_rel_A, "annotation",
                accessToken, accessTokenSecret, testDocRelate_B.replace("golden", "lemon"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_documents_X_rels_X_GET\n" +
            "GET /records/{record_id}/documents/{document_id}/rels/{rel_type}/");
        testResultDoc = (Document) client.records_X_documents_X_rels_X_GET(pagingOrderingQuery, recordId, docId_rel_A,
                "annotation", accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==related docs ================================================\n\n");


        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                allergyDoc.replace("weekends","Sundays"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_1x_toannotate = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n--------------------------------------------\n\n");

        System.out.println("records_X_documents_X_rels_X_external_X_XPUT\n" +
            "PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/external/{app_id}/{external_id}");
        testResultDoc = (Document) client.records_X_documents_X_rels_X_external_X_XPUT(
                recordId, docId_1x_toannotate, "annotation", allergyApp, "externalId_annotation" + extrnlRandom,
                accessToken, accessTokenSecret,
                testDocExtrnl.replace("external from", "external test annotation, from"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_GET\n" +
            "GET /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_GET(pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_documents_GET\n" +
            "GET /records/{record_id}/documents/?type={type_url}");
        testResultDoc = (Document) client.records_X_documents_GET(
                "http://indivo.org/vocab/xml/documents#Allergy",
                pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        //if (skipnone) {
        System.out.println("records_X_notifyPOST\n" +
                "POST /records/{record_id}/notify");
        testResultDoc = (Document) client.records_X_notifyPOST("take note", "huh?", docId_1,
                recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        //}

        System.out.println("++++++++++++++++++++++++++ record specific app specific ++++++++++++++++++++++++++++++++++++++++");
        System.out.println("records_X_apps_X_documents_POST\n" +
            "POST /records/{record_id}/apps/{app_id}/documents/");
        testResultDoc = (Document) client.records_X_apps_X_documents_POST(recordId, allergyApp,
                accessToken, accessTokenSecret, testDocRecApp, "application/xml", options);
        String docId_rec_app_1 = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_apps_X_documents_external_XPUT\n" +
            "PUT /records/{record_id}/apps/{app_id}/documents/external/{external_id}");
        testResultDoc = (Document) client.records_X_apps_X_documents_external_XPUT(recordId, allergyApp,
                "externalId_record_app_" + extrnlRandom, accessToken, accessTokenSecret,
                testDocRecApp.replace("app specific blah blah","app specific externalId"), "application/xml", options);
        String docId_rec_app_1x = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_apps_X_documents_X_labelPUT\n" +
            "PUT /records/{record_id}/apps/{app_id}/documents/{document_id}/label");
        testResultDoc = (Document) client.records_X_apps_X_documents_X_labelPUT(recordId, allergyApp,
                docId_rec_app_1, accessToken, accessTokenSecret, "label_on_rec_app", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_apps_X_documents_GET\n" +
            "GET /records/{record_id}/apps/{app_id}/documents/");
        testResultDoc = (Document) client.records_X_apps_X_documents_GET(pagingOrderingQuery,
                recordId, allergyApp, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_apps_X_documents_XGET\n" +
            "GET /records/{record_id}/apps/{app_id}/documents/{document_id}");
        testResultDoc = (Document) client.records_X_apps_X_documents_XGET(
                recordId, allergyApp, docId_rec_app_1, accessToken, accessTokenSecret, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_apps_X_documents_X_metaGET\n" +
        "GET /records/{record_id}/apps/{app_id}/documents/{document_id}/meta");
        testResultDoc = (Document) client.records_X_apps_X_documents_X_metaGET(recordId,
                allergyApp, docId_rec_app_1, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_apps_X_documents_external_X_metaGET\n" +
            "GET /records/{record_id}/apps/{app_id}/documents/external/{external_id}/meta");
        testResultDoc = (Document) client.records_X_apps_X_documents_external_X_metaGET(recordId,
                allergyApp, "externalId_record_app_" + extrnlRandom, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("+++ END record specific app specific, START app specific +++++++++++++++++");

        testResultDoc = (Document) client.apps_X_documents_POST(allergyApp, testDocApp, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) client.apps_X_documents_external_XPUT(
                allergyApp, "externalId_appSpecific_" + extrnlRandom,
                testDocApp.replace("testing","testing app specific"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) client.apps_X_documents_external_X_metaGET(
                allergyApp, "externalId_appSpecific_" + extrnlRandom, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_app_1x = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) client.apps_X_documents_X_metaGET(allergyApp, docId_app_1x, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) client.apps_X_documents_XGET(allergyApp, docId_app_1x, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) client.apps_X_documents_GET(pagingOrderingQuery, allergyApp, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        System.out.println("\n============= END app specific ===============\n\n");


        System.out.println("records_X_documents_POST\n" +
            "POST /records/{record_id}/documents/");
        testResultDoc = (Document) client.records_X_documents_POST(recordId, accessToken, accessTokenSecret,
                hba1cDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("records_X_reports_minimal_measurements_X_GET\n" +
            "GET /records/{record_id}/reports/minimal/measurements/{lab_code}/");
        testResultDoc = (Document) client.records_X_reports_minimal_measurements_X_GET(
                pagingOrderingQuery, recordId, "HBA1C", accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("records_X_reports_minimal_X_GET\n" +
            "GET /records/{record_id}/reports/minimal/medications/");
        testResultDoc = (Document) client.records_X_reports_minimal_X_GET(pagingOrderingQuery,
                recordId, "allergies", accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        String[] accountpassword = properties.getProperty("account_password").split(" ");
        String accountId = accountpassword[0];
//        String username = accountpassword[1];
//        String password = accountpassword[2];

        System.out.println("records_X_shares_POST\n" +
            "POST /records/{record_id}/shares/");
        /* testResultDoc = (Document) chrome.records_X_shares_POST(
         accountId, "physician", recordId, sessionToken, sessionTokenSecret, options);*/
        testResultDoc = (Document) client.records_X_shares_POST(
                accountId, "physician", recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("records_X_shares_GET\n" +
            "GET /records/{record_id}/shares/");
        testResultDoc = (Document) client.records_X_shares_GET(pagingOrderingQuery,
                recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("records_X_shares_X_deletePOST\n" +
        "POST /records/{record_id}/shares/{account_id}/delete");
        testResultDoc = (Document) client.records_X_shares_X_deletePOST(recordId, accountId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("records_X_shares_GET\n" +
            "GET /records/{record_id}/shares/");
        testResultDoc = (Document) client.records_X_shares_GET(pagingOrderingQuery,
                recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        String carenetPhysician = properties.getProperty("carenets_physicians");
        String docId_contact = properties.getProperty("contact_docId");
        String docId_demographics = properties.getProperty("demographics_docId");
        someChromeWork(docId_1, docId_contact, docId_demographics, carenetPhysician);

        System.out.println("carenets_X_documents_GET\n" +
            "GET /carenets/{carenet_id}/documents/");
        testResultDoc = (Document) client.carenets_X_documents_GET(pagingOrderingQuery, carenetPhysician,
                accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("carenets_X_documents_GET\n" +
            "GET /carenets/{carenet_id}/documents/?type={type_url}");
        testResultDoc = (Document) client.carenets_X_documents_GET(
                "http://indivo.org/vocab/xml/documents#Allergy", pagingOrderingQuery,
                carenetPhysician, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("carenets_X_documents_XGET\n" +
            "GET /carenets/{carenet_id}/documents/{document_id}");
        testResultDoc = (Document) client.carenets_X_documents_XGET(
                carenetPhysician, docId_1, accessToken, accessTokenSecret, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");



        System.out.println("carenets_X_documents_special_demographicsGET\n" +
            "GET /carenets/{carenet_id}/documents/special/demographics");
        testResultDoc = (Document) client.carenets_X_documents_special_demographicsGET(
                carenetPhysician, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("carenets_X_documents_special_contactGET\n" +
            "GET /carenets/{carenet_id}/documents/special/contact");
        testResultDoc = (Document) client.carenets_X_documents_special_contactGET(carenetPhysician,
                accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("carenets_X_documents_X_metaGET\n" +
            "GET /carenets/{carenet_id}/documents/{document_id}/meta");
        testResultDoc = (Document) client.carenets_X_documents_X_metaGET(carenetPhysician,
                docId_1, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

//        if (skipnone)
        try {
        System.out.println("carenets_X_reports_minimal_measurements_X_GET\n" +
            "GET /carenets/{carenet_id}/reports/minimal/measurements/{lab_code}/");
        testResultDoc = (Document) client.carenets_X_reports_minimal_measurements_X_GET(
                pagingOrderingQuery, carenetPhysician, "HBA1C", accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }


        System.out.println("carenets_X_reports_minimal_X_GET\n" +
            "GET /carenets/{carenet_id}/reports/minimal/allergies/");
        testResultDoc = (Document) client.carenets_X_reports_minimal_X_GET(
                pagingOrderingQuery, carenetPhysician, "allergies", accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("carenets_X_recordGET\n" +
            "GET /carenets/{carenet_id}/record");
        testResultDoc = (Document) client.carenets_X_recordGET(
                carenetPhysician, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("carenets_X_accounts_X_permissionsGET\n" +
            "GET /carenets/{carenet_id}/accounts/{account_id}/permissions");
        testResultDoc = (Document) client.carenets_X_accounts_X_permissionsGET(
                carenetPhysician, accountId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

    }

    private List<String> listAllDocs(String recId)
            throws IndivoClientException, XPathExpressionException {
        List<String> retVal = new ArrayList<String>();

        Document docsDoc = (Document) client.records_X_documents_GET(pagingOrderingQuery, recordId, accessToken, accessTokenSecret, options);

        NodeList allDocs = (NodeList) xpath.evaluate("/Documents/Document/@id", docsDoc, XPathConstants.NODESET);
        System.out.println("allDocs.getLength(): " + allDocs.getLength());

        for (int ii = 0; ii < allDocs.getLength(); ii++) {
            String idStr = ((Attr)allDocs.item(ii)).getValue();
            retVal.add(idStr);
        }
        return retVal;
    }

    private void someChromeWork(String docId, String contactId, String demographicsId, String carenetId)
            throws IndivoClientException {
        chrome = new Rest("chrome", "chrome", "http://localhost:8000", null);
        String[] accountpassword = properties.getProperty("account_password").split(" ");
        String accountId = accountpassword[0];
        String username = accountpassword[1];
        String password = accountpassword[2];

        String testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        Map<String,String> testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        String sessionToken = testResultForm.get("oauth_token");
        String sessionTokenSecret = testResultForm.get("oauth_token_secret");

        Document resltDoc = (Document) chrome.records_X_documents_X_carenets_XPUT(
                recordId, docId, carenetId, sessionToken, sessionTokenSecret, options);

        resltDoc = (Document) chrome.records_X_documents_X_carenets_XPUT(
                recordId, contactId, carenetId, sessionToken, sessionTokenSecret, options);

        resltDoc = (Document) chrome.records_X_documents_X_carenets_XPUT(
                recordId, demographicsId, carenetId, sessionToken, sessionTokenSecret, options);
    }

}
