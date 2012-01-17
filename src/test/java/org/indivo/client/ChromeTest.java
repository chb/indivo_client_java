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

import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.GregorianCalendar;

import java.net.URLEncoder;
import javax.xml.datatype.DatatypeConfigurationException;

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
public class ChromeTest {

    //private boolean skipnone = false;

    private Map<String, Object> options = null;
    private String recordId = null;
    private String recordId_second = null;
    private String appId = "chrome@apps.indivo.org";
    private String problemApp = "problems@apps.indivo.org";
    private Properties properties = null;
    private String sessionToken = null;
    private String sessionTokenSecret = null;
    private String sessionToken_second = null;
    private String sessionTokenSecret_second = null;
    private String pagingOrderingQuery = null;
    private String extrnlRandom = null;

    private XPath xpath = null;

    private DatatypeFactory dtf = null;
    private XMLGregorianCalendar gregCal = null;

    private String demographicsDoc =
"<Demographics xmlns=\"Demographics\">"
+ "<dateOfBirth>2009-11-02T00:40:30+00:00</dateOfBirth>"
+ "</Demographics>";
    
    private String contactDoc =
"<Contact  xmlns=\"Contact\">"
+ "<name><fullname>nameForTestDocument</fullname><givenName/><familyName/></name>"
+ "<email type=\"work\">nathan.finstein@childrens.harvard.edu</email>"
+ "<address type=\"home\"><streetAddress>1 one way</streetAddress>"
+ "<postalCode>12345</postalCode>"
+ "<locality>ames</locality>"
+ "<region>Colorado</region>"
+ "<country>USA</country></address>"
+ "<location type=\"home\"><latitude>90</latitude><longitude>90</longitude></location>"
+ "</Contact>";

    String testDocExtrnl = "<Testing123>strictly for testing - external from chrome</Testing123>";
    String testDocStatus = "<Testing123>strictly for testing Status from chrome</Testing123>";
    String testDocRelate_A = "<Testing123>strictly for testing Relate from chrome A</Testing123>";
    String testDocRelate_B = "<Testing123>strictly for testing Relate from chrome B</Testing123>";
    String testDocAppSpecific = "<Testing123>strictly for testing - app specific from chrome</Testing123>";
    String testDocAppSpecificR = "<Testing123>strictly for testing - app specific record specific from chrome</Testing123>";
    String testDoc = "<Testing123>strictly for testing - chrome created no externalID</Testing123>";
    //String testDocRplc = "<Testing123>strictly for testing - replaced by Chrome</Testing123>";
    String testDocEstablished = "<Testing123>strictly for testing - Have Chrome try to delete later</Testing123>";
    String testDocToDelete = "<Testing123>strictly for testing - Chrome should be able to delete this if done soon</Testing123>";
    String testDocToReplace = "<Testing123>strictly for testing - Chrome will replace this version</Testing123>";
    String testDocReplacement = "<Testing123>strictly for testing - Chrome will use this to replace the other</Testing123>";
    String testDocToShare = "<Testing123>share this</Testing123>";

