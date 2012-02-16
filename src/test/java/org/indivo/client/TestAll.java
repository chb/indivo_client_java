package org.indivo.client;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.indivo.client.IndivoClientException;


public class TestAll {
	
/*Rest(java.lang.String oauthConsumerKey, java.lang.String oauthConsumerSecret, java.lang.String baseURL, ResponseTypeConversion responseTypeConversion)*/
	
	private static String contactDoc = 
"<Contact xmlns=\"http://indivo.org/vocab/xml/documents#\">"
+ "\n    <name>"
+ "\n        <fullName>Sebastian Rockwell Cotour</fullName>"
+ "\n        <givenName>Sebastian</givenName>"
+ "\n        <familyName>Cotour</familyName>"
+ "\n    </name>"
+ "\n    <email type=\"personal\">"
+ "\n        scotour@hotmail.com"
+ "\n    </email>"
+ "\n    <email type=\"work\">"
+ "\n        sebastian.cotour@childrens.harvard.edu"
+ "\n    </email>"
+ "\n    <address type=\"home\">"
+ "\n        <streetAddress>15 Waterhill Ct.</streetAddress>"
+ "\n        <postalCode>53326</postalCode>"
+ "\n        <locality>New Brinswick</locality>"
+ "\n        <region>Montana</region>"
+ "\n        <country>US</country>"
+ "\n        <timeZone>-7GMT</timeZone>"
+ "\n    </address>"
+ "\n    <location type=\"home\">"
+ "\n        <latitude>47N</latitude>"
+ "\n        <longitude>110W</longitude>"
+ "\n    </location>"
+ "\n    <phoneNumber type=\"home\">5212532532</phoneNumber>"
+ "\n    <phoneNumber type=\"work\">6217233734</phoneNumber>"
+ "\n    <instantMessengerName protocol=\"aim\">scotour</instantMessengerName>"
+ "\n</Contact>";
	
        private Log logger = null;

	private XPath xpath = null;
	private Rest adminRest = null;
	private Rest allergyRest = null;
	
	private String recid = null;
	private String token = null;
	private String secret = null;

