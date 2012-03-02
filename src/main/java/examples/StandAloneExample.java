package examples;

import org.indivo.client.Rest;
import org.indivo.client.IndivoClientException;
import org.indivo.client.DefaultResponseTypeConversion;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;


public class StandAloneExample {
    private String sampleContact =
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

    private static String allergyDoc = 
"<Allergy xmlns=\"http://indivo.org/vocab/xml/documents#\">"
+ "\n  <dateDiagnosed>2009-05-16</dateDiagnosed>"
+ "\n  <diagnosedBy>Children's Hospital Boston</diagnosedBy>"
+ "\n  <allergen>"
+ "\n    <type type=\"http://codes.indivo.org/codes/allergentypes/\" value=\"drugs\">Drugs</type>"
+ "\n    <name type=\"http://codes.indivo.org/codes/allergens/\" value=\"penicillin\">Penicillin</name>"
+ "\n  </allergen>"
+ "\n  <reaction>blue rash</reaction>"
+ "\n  <specifics>this only happens on weekends</specifics>"
+ "\n</Allergy>";


    private static StandAloneExample instance = null;

	private DefaultResponseTypeConversion rsc = null;
	private XPath xpath = null;
	
	public static void main(String[] args) throws IndivoClientException, XPathExpressionException {
		instance = new StandAloneExample();
        instance.runExample();
    }

    private StandAloneExample() throws IndivoClientException {
        xpath = XPathFactory.newInstance().newXPath();
        rsc = new DefaultResponseTypeConversion();
    }

    private void runExample() throws IndivoClientException, XPathExpressionException {
        DefaultResponseTypeConversion rsc = new DefaultResponseTypeConversion();
        Rest adminRest = new Rest("sampleadmin_key", "sampleadmin_secret", "http://localhost:8000", rsc);
    	Rest phaRest = new Rest("hospital-connector", "hospital-connector-secret", "http://localhost:8000", null);	

        Document recinfo = (Document) adminRest.records_POST(sampleContact, null);
	    String recordId = xpath.evaluate("/Record/@id", recinfo);

        Map<String,String> setupResult = (Map<String,String>) adminRest.records_X_apps_X_setupPOST(recordId,
                "indivoconnector@apps.indivo.org",
                null,   // http request body not required by setup
                null,   // no body format
                null    /*not options in this example*/);  

        if (! recordId.equals(setupResult.get("xoauth_indivo_record_id"))) {
            throw new RuntimeException("expected record id from setup of: " + recordId + ", got: " + setupResult.get("xoauth_indivo_record_id"));
        }
        String token = setupResult.get("oauth_token");
        String secret = setupResult.get("oauth_token_secret");



        Document retdoc = (Document) phaRest.records_X_documents_POST(
	                recordId, token, secret, allergyDoc, "application/xml", null);
	    String newDocId = xpath.evaluate("/Document/@id", retdoc);
	    System.out.println("new doc id: " + newDocId);
	    System.out.println(phaRest.getUtils().domToString(retdoc) + "\n\n\n");


        /* We know there is less than 7, but just for example this is how to read metadata of the first 7 */
        Document docslist = (Document) phaRest.records_X_documents_GET(
                "limit=7&offset=0", recordId, token, secret, null);
        System.out.println(phaRest.getUtils().domToString(docslist) + "\n\n\n");

    }

}