    String allergyDoc =
"<Allergy xmlns=\"http://indivo.org/vocab/xml/documents#\">\n" +
"  <dateDiagnosed>2009-05-16</dateDiagnosed>\n" +
"  <diagnosedBy>Children's Hospital Boston</diagnosedBy>\n" +
"  <allergen>\n" +
"    <type type=\"http://codes.indivo.org/codes/allergentypes/\" value=\"drugs\">Drugs</type>\n" +
"    <name type=\"http://codes.indivo.org/codes/allergens/\" value=\"penicillin\">Penicillin</name>\n" +
"  </allergen>\n\n" +
"  <reaction>blue rash</reaction>\n" +
"  <specifics>this only happens on weekends</specifics>\n" +
"</Allergy>";

    
    String medicationDoc =
"<Medication xmlns=\"http://indivo.org/vocab/xml/documents#\">" +
  "<dateStarted>2009-02-05</dateStarted>" +
  "<name type=\"http://indivo.org/codes/meds#\" abbrev=\"c2i\" value=\"cox2-inhibitor\">COX2 Inhibitor</name>" +
  "<brandName type=\"http://indivo.org/codes/meds#\" abbrev=\"vioxx\" value=\"Vioxx\">Vioxx</brandName>" +
  "<dose><value>3</value><unit type=\"http://indivo.org/codes/units#\" value=\"pills\" abbrev=\"p\">pills</unit></dose>" +
  "<route type=\"http://indivo.org/codes/routes#\" value=\"PO\">By Mouth</route>" +
  "<strength><value>100</value><unit type=\"http://indivo.org/codes/units#\" value=\"mg\" abbrev=\"mg\">milligram</unit></strength>" +
  "<frequency type=\"http://indivo.org/codes/frequency#\" value=\"daily\">daily</frequency>" +
  "</Medication>";


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws
            IndivoClientException, IOException, XPathExpressionException, javax.xml.datatype.DatatypeConfigurationException {
        // TODO code application logic here
        ChromeTest instance = new ChromeTest();
        //instance.appIdEnc = URLEncoder.encode(instance.appId, "UTF-8");
        Properties props = new Properties();
        instance.properties = props;
        try {
            InputStream xmlStream =
                    instance.getClass().getResourceAsStream("/forChromeProperties.xml");
            if( xmlStream == null ) {
                throw new RuntimeException("unable to find resource: forChromeProperties.xml");
            }
            props.loadFromXML(xmlStream);
        } catch (IOException exception) {
            throw new RuntimeException("unable to find resource: forChromeProperties.xml", exception);
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
        Rest chrome = new Rest("chrome", "chrome", "http://localhost:8000", null);
        String[] accountpassword = properties.getProperty("account_password").split(" ");
        String accountId = accountpassword[0];
        String username = accountpassword[1];
        String password = accountpassword[2];

        String[] accountpassword_second = properties.getProperty("second_account_password").split(" ");
        String accountId_second = accountpassword[0];
        String username_second = accountpassword[1];
        String password_second = accountpassword[2];

        recordId = properties.getProperty("recordId");
        recordId_second = properties.getProperty("second_recordId");


        
        System.out.println("testing -- oauth_internal_session_createPOST");
        String testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        System.out.println("oauth_internal_session_createPOST  once");
        Map<String,String> testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken = testResultForm.get("oauth_token");
        sessionTokenSecret = testResultForm.get("oauth_token_secret");

        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username_second, password_second, null, null, options);
        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken_second = testResultForm.get("oauth_token");
        sessionTokenSecret_second = testResultForm.get("oauth_token_secret");

        Document testResultDoc = null;
        System.out.println("testing -- records_X_documents_X_versions_GET");

        System.out.println("testing -- records_X_documents_GET");
        String xmlDateTime = null;
        gregCal = dtf.newXMLGregorianCalendar(new GregorianCalendar());
        xmlDateTime = gregCal.toXMLFormat();

        String hba1cDoc = "<HBA1C xmlns=\"http://indivo.org/vocab#\" value=\"6.3\""
            + " unit=\"mg/dL\" datetime=\"" + xmlDateTime + "\" />";    // 2009-07-16T13:10:00

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, hba1cDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------\n\n");
        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret,
                hba1cDoc.replace("\"6.3\"", "\"7.4\""), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------\n\n");

        System.out.println("testing -- accounts_X_records_GET");
        testResultDoc = (Document) chrome.accounts_X_records_GET(
                pagingOrderingQuery, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("recordId: " + recordId);
        System.out.println("testing -- records_XGET");
        testResultDoc = (Document) chrome.records_XGET(recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_apps_GET");
        //record_has_app
        String[] recordHasApp = properties.getProperty("record_has_app").split(" ");
        if (recordHasApp.length != 2) {
            throw new RuntimeException(
                    "unexpected record_has_app property: " + properties.getProperty("record_has_app"));
        }
        testResultDoc = (Document) chrome.records_X_apps_GET(
                pagingOrderingQuery, recordHasApp[0], sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\nexpected: record has app: " + recordHasApp[0] + " " + recordHasApp[1] +
                "\n============\n\n");

        System.out.println("testing -- records_X_documents_GET");
        testResultDoc = (Document) chrome.records_X_documents_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        String carenetId = properties.getProperty("carenets_physicians");
        System.out.println("testing -- carenets_X_documents_GET");
        testResultDoc = (Document) chrome.carenets_X_documents_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_GET");
        testResultDoc = (Document) chrome.records_X_documents_GET(
                "http://indivo.org/vocab/xml/documents#Medication", pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- carenets_X_documents_GET");
        testResultDoc = (Document) chrome.carenets_X_documents_GET(
                "http://indivo.org/vocab/xml/documents#Medication", pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_special_demographicsPUT");
        testResultDoc = (Document) chrome.records_X_documents_special_demographicsPUT(
                recordId, sessionToken, sessionTokenSecret, demographicsDoc, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n-------------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_special_demographicsGET");
        testResultDoc = (Document) chrome.records_X_documents_special_demographicsGET(recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        try {
        System.out.println("testing -- carenets_X_documents_special_demographicsGET");
        testResultDoc = (Document) chrome.carenets_X_documents_special_demographicsGET(carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (Exception excp) {
            System.out.println(excp.getMessage());
        }

        System.out.println("testing -- records_X_reports_minimal_measurements_X_GET");
        testResultDoc = (Document) chrome.records_X_reports_minimal_measurements_X_GET(
        pagingOrderingQuery, recordId, "HBA1C", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

//        if (skipnone) 
        try {
        System.out.println("testing -- carenets_X_reports_minimal_measurements_X_GET");
        testResultDoc = (Document) chrome.carenets_X_reports_minimal_measurements_X_GET(
                pagingOrderingQuery, carenetId, "HBA1C", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

        testResultDoc = (Document) chrome.records_X_documents_POST(recordId, sessionToken, sessionTokenSecret,
                medicationDoc,  "application/xml", options);

        System.out.println("testing -- records_X_reports_minimal_X_GET");
        testResultDoc = (Document) chrome.records_X_reports_minimal_X_GET(
                pagingOrderingQuery, recordId, "medications", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- carenets_X_reports_minimal_X_GET");
        testResultDoc = (Document) chrome.carenets_X_reports_minimal_X_GET(pagingOrderingQuery, carenetId,
                "medications", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");



        System.out.println("testing -- accounts_XGET");
        testResultDoc = (Document) chrome.accounts_XGET(accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- accounts_X_infoSetPOST");
        testResultDoc = (Document) chrome.accounts_X_infoSetPOST(
                "new full_name", "nathan.finstein@childrens.harvard.edu", accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- accounts_X_authsystems_password_setUsernamePOST");
        testResultDoc = (Document) chrome.accounts_X_authsystems_password_setUsernamePOST(username + "_new", accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------\n\n");
        // now set it back right away so we don't break anything
        System.out.println("testing -- accounts_X_authsystems_password_setUsernamePOST");
        testResultDoc = (Document) chrome.accounts_X_authsystems_password_setUsernamePOST(username, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------\n\n");
        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        System.out.println("oauth_internal_session_createPOST  twice");
        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken = testResultForm.get("oauth_token");
        sessionTokenSecret = testResultForm.get("oauth_token_secret");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- accounts_X_authsystems_password_changePOST");
        testResultDoc = (Document) chrome.accounts_X_authsystems_password_changePOST(password, password + "_new", accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");
        // now change it right back
        System.out.println("testing -- accounts_X_authsystems_password_changePOST");
        testResultDoc = (Document) chrome.accounts_X_authsystems_password_changePOST(password + "_new", password, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        // now set it back to what it was
        System.out.println("testing -- accounts_X_authsystems_password_setUsernamePOST");
//        testResultDoc = (Document) chrome.accounts_X_authsystems_password_setUsernamePOST(username, accountId, sessionToken, sessionTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//        if (skipnone)
        try {
        System.out.println("testing -- records_X_apps_XDELETE");
        testResultDoc = (Document) chrome.records_X_apps_XDELETE(
                recordId_second, problemApp, sessionToken_second, sessionTokenSecret_second, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }


        if (false) {  // skip for now, test with OAuth dance
        System.out.println("testing -- oauth_internal_request_tokens_X_claimPOST");
        testResultDoc = (Document) chrome.oauth_internal_request_tokens_X_claimPOST("requestToken", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- oauth_internal_request_tokens_X_infoGET");
        testResultDoc = (Document) chrome.oauth_internal_request_tokens_X_infoGET("requestToken", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- oauth_internal_request_tokens_X_approvePOST");
        testResultForm = (Map<String,String>) chrome.oauth_internal_request_tokens_X_approvePOST(recordId, "requestToken",
                sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- oauth_internal_surlVerifyGET");
        testResultDoc = (Document) chrome.oauth_internal_surlVerifyGET("/widgets/WidgetName?param1=foo&param2=bar&surl_timestamp=<TIMESTAMP>&surl_token=<TOKEN>&surl_sig=<SIGNATURE>", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        }

        System.out.println("testing -- records_X_carenets_GET");
        testResultDoc = (Document) chrome.records_X_carenets_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- carenets_X_recordGET");
        testResultDoc = (Document) chrome.carenets_X_recordGET(carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_shares_POST");
        testResultDoc = (Document) chrome.records_X_shares_POST(accountId, "physician", recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n-------------------------------------------\n\n");

        System.out.println("testing -- records_X_shares_GET");
        testResultDoc = (Document) chrome.records_X_shares_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n-------------------------------------------\n\n");

        System.out.println("testing -- records_X_shares_X_deletePOST");
        testResultDoc = (Document) chrome.records_X_shares_X_deletePOST(recordId, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n-------------------------------------------\n\n");

        System.out.println("testing -- records_X_shares_GET");
        testResultDoc = (Document) chrome.records_X_shares_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc));
        System.out.println("\n-should be no shares--------------------------\n\n");

        // put it back for later testing
        System.out.println("testing -- records_X_shares_POST");
        testResultDoc = (Document) chrome.records_X_shares_POST(accountId, "physician", recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        List<String> docsList = null;
        try {
            docsList = listAllDocs(recordId, chrome);
        } catch (XPathExpressionException xpee) {
            throw new RuntimeException(xpee);
        }
        System.out.println("testing -- records_X_documents_X_metaGET");
        testResultDoc = (Document) chrome.records_X_documents_X_metaGET(
                recordId, docsList.get(0), sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        System.out.println("testing -- carenets_X_documents_X_metaGET");
        testResultDoc = (Document) chrome.carenets_X_documents_X_metaGET(
                carenetId, docsList.get(0), sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_XGET");
        testResultDoc = (Document) chrome.records_X_documents_XGET(
                recordId, docsList.get(0), sessionToken, sessionTokenSecret, "application/xml", options);
        System.out.println("from: chrome.records_X_documents_XGET" + Utils.domToString(testResultDoc) + "\n\n");

        shareAllDocs(recordId, docsList, carenetId, chrome);

        System.out.println("testing -- carenets_X_documents_XGET");
        testResultDoc = (Document) chrome.carenets_X_documents_XGET(carenetId, docsList.get(0), sessionToken, sessionTokenSecret, "application/xml", options);
        System.out.println("from: chrome.carenets_X_documents_XGET" + Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        //shareAllDocs(recordId, docsList, carenetId, chrome);



        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        System.out.println("oauth_internal_session_createPOST  twice");
        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken = testResultForm.get("oauth_token");
        sessionTokenSecret = testResultForm.get("oauth_token_secret");


        System.out.println("testing -- carenets_X_documents_special_demographicsGET");
        testResultDoc = (Document) chrome.carenets_X_documents_special_demographicsGET(carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_special_contactPUT");
        testResultDoc = (Document) chrome.records_X_documents_special_contactPUT(
                recordId, sessionToken, sessionTokenSecret, contactDoc, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String newestContactId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_special_contactGET");
        testResultDoc = (Document) chrome.records_X_documents_special_contactGET(recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - -  - ------------------------------\n\n");

        List<String> justNewContactId = new ArrayList<String>();
        justNewContactId.add(newestContactId);
        shareAllDocs(recordId, justNewContactId, carenetId, chrome);

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                    recordId, sessionToken, sessionTokenSecret, testDocToShare, "application/xml", options);
        String docToShareId = xpath.evaluate("/Document/@id", testResultDoc);
        List<String> justToShareId = new ArrayList<String>();
        justToShareId.add(docToShareId);
        shareAllDocs(recordId, justToShareId, carenetId, chrome);

        System.out.println("testing -- carenets_X_documents_XGET");
        testResultDoc = (Document) chrome.carenets_X_documents_XGET(
                carenetId, newestContactId, sessionToken, sessionTokenSecret, "application/xml", options);
        System.out.println("from: chrome.carenets_X_documents_XGET" + Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n-------------------------------------------------\n\n");

        System.out.println("testing -- carenets_X_documents_special_contactGET");
        testResultDoc = (Document) chrome.carenets_X_documents_special_contactGET(carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

//        if (skipnone)
        try{
        System.out.println("testing -- records_X_documents_external_X_XPUT");
        testResultDoc = (Document) chrome.records_X_documents_external_X_XPUT(
                recordId, appId, "externalId" + extrnlRandom, sessionToken, sessionTokenSecret, testDocExtrnl, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

       String establishedDocumentId = properties.getProperty("establishedDocumentId");
        if (establishedDocumentId == null) {
            // add a document and try deleting it in a much later run
        System.out.println("testing -- records_X_documents_POST");
            testResultDoc = (Document) chrome.records_X_documents_POST(
                    recordId, sessionToken, sessionTokenSecret, testDocEstablished, "application/xml", options);
            System.out.println(Utils.domToString(testResultDoc) + "\n\n");
            establishedDocumentId = xpath.evaluate("/Document/@id", testResultDoc);
            System.out.println("\nadding to establishedDocumentId to properties\n\n");
            properties.setProperty("establishedDocumentId", establishedDocumentId);
            try {
                OutputStream propOs = new FileOutputStream("forChromeProperties.xml_NEW");
                properties.storeToXML(propOs, "added establishedDocumentId", "UTF-8");
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            System.out.println("\n--------------------------------------\n\n");
        } else //if (skipnone)
            try {
            boolean deleteSuccess = true;
            try {
        System.out.println("testing -- records_X_documents_XDELETE");
            testResultDoc = (Document) chrome.records_X_documents_XDELETE(recordId, establishedDocumentId, sessionToken, sessionTokenSecret, options);
            System.out.println(Utils.domToString(testResultDoc) + "\n\n");
            } catch (IndivoClientException ice) {
                System.out.println("failed to delete established document, as expected: ");
        System.out.println("testing -- records_X_documents_XGET");
                testResultDoc = (Document) chrome.records_X_documents_XGET(
                        recordId, establishedDocumentId, sessionToken, sessionTokenSecret, "application/xml", options);
                System.out.println(testResultDoc + "\n\n");
                System.out.println("\n--------------------------------------\n\n");
                deleteSuccess = false;
            }
            if (deleteSuccess) {
                System.out.println("was not supposed to be able to delete: "  + establishedDocumentId);
                System.out.println(Utils.domToString(testResultDoc) + "\n\n");
                boolean ableToReadEstablished = true;
                try {
        System.out.println("testing -- records_X_documents_XGET");
                    testResultDoc = (Document) chrome.records_X_documents_XGET(
                        recordId, establishedDocumentId, sessionToken, sessionTokenSecret, "application/xml", options);
                } catch (IndivoClientException ice) {
                    System.out.println("tried to read impossible-to-delete document, here is the result: " + ice.getMessage());
                    ableToReadEstablished = false;
                }
                if (ableToReadEstablished) {
                    System.out.println("tried and succeeded in reading impossible-to-delete document, here is the result: ");
                    System.out.println(Utils.domToString(testResultDoc));
                }
                throw new RuntimeException("was not supposed to be able to delete: "  + establishedDocumentId);
            }
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }




        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        System.out.println("oauth_internal_session_createPOST  twice");
        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken = testResultForm.get("oauth_token");
        sessionTokenSecret = testResultForm.get("oauth_token_secret");


        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                    recordId, sessionToken, sessionTokenSecret, testDocToDelete, "application/xml", options);

        String deletableDocumentId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("testing -- records_X_documents_XDELETE");
        testResultDoc = (Document) chrome.records_X_documents_XDELETE(
                recordId, deletableDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");

        boolean wasinfactdeleted = false;
        try {
        System.out.println("testing -- records_X_documents_XGET");
        testResultDoc = (Document) chrome.records_X_documents_XGET(
                recordId, deletableDocumentId, sessionToken, sessionTokenSecret, "application/xml", options);
        } catch (IndivoClientException ice) {
            System.out.println("doc should have been deleted: " + ice.getMessage());
            wasinfactdeleted = true;
        }
        if (! wasinfactdeleted) {
            System.out.println(Utils.domToString(testResultDoc) + "\n\n");
            throw new RuntimeException("should have been deleted");
        }
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, testDocToReplace, "application/xml", options);
        String toreplaceDocumentId = xpath.evaluate("/Document/@id", testResultDoc);
        
        System.out.println("testing -- records_X_documents_X_replacePOST");
        testResultDoc = (Document) chrome.records_X_documents_X_replacePOST(
                recordId, toreplaceDocumentId, sessionToken, sessionTokenSecret, testDocReplacement, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String thereplacementDocumentId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n==================================================\n\n");



        System.out.println("testing -- records_X_documents_X_versions_GET");
        testResultDoc = (Document) chrome.records_X_documents_X_versions_GET(
                pagingOrderingQuery, recordId, toreplaceDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        System.out.println("testing -- records_X_documents_X_versions_GET");
        testResultDoc = (Document) chrome.records_X_documents_X_versions_GET(
                pagingOrderingQuery, recordId, thereplacementDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, testDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String gettingLabelId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n--------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_X_labelPUT");
        testResultDoc = (Document) chrome.records_X_documents_X_labelPUT(
                recordId, gettingLabelId, sessionToken, sessionTokenSecret, "labelLabel", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

//        if (skipnone)
        try {
        System.out.println("testing -- records_X_documents_external_X_X_metaGET");
        testResultDoc = (Document) chrome.records_X_documents_external_X_X_metaGET(
                recordId, appId, "externalId" + extrnlRandom, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_external_X_X_labelPUT");
        testResultDoc = (Document) chrome.records_X_documents_external_X_X_labelPUT(
                recordId, appId, "externalId" + extrnlRandom, sessionToken, sessionTokenSecret, "labelForExternal", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        /* FIXME -- this call is changed by a more up-to-date codegen */
        System.out.println("testing -- records_X_documents_X_replace_external_X_XPUT");
        testResultDoc = (Document) chrome.records_X_documents_X_replace_external_X_XPUT(
                recordId, "documentId", appId, "externalId" + extrnlRandom, sessionToken, sessionTokenSecret,
            "body", "requestContentType", options);

        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }



        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, testDocStatus, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String statusDocumentId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n----------------------------------------------\n\n");



        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        System.out.println("oauth_internal_session_createPOST  twice");
        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken = testResultForm.get("oauth_token");
        sessionTokenSecret = testResultForm.get("oauth_token_secret");


        System.out.println("testing -- records_X_documents_X_setStatusPOST");
        testResultDoc = (Document) chrome.records_X_documents_X_setStatusPOST(
                "because", "void", recordId, statusDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_X_setStatusPOST");
        testResultDoc = (Document) chrome.records_X_documents_X_setStatusPOST(
                "because", "archived", recordId, statusDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_X_setStatusPOST");
        testResultDoc = (Document) chrome.records_X_documents_X_setStatusPOST(
                "because", "active", recordId, statusDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_X_statusHistoryGET");
        testResultDoc = (Document) chrome.records_X_documents_X_statusHistoryGET(
                recordId, statusDocumentId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


//        if (skipnone)
///        try {
        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, testDocRelate_A, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String relateDocumentId_A = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, testDocRelate_B, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String relateDocumentId_B = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n----------------------------------------------\n\n");
///        } catch (IndivoClientException ice) {
///            reportKnownError(ice);
///        }

        System.out.println("testing -- records_X_documents_X_rels_X_POST");
        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_POST(
                recordId, relateDocumentId_B, "interpretation", sessionToken, sessionTokenSecret,
                testDocRelate_A.replace(" A", " C"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- records_X_documents_X_rels_X_XPUT");
        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_XPUT(
                recordId, relateDocumentId_A, "interpretation", relateDocumentId_B, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        try {
        System.out.println("testing -- records_X_documents_X_rels_X_external_X_XPUT");
        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_external_X_XPUT(recordId, relateDocumentId_A, "interpretation",
                appId, "externalId_for_rels" + extrnlRandom, sessionToken, sessionTokenSecret, testDocExtrnl, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

        System.out.println("testing -- records_X_documents_X_rels_X_GET");
        testResultDoc = (Document) chrome.records_X_documents_X_rels_X_GET(pagingOrderingQuery, recordId, relateDocumentId_B,
                "interpretation", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


//        if (skipnone)
        try {
        System.out.println("testing -- accounts_X_inbox_GET");
        testResultDoc = (Document) chrome.accounts_X_inbox_GET("0", pagingOrderingQuery, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("testing -- accounts_X_inbox_XGET");
        testResultDoc = (Document) chrome.accounts_X_inbox_XGET(accountId, "messageId", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- accounts_X_inbox_X_attachments_X_acceptPOST");
        testResultDoc = (Document) chrome.accounts_X_inbox_X_attachments_X_acceptPOST(accountId, "messageId", 1,  sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }




        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST(username, password, null, null, options);
        System.out.println("oauth_internal_session_createPOST  twice");
        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        sessionToken = testResultForm.get("oauth_token");
        sessionTokenSecret = testResultForm.get("oauth_token_secret");


//        if (skipnone)
        try {
        System.out.println("TESTING APP SPECIFIC");
        System.out.println("testing -- apps_X_documents_POST");
        testResultDoc = (Document) chrome.apps_X_documents_POST(appId, testDocAppSpecific, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String appSpecificDocumentId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- apps_X_documents_external_XPUT");
        testResultDoc = (Document) chrome.apps_X_documents_external_XPUT(
                appId, "externalId_appSpec" + extrnlRandom, testDocAppSpecific, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("testing -- apps_X_documents_X_labelPUT");
        testResultDoc = (Document) chrome.apps_X_documents_X_labelPUT(appId, appSpecificDocumentId, "this_is_app_specific", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- apps_X_documents_GET");
        testResultDoc = (Document) chrome.apps_X_documents_GET(pagingOrderingQuery, appId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- apps_X_documents_X_metaGET");
        testResultDoc = (Document) chrome.apps_X_documents_X_metaGET(appId, appSpecificDocumentId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        System.out.println("testing -- apps_X_documents_external_X_metaGET");
        testResultDoc = (Document) chrome.apps_X_documents_external_X_metaGET(appId, "externalId" + extrnlRandom, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("testing -- apps_X_documents_XGET");
        testResultDoc = (Document) chrome.apps_X_documents_XGET(appId, appSpecificDocumentId, "application/xml", options);
        System.out.println(testResultDoc + "\n\n");
        System.out.println("\n----------------------------------------------\n\n");

        //System.out.println("encoded: " + URLEncoder.encode(appId, "UTF-8"));
        System.out.println("testing -- records_X_apps_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_apps_X_documents_POST(
                recordId, appId, sessionToken, sessionTokenSecret, testDocAppSpecificR, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        String appRecSpecificDocumentId = xpath.evaluate("/Document/@id", testResultDoc);

        System.out.println("testing -- records_X_apps_X_documents_external_XPUT");
        testResultDoc = (Document) chrome.records_X_apps_X_documents_external_XPUT(recordId, appId,
                "externalId_recApp" + extrnlRandom,  sessionToken, sessionTokenSecret, testDocAppSpecificR, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_apps_X_documents_X_labelPUT");
        testResultDoc = (Document) chrome.records_X_apps_X_documents_X_labelPUT(recordId, appId,
                appRecSpecificDocumentId, sessionToken, sessionTokenSecret, "labelForAppRec", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");



        System.out.println("testing -- records_X_apps_X_documents_GET");
        testResultDoc = (Document) chrome.records_X_apps_X_documents_GET(
        pagingOrderingQuery, recordId, appId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        Object testResultObj = chrome.records_X_apps_X_documents_XGET(
            recordId, appId, appRecSpecificDocumentId, sessionToken, sessionTokenSecret, "application/xml", options);
        System.out.println(testResultObj + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_apps_X_documents_X_metaGET");
        testResultDoc = (Document) chrome.records_X_apps_X_documents_X_metaGET(
                recordId, appId, appRecSpecificDocumentId,  sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_apps_X_documents_external_X_metaGET");
        testResultDoc = (Document) chrome.records_X_apps_X_documents_external_X_metaGET(
                recordId, appId, "externalId_recApp" + extrnlRandom, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }


        System.out.println("testing -- records_X_documents_X_carenets_GET");
        testResultDoc = (Document) chrome.records_X_documents_X_carenets_GET(pagingOrderingQuery, recordId,
                docToShareId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n..................................................\n\n");

        System.out.println("testing -- records_X_documents_X_carenets_XDELETE");
        testResultDoc = (Document) chrome.records_X_documents_X_carenets_XDELETE(
                recordId, docToShareId, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n...................................................\n\n");

        System.out.println("testing -- records_X_documents_X_carenets_GET");
        testResultDoc = (Document) chrome.records_X_documents_X_carenets_GET(pagingOrderingQuery, recordId,
                docToShareId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n...................................................\n\n");

        System.out.println("testing -- records_X_documents_X_neversharePUT");
        testResultDoc = (Document) chrome.records_X_documents_X_neversharePUT(recordId, docToShareId,
                sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n....................................................\n\n");


        try {
        shareAllDocs(recordId, justToShareId, carenetId, chrome);
        } catch (org.indivo.client.IndivoClientExceptionHttp404 expected404) {
            System.out.println("sort of expected, but 403 seemed more intuitive: " + expected404.getMessage());
        }
        System.out.println("\n.................................................\n\n");

        System.out.println("testing -- records_X_documents_X_nevershareDELETE");
        testResultDoc = (Document) chrome.records_X_documents_X_nevershareDELETE(
                recordId, docToShareId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n..................................................\n\n");

        shareAllDocs(recordId, justToShareId, carenetId, chrome);
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) chrome.records_X_documents_POST(
                recordId, sessionToken, sessionTokenSecret, allergyDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------\n\n");

        System.out.println("testing -- records_X_autoshare_bytype_allGET");
        testResultDoc = (Document) chrome.records_X_autoshare_bytype_allGET(recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------\n\n");

        System.out.println("testing -- records_X_autoshare_carenets_X_bytype_setPOST");
        testResultDoc = (Document) chrome.records_X_autoshare_carenets_X_bytype_setPOST("http://indivo.org/vocab/xml/documents#Allergy", recordId,
                carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n-------------------------------------------------\n\n");

        System.out.println("testing -- records_X_autoshare_bytype_allGET");
        testResultDoc = (Document) chrome.records_X_autoshare_bytype_allGET(recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----------------------------------------\n\n");

        System.out.println("testing -- records_X_autoshare_bytype_GET");
        testResultDoc = (Document) chrome.records_X_autoshare_bytype_GET("http://indivo.org/vocab/xml/documents#Allergy", pagingOrderingQuery,
                recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n---------------------------------------------------\n\n");

        System.out.println("testing -- records_X_autoshare_carenets_X_bytype_unsetPOST");
        testResultDoc = (Document) chrome.records_X_autoshare_carenets_X_bytype_unsetPOST("http://indivo.org/vocab/xml/documents#Allergy", recordId,
                carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        System.out.println("testing -- records_X_autoshare_bytype_GET");
        testResultDoc = (Document) chrome.records_X_autoshare_bytype_GET("http://indivo.org/vocab/xml/documents#Allergy", pagingOrderingQuery,
                recordId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n=================================================\n\n");

        System.out.println("testing -- carenets_X_apps_GET");
        testResultDoc = (Document) chrome.carenets_X_apps_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

//        if (skipnone)
        try {
        System.out.println("testing -- carenets_X_apps_XPUT");
        testResultDoc = (Document) chrome.carenets_X_apps_XPUT(carenetId, "allergies@apps.indivo.org", sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }


        System.out.println("testing -- carenets_X_apps_XDELETE");
        testResultDoc = (Document) chrome.carenets_X_apps_XDELETE(carenetId, "allergies@apps.indivo.org",
                 sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n===================================carenets_X_apps_XDELETE===============\n\n");

        System.out.println("testing -- carenets_X_accounts_GET");
        testResultDoc = (Document) chrome.carenets_X_accounts_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------------\n\n");

        System.out.println("testing -- carenets_X_accounts_POST");
        testResultDoc = (Document) chrome.carenets_X_accounts_POST(accountId, "true", carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------------\n\n");

        System.out.println("testing -- carenets_X_accounts_GET");
        testResultDoc = (Document) chrome.carenets_X_accounts_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------------\n\n");

        System.out.println("testing -- carenets_X_accounts_XDELETE");
        testResultDoc = (Document) chrome.carenets_X_accounts_XDELETE(carenetId, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n------------------------------------------------------\n\n");

        System.out.println("testing -- carenets_X_accounts_GET");
        testResultDoc = (Document) chrome.carenets_X_accounts_GET(pagingOrderingQuery, carenetId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- carenets_X_accounts_X_permissionsGET");
        testResultDoc = (Document) chrome.carenets_X_accounts_X_permissionsGET(carenetId, accountId, sessionToken, sessionTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
    }

    private List<String> listAllDocs(String recId, Rest chrome)
            throws IndivoClientException, XPathExpressionException {
        List<String> retVal = new ArrayList<String>();

        Document docsDoc = (Document) chrome.records_X_documents_GET(pagingOrderingQuery, recordId, sessionToken, sessionTokenSecret, options);

        NodeList allDocs = (NodeList) xpath.evaluate("/Documents/Document/@id", docsDoc, XPathConstants.NODESET);
        System.out.println("allDocs.getLength(): " + allDocs.getLength());

        for (int ii = 0; ii < allDocs.getLength(); ii++) {
            String idStr = ((Attr)allDocs.item(ii)).getValue();
            retVal.add(idStr);
        }
        return retVal;
    }

    private void shareAllDocs(String recId, List<String> docsList, String carenetId, Rest chrome)
            throws IndivoClientException {

        for (int ii = 0; ii < docsList.size(); ii++) {
            String idStr = docsList.get(ii);
            System.out.println("idStr: " + idStr);

            System.out.println("testing -- records_X_documents_X_carenets_XPUT");
            Document resltDoc = (Document) chrome.records_X_documents_X_carenets_XPUT(
                    recordId, idStr, carenetId, sessionToken, sessionTokenSecret, options);
            System.out.println("sharing -- record:" + recordId + " doc:" + idStr + "   " + Utils.domToString(resltDoc) + "\n\n");
        }



        System.out.println("\n+===================++++++++++++++++++\n\n");
    }
}