    public static void main(String[] args) throws IndivoClientException {
    	TestAll instance = new TestAll();
        try {
        	List<String> recTokSec = instance.setup();
        	instance.testopts(recTokSec);
System.exit(0);
        	
        	recTokSec = instance.setup();
        	instance.recid = recTokSec.get(0);  instance.token = recTokSec.get(1);  instance.secret = recTokSec.get(2);
            instance.testapps();
            instance.testrecords_a();
            instance.testrecordapp();
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
    }
    
    private TestAll() throws IndivoClientException {
        logger = LogFactory.getLog(this.getClass());

    	xpath = XPathFactory.newInstance().newXPath();
    	
    	adminRest = new Rest("sampleadmin_key", "sampleadmin_secret", "http://localhost:8000", null);
    	allergyRest = new Rest("hospital-connector", "hospital-connector-secret", "http://localhost:8000", null);	
    }
    
    private void testopts(List<String> recTokSec) throws IndivoClientException, XPathExpressionException {
    	String recid_b = recTokSec.get(0);  String token_b = recTokSec.get(1);  String secret_b = recTokSec.get(2);
        logger.info("\n\n\nin testopts");

    	for (int ii = 0; ii < 100; ii++) {
	    	allergyRest.records_X_documents_POST(
	                recid_b, token_b, secret_b,
	                "<testopts>" + (ii +1) + "</testopts>", "application/xml", null);
    	}
        logger.info("in testopts done adding 100 docs");
    	
    	int number = -1;
    	int numbertot = 0;
    	while (number != 0) {
			Document docs = (Document) allergyRest.records_X_documents_GET(
					"limit=15&offset=" + numbertot, recid_b, token_b, secret_b, null);
                        logger.info("in testopts: " + allergyRest.getUtils().domToString(docs));
			NodeList docsnl = (NodeList) xpath.evaluate("/Documents/Document", docs, XPathConstants.NODESET);
			number = docsnl.getLength();
			if ((numbertot == 90 && number != 10) || (number != 15)) {
				System.out.println("Number docs retrived, up to 15 expected.  prior, now: " + numbertot + ", " + number);
				numbertot += number;
			}
    	}
    	if (numbertot != 100) {
    		throw new RuntimeException("expected 100 total, got: " + numbertot);
    	}
        logger.info("in testopts, total: " + numbertot);
    }
    
    private void testrecordapp() throws IndivoClientException {
    	Document retdoc = (Document) allergyRest.records_X_apps_X_documents_POST(recid, "indivoconnector@apps.indivo.org",
    			token, secret, "<app_rec_specific>app rec specific</app_rec_specific>",
    			"application/xml", null);
	    System.out.println("records_X_apps_X_documents_POST");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.records_X_apps_X_documents_external_XPUT(
	            recid, "indivoconnector@apps.indivo.org", "externalId_rec_app_specific",
	            token, secret, "<app_rec_specific>app rec specific externalId_rec_app_specific</app_rec_specific>",
	            "application/xml", null);
	    System.out.println("records_X_apps_X_documents_external_XPUT");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.records_X_apps_X_documents_external_X_metaGET(
	    	     recid, "indivoconnector@apps.indivo.org", "externalId_rec_app_specific",token, secret, null);
	    System.out.println("records_X_apps_X_documents_external_X_metaGET");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");
    }
    
    private void testrecords_a() throws IndivoClientException {
    	
    	Document retdoc = (Document) adminRest.records_external_X_XPUT(
    			"sample_admin_app@apps.indivo.org", "externalId__indivoconnector_external", contactDoc, null);
	    System.out.println("records_external_X_XPUT");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
    	
	    // two legged flavor
	    retdoc = (Document) adminRest.records_XGET(recid, null, null, null);
	    System.out.println("admin records_XGET");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
            try {
	    System.out.println("records_XGET" + " token, secret: " + token + ", " + secret);
	    retdoc = (Document) allergyRest.records_XGET(recid, token, secret, null);
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
            } catch (IndivoClientException ice) {
                System.out.println("not sure why this failed, 3 legged read should work: " + ice.getMessage());
            }
	    
	    // two legged flavor
	    retdoc = (Document) adminRest.records_X_apps_GET(
	    		null, recid, null, null, null);
	    System.out.println("records_X_apps_GET");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.records_X_apps_XGET(
	    		recid, "indivoconnector@apps.indivo.org", null, null, null);
	    System.out.println("records_X_apps_XGET");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    
    }

    private List<String> setup() throws IndivoClientException, XPathExpressionException {
    	
	    Document retdoc = (Document) adminRest.records_POST(contactDoc, null);
	    String recid_a = xpath.evaluate("/Record/@id", retdoc);
	    System.out.println("record id: " + recid_a);
	    
	    Map<String,String> setupres = (Map<String,String>)
	    		adminRest.records_X_apps_X_setupPOST(recid_a, "indivoconnector@apps.indivo.org", null, null, null);
	    System.out.println("setup result: " + setupres.getClass().getName());
	    
	    String token_a = setupres.get("oauth_token");
	    String secret_a = setupres.get("oauth_token_secret");
	    String surecid = setupres.get("xoauth_indivo_record_id");
	    assert surecid.equals(recid_a);
	    System.out.println("recid: " + recid_a + "  -- token, secret: " + token_a + "  " + secret_a);
	    
	    return Arrays.asList(recid_a, token_a, secret_a);
    }
    
    private void testapps() throws IndivoClientException, XPathExpressionException {
/*Rest(java.lang.String oauthConsumerKey, java.lang.String oauthConsumerSecret, java.lang.String baseURL, ResponseTypeConversion responseTypeConversion)*/
	    Document retdoc = (Document) adminRest.apps_GET(null);
	    System.out.println("apps_GET(_)");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");

//Object apps_XDELETE()
	    
	    retdoc = (Document) adminRest.apps_XGET("indivoconnector@apps.indivo.org", null);
	    System.out.println("apps_XGET(_,_)");
	    System.out.println(adminRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
// Object apps_X_documents_GET()
	    
	    retdoc = (Document) allergyRest.apps_X_documents_POST("indivoconnector@apps.indivo.org", "<allergyappdoc>allergyappdoc</allergyappdoc>",
	    		"application/xml", null);
	    System.out.println("apps_X_documents_POST(_,_,_,_)");
	    String appSpecificId = xpath.evaluate("/Document/@id", retdoc); //<Document id="14c81023-c84f-496d-8b8e-9438280441d3" 
	    System.out.println("new doc id: " + appSpecificId);
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_external_XPUT("indivoconnector@apps.indivo.org",
                "externalId__apps_X_documents_external_XPUT",
                "<allergyappdoc>externalId__apps_X_documents_external_XPUT</allergyappdoc>", "application/xml", null);
	    System.out.println("apps_X_documents_external_XPUT()");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_external_X_metaGET(
	    		"indivoconnector@apps.indivo.org","externalId__apps_X_documents_external_XPUT", null);
	    System.out.println("apps_X_documents_external_X_metaGET()");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_XGET(
	    		"indivoconnector@apps.indivo.org", appSpecificId, "application/xml", null);
	    System.out.println("allergyRest.apps_X_documents_XGET");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_XPUT(
	    		"indivoconnector@apps.indivo.org", appSpecificId,
	    		"<allergyappdoc>allergyappdoc_replacement</allergyappdoc>", "application/xml", null);
	    System.out.println("apps_X_documents_XPUT");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_XGET(
	    		"indivoconnector@apps.indivo.org", appSpecificId, "application/xml", null);
	    System.out.println("after replacement of " + appSpecificId);
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_X_labelPUT(
	    		"indivoconnector@apps.indivo.org", appSpecificId, "test_label", null);
	    System.out.println("apps_X_documents_X_labelPUT");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");

	    retdoc = (Document) allergyRest.apps_X_documents_X_metaGET(
	            "indivoconnector@apps.indivo.org", appSpecificId, null);
	    System.out.println("apps_X_documents_X_metaGET");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    
	    retdoc = (Document) allergyRest.apps_X_documents_XDELETE(
	    		"indivoconnector@apps.indivo.org", appSpecificId, null);
	    System.out.println("allergyApp.apps_X_documents_XDELETE");
	    System.out.println(allergyRest.getUtils().domToString(retdoc) + "\n\n\n");
	    assert retdoc.getDocumentElement().getTagName() == "ok";
	    
	    try {
	    	retdoc = (Document) allergyRest.apps_X_documents_XGET(
	    			"indivoconnector@apps.indivo.org", appSpecificId, "application/xml", null);
	    	throw new RuntimeException("document should have been deleted: " + allergyRest.getUtils().domToString(retdoc));
	    } catch (IndivoClientException ice) {
		    System.out.println(ice.getMessage());
	    }
	    
	    
    }
}
