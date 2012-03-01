package examples;

import org.indivo.client.Rest;
import org.w3c.dom.Document;


Rest(java.lang.String oauthConsumerKey, java.lang.String oauthConsumerSecret, java.lang.String baseURL, ResponseTypeConversion responseTypeConversion)

public class StandAloneExample {
	sampleContact = ""
	
	DefaultResponseTypeConversion rsc = new DefaultResponseTypeConversion();
	Rest rest = new Rest("sampleadmin_key", "sampleadmin_secret", "http://localhost:8080", rsc);
	
	Document recinfo = (Document) rest.records_POST(sampleContact, null);
	
	rest.records_X_apps_X_setupPOST(java.lang.String recordId,
            java.lang.String phaEmail,
            java.lang.Object body,
            java.lang.String requestContentType,
            java.util.Map<java.lang.String,java.lang.Object> options)
}
