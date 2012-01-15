package org.indivo.client;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.Random;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

import org.indivo.client.Rest;
import org.indivo.client.Utils;
import org.indivo.client.IndivoClientException;

import org.w3c.dom.*;


public class AdminTest {

    private static AdminTest instance = null;
    
    //private boolean skipnone = false;
    private Rest admin = null;
    private Rest chrome = null;

    private XPath xpath = null;

    private OutputStream forChrome = null;
    private OutputStream forPha = null;
    private OutputStream forDance = null;

//    boolean alreadyCreatedAccount = true;
    private String extrnlRandom = null;

  private AdminTest()
        throws IndivoClientException, IOException {

      System.out.println("Note: before each run of AdminTest, run <indivo_server>/utils/reset.sh -bs");
      System.err.println("Note: before each run of AdminTest, run <indivo_server>/utils/reset.sh -bs");

      admin = new Rest("sampleadmin_key", "sampleadmin_secret", "http://localhost:8000", null);
      xpath = XPathFactory.newInstance().newXPath();

      forChrome = new FileOutputStream("forChromeProperties.xml");
      forChrome.write(startProperties.getBytes());

      forPha = new FileOutputStream("forPhaProperties.xml");
      forPha.write(startProperties.getBytes());

      forDance = new FileOutputStream("forDance.txt");

      chrome = new Rest("chrome", "chrome", "http://localhost:8000", null);

      extrnlRandom = Integer.toString(new Random().nextInt(1000000));

  }



  public static void main(String[] args)
          throws IndivoClientException, javax.xml.xpath.XPathExpressionException,
          IOException {
    instance = new AdminTest();
    System.out.println("args.length: " + args.length);
    if (args.length > 0) { System.out.println("args[0]: " + args[0]); }
//    if (args.length > 0 && args[0].equals("db_cleared")) {
//        instance.alreadyCreatedAccount = false;
//    }
//    System.out.println("alreadyCreatedAccount: " + instance.alreadyCreatedAccount);
    instance.dotest();

    instance.forChrome.write("</properties>\n".getBytes());
    instance.forPha.write("</properties>\n".getBytes());
    instance.forChrome.close();
    instance.forPha.close();
    instance.forDance.close();
  }


    private String nodeString(Node node) {
        String withpre = Utils.domToString(node);
        return withpre.substring(withpre.indexOf("?>") +2).trim();
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


    private String startProperties = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
+ "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n"
+ "<properties version='1.0'>\n"
+ "<comment>some values created by the Admin test that are needed by the tests of another principal</comment>\n\n";
//"<entry key='surveyCollectorAppID'>survey_collector@apps.indivo.org</entry>
    private void dotest()
        throws org.indivo.client.IndivoClientException,
        javax.xml.xpath.XPathExpressionException,
        IOException {

// chrome(account_ruleset.is_owner) machineapp(machapp_ruleset.machineapp_record_created_rule)
// PUT /records/{record_id}/documents/special/demographics
    Document testResultDoc = null;

    String recordId = null;
    String appRecordId = null;
    String phaRecordId = null;
    String danceRecordId = null;
    String second_recordId = null;
    String accountId = "nate_a@indivo.org";
    String accountIdp = "nate_p@indivo.org";
    String accountIdPha = "nate_pha@indivo.org";
    String accountId_second = "nate_second@indivo.org";
    String accountId_third = "nate_third@indivo.org";
    String accountId_share = "nate_share@indivo.org";
    String documentId = null;
    String replacementId = null;
    String willBeReplacedId = null;
    String willBeReplaced_2_Id = null;
    String appId = "problems@apps.indivo.org";
    String allergyAppId = "allergies@apps.indivo.org";
    String adminAppId = "sample_admin_app@apps.indivo.org";
    String accessToken = null;
    String accessTokenSecret = null;
    Map<String,Object> options = null;

    String carenetIdWorkSchool = null;
    String carenetIdPhysicians = null;
    String carenetIdFamily = null;


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

    String demographicsDoc =
"<Demographics xmlns=\"Demographics\">"
+ "<dateOfBirth>2009-06-02T00:40:30+00:00</dateOfBirth>"
+ "</Demographics>";

    String testDoc = "<Testing123>strictly for testing</Testing123>";

    String testProcedure =
"<Procedure xmlns=\"http://indivo.org/vocab/xml/documents#\">" +
"<datePerformed>2009-05-16T12:01:00</datePerformed>" +
"<name type=\"http://codes.indivo.org/procedures#\" value=\"85\" abbrev=\"append\">Appendectomy</name>" +
"<provider><name>Kenneth Mandl</name><institution>Children's Hospital Boston</institution></provider>" +
"</Procedure>";

    String testProblem =
"<Problem xmlns=\"http://indivo.org/vocab/xml/documents#\">" +
"<dateOnset>2009-05-16T12:02:00Z</dateOnset>" +
"<dateResolution>2009-05-16T16:00:00Z</dateResolution>" +
"<name type=\"http://codes.indivo.org/problems/\" value=\"123\" abbrev=\"MI\">Myocardial Infarction</name>" +
"<comments>mild heart attack</comments>" +
"<diagnosedBy>Dr. Mandl</diagnosedBy>" +
"</Problem>";

    // machineapp: None
// POST /accounts/
        System.out.println("testing -- records_POST");
        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        recordId = xpath.evaluate("/Record/@id", testResultDoc);

        System.out.println("testing -- records_POST");
        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        second_recordId = xpath.evaluate("/Record/@id", testResultDoc);

        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        appRecordId = xpath.evaluate("/Record/@id", testResultDoc);


        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        phaRecordId = xpath.evaluate("/Record/@id", testResultDoc);
        String docId_pha_contact = xpath.evaluate("/Record/contact/@document_id", testResultDoc);
        System.out.println("\n=========phaRecordId: " + phaRecordId + "=========================================\n\n");

        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        danceRecordId = xpath.evaluate("/Record/@id", testResultDoc);

        testResultDoc = (Document) admin.accounts_POST(
                "dance.test@indivo.org", "dance.test@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);

        testResultDoc = (Document) admin.accounts_X_authsystems_POST(
                "password", "dance", "ABC", "dance.test@indivo.org", options);

        testResultDoc = (Document) admin.records_X_ownerPUT(
                danceRecordId, "dance.test@indivo.org", "text/plain", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        System.out.println("\n=========danceRecordId, for testing servlet: " + danceRecordId +
                "\n username, pass: " + "dance, ABC  =====================\n\n");

        forDance.write(("to test oauth dance: recordId=" +
                danceRecordId + ", account-user-name=dance, account-pass=ABC\n").getBytes());

        testResultDoc = (Document) admin.records_X_documents_special_demographicsPUT(
                phaRecordId, null, null, demographicsDoc, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String docId_pha_demographics = xpath.evaluate("/Document/@id", testResultDoc);
        forPha.write(("<entry key='demographics_docId'>" + docId_pha_demographics + "</entry>\n").getBytes());
        forPha.write(("<entry key='contact_docId'>" + docId_pha_contact + "</entry>\n").getBytes());


//        if (! alreadyCreatedAccount) {
        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                accountIdp, "nathan_p.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
        System.out.println("\np --------------------------------\n\n");

        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                accountIdPha, "nathan_pha.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
        System.out.println("\np --------------------------------\n\n");

        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                accountId_second, "nathan_second.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
        System.out.println("\np --------------------------------\n\n");


        System.out.println("testing -- accounts_X_authsystems_POST");
        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate_p", "pass", accountIdp, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate_pha", "pass", accountIdPha, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- accounts_X_authsystems_POST");
        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate_second", "pass", accountId_second, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

/*    if (skipnone) */
        try {
        System.out.println("accounts_X_primarySecretGET\n" +
            "GET /accounts/{account_id}/primary-secret");
        testResultDoc = (Document) admin.accounts_X_primarySecretGET(accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

        System.out.println("testing -- records_POST");
        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        recordId = xpath.evaluate("/Record/@id", testResultDoc);

        System.out.println("testing -- records_X_ownerPUT");
        testResultDoc = (Document) admin.records_X_ownerPUT(recordId, accountIdp, "text/plain", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        testResultDoc = (Document) admin.records_X_ownerPUT(appRecordId, accountIdp, "text/plain", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        System.out.println("testing -- records_X_ownerPUT");
        testResultDoc = (Document) admin.records_X_ownerPUT(
                second_recordId, accountId_second, "text/plain", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                accountId, "nathan_c.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- accounts_X_authsystems_POST");
        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate", "pass", accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");
//        }

        System.out.println("testing -- accounts_XGET");
        testResultDoc = (Document) admin.accounts_XGET(accountId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                accountId_third, "nathan_third.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
        System.out.println("\np --------------------------------\n\n");

        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                accountId_share, "nathan_share.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
        System.out.println("\np --------------------------------\n\n");

/*    if (skipnone)  */
        try {
        System.out.println("testing -- accounts_X_secretResendPOST");
        testResultDoc = (Document) admin.accounts_X_secretResendPOST(accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");


        System.out.println("testing -- accounts_X_resetPOST");
        testResultDoc = (Document) admin.accounts_X_resetPOST(accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

        System.out.println("testing -- accounts_X_authsystems_POST");
        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate_reset", "pass", accountId_third, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- accounts_X_authsystems_POST");
        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate_share", "pass", accountId_share, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("accounts_X_resetPOST\n" +
            "POST /accounts/{account_id}/reset");
        testResultDoc = (Document) admin.accounts_X_resetPOST(accountId_third, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        Map<String,String> testResultForm = null;
        String testResultFormAsString = null;

/*    if (skipnone) */
        try {
        testResultDoc = (Document) admin.accounts_X_authsystems_POST("password", "nate_reset", "pass_new", accountId_third, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - pass new- - - - - - - - - - -\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

        try {
        System.out.println("accounts_X_authsystems_password_setPOST\n" +
            "POST /accounts/{account_id}/authsystems/password/set");
        testResultDoc = (Document) admin.accounts_X_authsystems_password_setPOST("pass_new", "nate_reset", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

/*    if (skipnone)  */

        try {
        System.out.println("testing -- oauth_internal_session_createPOST");
        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST("nate_reset", "pass", null, null, options);
        throw new RuntimeException("expected use of old password to fail: " + testResultFormAsString);
        } catch (IndivoClientException iec) {
            System.out.println("session create failed using old password, try again with new password");
        }
        testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST("nate_reset", "pass_new", null, null, options);
        System.out.println("oauth_internal_session_createPOST  once");
        testResultForm =
                new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        Utils.printForm(testResultForm);

        System.out.println("testing -- accounts_XGET");
        testResultDoc = (Document) admin.accounts_XGET(accountId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }


        // needed for later pha test
        testResultDoc = (Document) admin.records_X_ownerPUT(phaRecordId, accountIdPha, "text/plain", options);
        System.out.println("for later Pha test: " + Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        forPha.write(("<entry key='account_password'>" + accountIdPha + ' ' + "nate_pha" + ' ' + "pass" + "</entry>\n").getBytes());

        forChrome.write(("<entry key='recordId'>" + recordId + "</entry>\n").getBytes());
        forChrome.write(("<entry key='second_recordId'>" + second_recordId + "</entry>\n").getBytes());
        forChrome.write(("<entry key='account_password'>" + accountIdp + ' ' + "nate_p" + ' ' + "pass" + "</entry>\n").getBytes());
        forChrome.write(("<entry key='second_account_password'>" + accountId_second + ' ' + "nate_second" + ' ' + "pass" + "</entry>\n").getBytes());

        System.out.println(recordId + "\n- - - - - - - - - - - - - - - - -\n\n");

/*        try {    */
        System.out.println("testing -- records_external_X_XPUT");
            testResultDoc = (Document) admin.records_external_X_XPUT(adminAppId, "externalId", contactDoc, "application/xml", options);
            System.out.println(Utils.domToString(testResultDoc) + "\n\n");
            System.out.println("\n==================================================\n\n");
/*        } catch (IndivoClientException ice) {
            throw ice;
        }
*/


        // this will fail on rerun due to null recordId, perhaps fix this at some point, or maybe never a reason to rerun
        System.out.println("testing -- records_X_documents_special_demographicsPUT");
        testResultDoc = (Document) admin.records_X_documents_special_demographicsPUT(
                recordId, null, null, demographicsDoc, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_special_demographicsGET");
        testResultDoc = (Document) admin.records_X_documents_special_demographicsGET(recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
    /* <user_app name='Problems' email='problems@apps.indivo.org'>
      <consumer_key>problems@apps.indivo.org</consumer_key>
      <secret>problems</secret>*/

        String contactDoc0 = contactDoc.replace("nameForTestDocument", "alteredName");
        System.out.println("testing -- records_X_documents_special_contactPUT");
        testResultDoc = (Document) admin.records_X_documents_special_contactPUT(
                recordId, accessToken, accessTokenSecret, contactDoc0, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_special_contactGET");
        testResultDoc = (Document) admin.records_X_documents_special_contactGET(recordId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("SETUP an APP");
        // chrome will try deleting this one
        System.out.println("testing -- records_X_apps_X_setupPOST");
        /*Map<String,String>*/ testResultForm = (Map<String,String>) admin.records_X_apps_X_setupPOST(
                appRecordId, appId, null, null, options);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        System.out.println("testing -- records_X_apps_X_setupPOST");
        forChrome.write(("<entry key='record_has_app'>" + appRecordId + " " + appId + "</entry>\n").getBytes());

        testResultDoc = (Document) admin.records_POST(contactDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n");
        String deleteAppRecordId = xpath.evaluate("/Record/@id", testResultDoc);

        testResultForm = (Map<String,String>) admin.records_X_apps_X_setupPOST(
                deleteAppRecordId, appId, null, null, options);

        testResultDoc = (Document) admin.records_X_shares_GET(null, deleteAppRecordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- shareing before delete of app- - - - - - - - - - - - - - - -\n\n");

        System.out.println("records_X_apps_XDELETE\n" +
            "DELETE /records/{record_id}/apps/{app_id}");
        testResultDoc = (Document) admin.records_X_apps_XDELETE(deleteAppRecordId, appId, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");

        testResultDoc = (Document) admin.records_X_shares_GET(null, deleteAppRecordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n====shareing after delete of app==============================================\n\n");

        testResultForm = (Map<String,String>) admin.records_X_apps_X_setupPOST(
                phaRecordId, allergyAppId, null, null, options);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        String phaToken = testResultForm.get("oauth_token");
        String phaSecret = testResultForm.get("oauth_token_secret");
        forPha.write(("<entry key='recordId_token'>" + phaRecordId + " " + phaToken + " " + phaSecret + "</entry>\n").getBytes());

        testResultDoc = (Document) admin.records_X_carenets_GET(null, phaRecordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String carenetIdWorkSchool_pha = xpath.evaluate("Carenets/Carenet[@name='Work/School']/@id", testResultDoc);
        String carenetIdPhysicians_pha = xpath.evaluate("Carenets/Carenet[@name='Physicians']/@id", testResultDoc);
        String carenetIdFamily_pha = xpath.evaluate("Carenets/Carenet[@name='Family']/@id", testResultDoc);
        forPha.write(("<entry key='carenets_work_school'>" + carenetIdWorkSchool_pha + "</entry>\n").getBytes());
        forPha.write(("<entry key='carenets_physicians'>" + carenetIdPhysicians_pha + "</entry>\n").getBytes());
        forPha.write(("<entry key='carenets_family'>" + carenetIdFamily_pha + "</entry>\n").getBytes());

        System.out.println("=======================================================================");


        System.out.println("Now a bunch of stuff just to test: POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set");
        testResultDoc = (Document) admin.records_X_documents_POST(
            deleteAppRecordId, accessToken, accessTokenSecret, testProblem, "application/xml", options);

        testResultDoc = (Document) admin.records_X_carenets_GET(null, deleteAppRecordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        String deleteAppWorkSchool = xpath.evaluate("Carenets/Carenet[@name='Work/School']/@id", testResultDoc);

        /*String*/ testResultFormAsString = (String)
                chrome.oauth_internal_session_createPOST("nate_share", "pass", null, null, options);
        System.out.println("oauth_internal_session_createPOST  once");
        testResultForm =
                new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
        String sessionToken = testResultForm.get("oauth_token");
        String sessionSecret = testResultForm.get("oauth_token_secret");

        System.out.println("testing -- records_X_ownerPUT");
        testResultDoc = (Document) admin.records_X_ownerPUT(
                deleteAppRecordId, accountId_share, "text/plain", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n--------------------------------------------------\n\n");

        testResultDoc = (Document) chrome.carenets_X_documents_GET(
                null, deleteAppWorkSchool, sessionToken, sessionSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n----before autoshare Allergy----------------------------------------------\n\n");

        System.out.println("records_X_autoshare_carenets_X_bytype_setPOST\n" +
            "POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set");
        testResultDoc = (Document) admin.records_X_autoshare_carenets_X_bytype_setPOST(
                "http://indivo.org/vocab/xml/documents#Problem",
                recordId, deleteAppWorkSchool, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");

        testResultDoc = (Document) chrome.carenets_X_documents_GET(
                null, deleteAppWorkSchool, sessionToken, sessionSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        
        System.out.println("\n==End test of: POST /records/{record_id}/autoshare/carenets/{carenet_id}/bytype/set ================================================\n\n");




        String apptoken = null;
        String appSecret = null;
        Rest pha = new Rest(appId, "problems", "http://localhost:8000", null);
 /*   if (skipnone) */
        try {
        System.out.println("SETUP an APP");
        System.out.println("testing -- records_X_apps_X_setupPOST");
        testResultForm = (Map<String,String>) admin.records_X_apps_X_setupPOST(
                recordId, appId, null, null, options);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");

        // chrome will try deleting this one
        System.out.println("testing -- records_X_apps_X_setupPOST");
        testResultForm = (Map<String,String>) admin.records_X_apps_X_setupPOST(
                second_recordId, appId, null, null, options);
        System.out.println(Utils.printForm(testResultForm) + "\n\n");

        apptoken = testResultForm.get("oauth_token");
        appSecret = testResultForm.get("oauth_token_secret");
        System.out.println("authorized: oauth_token: " + testResultForm.get("oauth_token")
                + ", oauth_token_secret: " + testResultForm.get("oauth_token_secret") + " for"
                + " recordId: " + recordId);
        System.out.println("\n\n- - - - - - - - - - - - - - - - - - - -\n\n");

        testResultDoc = (Document) pha.records_X_documents_GET(null, recordId, apptoken, appSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n\n- - - - - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- records_X_apps_XDELETE");
        testResultDoc = (Document) admin.records_X_apps_XDELETE(recordId, appId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n\n- - - - - - - - - - - - - - - - - - - -\n\n");

        try {
        System.out.println("Should fail after app delete");
        testResultDoc = (Document) pha.records_X_documents_GET(null, recordId, apptoken, appSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\nShould have failed after app delete\n\n");
        } catch (IndivoClientException ice) {
            System.out.println(ice.getMessage());
        }
        System.out.println("\n\n- - - - - - - - - - - - - - - - - - - -\n\n");


        System.out.println("testing -- records_X_apps_X_setupPOST");
        testResultForm = (Map<String,String>) admin.records_X_apps_X_setupPOST(
                recordId, appId, null, null, options);
        apptoken = testResultForm.get("oauth_token");
        appSecret = testResultForm.get("oauth_token_secret");
        forPha.write(("<entry key='pha_token_secret'>" + apptoken + ' ' + appSecret + "</entry>\n").getBytes());
        System.out.println(Utils.printForm(testResultForm) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            reportKnownError(ice);
        }

        System.out.println("testing -- records_X_documents_POST");
        testResultDoc = (Document) admin.records_X_documents_POST(
                recordId, accessToken, accessTokenSecret, testDoc, "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        willBeReplacedId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_documents_X_replacePOST");
        testResultDoc = (Document) admin.records_X_documents_X_replacePOST(
                recordId, willBeReplacedId, accessToken, accessTokenSecret,
                testDoc.replace("testing", "testing_replace"), "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        replacementId = xpath.evaluate("/Document/@id", testResultDoc);
        System.out.println("\n==================================================\n\n");

        System.out.println("TESTING SHARING");
//        if (! alreadyCreatedAccount) {
        System.out.println("testing -- accounts_POST");
        testResultDoc = (Document) admin.accounts_POST(
                "nate_share@indivo.org", "nathan.finstein@childrens.harvard.edu",
                "Nathan Finstein", "0", "0", null);
        System.out.println(nodeString(testResultDoc));
//        }
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- records_X_shares_POST");
        testResultDoc = (Document) admin.records_X_shares_POST("nate_share@indivo.org", "parent", recordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- records_X_shares_GET");
        testResultDoc = (Document) admin.records_X_shares_GET(null, recordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- records_X_shares_X_deletePOST");
        testResultDoc = (Document) admin.records_X_shares_X_deletePOST(recordId, "nate_share@indivo.org", null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- records_X_shares_GET");
        testResultDoc = (Document) admin.records_X_shares_GET(null, recordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("testing -- records_X_carenets_GET");
        testResultDoc = (Document) admin.records_X_carenets_GET(null, recordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        carenetIdWorkSchool = xpath.evaluate("Carenets/Carenet[@name='Work/School']/@id", testResultDoc);
        carenetIdPhysicians = xpath.evaluate("Carenets/Carenet[@name='Physicians']/@id", testResultDoc);
        carenetIdFamily = xpath.evaluate("Carenets/Carenet[@name='Family']/@id", testResultDoc);
        forChrome.write(("<entry key='carenets_work_school'>" + carenetIdWorkSchool + "</entry>\n").getBytes());
        forChrome.write(("<entry key='carenets_physicians'>" + carenetIdPhysicians + "</entry>\n").getBytes());
        forChrome.write(("<entry key='carenets_family'>" + carenetIdFamily + "</entry>\n").getBytes());
        System.out.println("work_school, pyhsicians, family: " + carenetIdWorkSchool + ", "
                + carenetIdPhysicians + ", " + carenetIdFamily);
        System.out.println("\n==================================================\n\n");


        // This is probably really an account only action
////        System.out.println("testing -- records_X_autoshare_carenets_X_bytype_setPOST");
//        testResultDoc = (Document) admin.records_X_autoshare_carenets_X_bytype_setPOST(
//                type, recordId, carenetId, accessToken, accessTokenSecret, options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- records_X_notifyPOST");
        testResultDoc = (Document) admin.records_X_notifyPOST("Hey you!", null, null, recordId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("testing -- accounts_searchGET");
        testResultDoc = (Document) admin.accounts_searchGET("Nathan Finstein", null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n");
        System.out.println("testing -- accounts_searchGET");
        testResultDoc = (Document) admin.accounts_searchGET(null, "nathan_c.finstein@childrens.harvard.edu", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n");
        System.out.println("testing -- accounts_searchGET");
        testResultDoc = (Document) admin.accounts_searchGET("Nathan Finstein", "nathan_c.finstein@childrens.hharvard.edu", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

System.out.println("TEST SET STATE");
        System.out.println("testing -- accounts_XGET");
        testResultDoc = (Document) admin.accounts_XGET(accountId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- accounts_X_setStatePOST");
        testResultDoc = (Document) admin.accounts_X_setStatePOST("disabled", accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n");

        System.out.println("testing -- accounts_XGET");
        testResultDoc = (Document) admin.accounts_XGET(accountId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n\n");

        System.out.println("testing -- accounts_X_setStatePOST");
        testResultDoc = (Document) admin.accounts_X_setStatePOST("active", accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n- - - - - - - - - - - - - - - - -\n");

        System.out.println("testing -- accounts_XGET");
        testResultDoc = (Document) admin.accounts_XGET(accountId, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        testResultDoc = (Document) admin.records_X_documents_POST(
                recordId, accessToken, accessTokenSecret, testDoc.replace("testing", "testing replace external"),
                "application/xml", options);
        willBeReplaced_2_Id = xpath.evaluate("/Document/@id", testResultDoc);

        System.out.println("records_X_documents_X_replace_external_X_XPUT\n" +
            "PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}");
        testResultDoc = (Document) admin.records_X_documents_X_replace_external_X_XPUT(
                recordId, willBeReplaced_2_Id, appId, "external_replaced_" + extrnlRandom,
                accessToken, accessTokenSecret, testDoc.replace("testing", "testing replace external replacement"),
                "application/xml", options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


 /*   if (skipnone) */
        try {
        System.out.println("carenets_X_documents_special_contactGET\n" +
            "GET /carenets/{carenet_id}/documents/special/contact");
        testResultDoc = (Document) admin.carenets_X_documents_special_contactGET(
                carenetIdPhysicians, accessToken, accessTokenSecret, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");

        System.out.println("carenets_X_documents_special_demographicsGET\n" +
            "GET /carenets/{carenet_id}/documents/special/demographics");
        testResultDoc = (Document) admin.carenets_X_documents_special_demographicsGET(
                carenetIdPhysicians, null, null, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


        System.out.println("apps_X_documents_GET\n" +
            "GET /apps/{app_id}/documents/");
        testResultDoc = (Document) admin.apps_X_documents_GET(null, adminAppId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");
        } catch (IndivoClientException ice) {
            System.out.println(ice.getMessage() + "\n\n");
        }


        System.out.println("accounts_X_secretResendPOST\n" +
            "POST /accounts/{account_id}/secret-resend");
        testResultDoc = (Document) admin.accounts_X_secretResendPOST(accountId, options);
        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
        System.out.println("\n==================================================\n\n");


//        chrome = new Rest("chrome", "chrome", "http://localhost:8000", null);
//        String testResultFormAsString = (String)
//                chrome.oauth_internal_session_createPOST("nate", "pass", null, null, options);
//        testResultForm = new DefaultResponseTypeConversion().mapFromFormEncodedString(testResultFormAsString);
//        System.out.println(Utils.printForm(testResultForm) + "\n\n");
//
//        System.out.println("authorized: oauth_token: " + testResultForm.get("oauth_token")
//                + ", oauth_token_secret: " + testResultForm.get("oauth_token_secret") + " for"
//                + " recordId: " + recordId);

//        testResultDoc = (Document) chrome.carenets_X_apps_XPUT(carenetIdWorkSchool, "sample_admin_app@apps.indivo.org",
//                testResultForm.get("oauth_token"), testResultForm.get("oauth_token_secret"), options);
//        System.out.println(Utils.domToString(testResultDoc) + "\n\n");
//        System.out.println("\n==================================================\n\n");

//System.out.println("MACHINE_APP -- RECORD SPECIFIC APP SPECIFIC");



  }

}

