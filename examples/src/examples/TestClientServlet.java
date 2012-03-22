package examples;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.indivo.client.DefaultResponseTypeConversion;
import org.indivo.client.Rest;
import org.indivo.client.IndivoClientException;

/**
*/
public class TestClientServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Log logger = null;

    private String consumerKey;
    private String consumerSecret;
    private Rest oauthPha;
    private String indivoAPI_URL;
    private String indivoUI_URL;
    
    private String authStartPath;
    private String authAfterPath;

    private Map<String,String> savedSecrets;


    @Override
    public void init() throws ServletException {
        logger = LogFactory.getLog(this.getClass());
        
        indivoAPI_URL = getInitParameter("indivoAPI_URL");
        indivoUI_URL = getInitParameter("indivoUI_URL");
        
        authStartPath = getInitParameter("authStartPath");
        authAfterPath = getInitParameter("authAfterPath");
        consumerKey = getInitParameter("consumerKey");
        consumerSecret = getInitParameter("consumerSecret");
        logger.info("indivoAPI_URL: " + indivoAPI_URL +
        "    indivoUI_URL: " + indivoUI_URL +
        "    authAfterPath: " + authAfterPath +
        "    consumerKey: " + consumerKey +
        "    consumerSecret: " +consumerSecret);

        savedSecrets = new HashMap<String,String>();
        try {
            oauthPha = new Rest(consumerKey, consumerSecret, indivoAPI_URL, null, null);
        } catch (IndivoClientException ice) {
            throw new ServletException(ice);
        }
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        logger.info("GET request: " + req.getRequestURL().toString() + " ? " + req.getQueryString());
        resp.setContentType("text/html;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        Map<String,String[]> paramMap = req.getParameterMap();

        if (pathInfo == null) {
            logger.info("getLocalAddr(): " + req.getLocalAddr() + "    " +
                    "getContextPath(): " + req.getContextPath() + "    " +
                    "getServletPath(): " + req.getServletPath() + "   pathInfo: " + pathInfo);
            String response = "<html><head><title></title></head><body>"
+ "<p>This is a test of the oauth ''dance''.  If you have not already run "
+ "&lt;project_directory&gt;src/test/sh/testAdmin.sh do so now.  testAdmin.sh will "
+ "output file: forDance.txt.  Copy the recordId from forDance.txt into the input field below.</p>"
+ "<p>There must be a running Indivo server and a running Indivo UI server to run this test. "
+ "The URL-s for indivo server and indivo UI must have been correctly entered into the web.xml of "
+ "this servlet (prior to servlet initialization).</p>"
+ "<p>Unless you are already logged in as \"dance\", you will be prompted to login.  Log in as "
+ "<strong>dance</strong> with password <strong>ABC</strong></p>"
+ "<p>if you get \"bad response to claim_request_token\" in your browser, it may be that "
+ "you <code>&lt;indivo_server&gt;reset.sh -bs</code> while logged in as \"dance\". "
+ "Navigate this browser to Indivo UI, logout,"
+ " then navigate back here to this servlet (.../IndivoClientJavaOAuthTester).</p>"
+ "<strong>Start the oauth dance ...</strong>"
+ "<form action=\"http://" + req.getLocalAddr() + ":" + req.getLocalPort()
    + req.getContextPath() + req.getServletPath() + "/" + authStartPath + "\">"
+ "recordId: <input type=\"text\" name=\"indivo_record_id\"></input><br/>"
+ "<input type=\"submit\"></input>"
+ "</body></html>";
            try {
                ServletOutputStream respOut = resp.getOutputStream();
                respOut.println(response);
            } catch (IOException ioe) { throw new ServletException(ioe); }

        }
        else if(pathInfo.equals(authStartPath)
                || (pathInfo.charAt(0) == '/' && pathInfo.substring(1).equals(authStartPath)) ) {   //"/auth/start"
            logger.info("in authStartPath: " + authStartPath);

            String indivoRecordId = getStringFromPmap(paramMap, "indivo_record_id");
            String callbackURL = "http://" +
                    req.getLocalAddr() + ":" + req.getLocalPort() + req.getContextPath() +
                    req.getServletPath() + "/" + authAfterPath;
            Map<String,String> requestTokenSecret = getRequestToken(indivoRecordId, callbackURL);

            String oauth_callback_confirmed = requestTokenSecret.get("oauth_callback_confirmed");
            if (oauth_callback_confirmed == null) {
                logger.warn("oauth_callback_confirmed not present along with request token");
            }
            else if (! oauth_callback_confirmed.equals("true")) {
                throw new ServletException("oauth_callback_confirmed=" + oauth_callback_confirmed
                        + " request_token:" +  requestTokenSecret.get("oauth_token"));
            }

            logger.info("about to put " + requestTokenSecret.get("oauth_token") + " -- "
                    + requestTokenSecret.get("oauth_token_secret"));
            
            savedSecrets.put(
                    requestTokenSecret.get("oauth_token"),
                    requestTokenSecret.get("oauth_token_secret")
            );
            
            try {
                //User Authorization URL: https://INDIVO_SERVER/oauth/authorize
                logger.warn("about to redirect browser: \n" + resp.encodeRedirectURL(
                        indivoUI_URL + "/oauth/authorize?oauth_token="  + requestTokenSecret.get("oauth_token") ));
                resp.sendRedirect(resp.encodeRedirectURL(
                        indivoUI_URL + "/oauth/authorize?oauth_token="
                        + requestTokenSecret.get("oauth_token") ));
            } catch (IOException ioe) {
                throw new ServletException(ioe);
            }
            
            String response = "<html><head><title></title></head><body>"
                    + "<p>If not promptly redirected to " + indivoUI_URL
                    + "get help from your Indivo installation.</p>"
                    + "</body></html>";
            try {
                ServletOutputStream respOut = resp.getOutputStream();
                respOut.println(response);
            } catch (IOException ioe) { throw new ServletException(ioe); }
        }

        // request token authorization is complete (either success or failure)
        else if (pathInfo.equals(authAfterPath)
                || (pathInfo.charAt(0) == '/' && pathInfo.substring(1).equals(authAfterPath)) ) {   //"/auth/after"
            String token = getStringFromPmap(paramMap, "oauth_token");
            String verifier = getStringFromPmap(paramMap, "oauth_verifier");
            String savedSecret = savedSecrets.remove(token);
            
            // this map DOES have recordID (along with authorized token and secret) if user in fact authorized
            // when user has authorized, recordId may be given out
            Map<String,String> authorizedTokenSecret = getAuthorizedToken(token, savedSecret, verifier);

            String response = "<html><head><title></title></head><body>"
+ "<p>Authorization is complete<br/>The below token and secret values "
+ "can be used by an app which uses<br/>consumer_key: <code>" + consumerKey + "</code><br/>"
+ "with secret: <code>" + consumerSecret + "</code><br/>"
+ "to access record: <code>"
+ authorizedTokenSecret.get("xoauth_indivo_record_id") + "</code></p>"
+ "<p><strong>token: </strong><span style='background-color:#ccffff'>"
+ authorizedTokenSecret.get("oauth_token") + "</span><br/>"
+ "<strong>secret: </strong><span style='background-color:#ccffff'>"
+ authorizedTokenSecret.get("oauth_token_secret") + "</span><br/>"
+ "<strong>authorized for recordId: </strong><span style='background-color:#ccffff'>"
+ authorizedTokenSecret.get("xoauth_indivo_record_id") + "</span></p>"
+ "</body></html>";
            try {
                ServletOutputStream respOut = resp.getOutputStream();
                respOut.println(response);
            } catch (IOException ioe) { throw new ServletException(ioe); }
        }

        else {
            throw new ServletException("unexpected path info: " + pathInfo
                    + ".  must match value of match web.xml init-param named authStartPath:  " + authStartPath
                    + " or value of match web.xml init-param named authAfterPath:" + authAfterPath
                    + " or ''/runPhaExample''");
        }
    }

    private Map<String,String> getRequestToken(String indivoRecordId, String callbackURL) throws ServletException {
        Map<String,String> tokenSecret = null;
        try {
//            tokenSecret = (Map<String,String>) oauthPha.oauth_request_tokenPOST("oob", indivoRecordId, null);
            tokenSecret = (Map<String,String>) oauthPha.oauth_request_tokenPOST(callbackURL, indivoRecordId, null);
            StringBuffer postResult = new StringBuffer();
            Iterator<String> rti = tokenSecret.keySet().iterator();
            while (rti.hasNext()) {
                String rtiK = rti.next();
                postResult.append(rtiK + ": " + tokenSecret.get(rtiK) + "   ");
            }
            logger.info("result of oauth_request_tokenPOST: " + postResult);
        } catch (IndivoClientException ice) {
            throw new ServletException(ice);
        }
        
        return tokenSecret;
    }

    private Map<String,String> getAuthorizedToken(String requestToken, String requestSecret, String verifier)
            throws ServletException {
        Map<String,String> tokenSecret = null; 
        try {
            tokenSecret = (Map<String,String>) oauthPha.oauth_access_tokenPOST(
                    requestToken, requestSecret, verifier);
        } catch (IndivoClientException ice) {
            throw new ServletException(ice);
        }
        return tokenSecret;
    }

    private String getStringFromPmap(Map<String,String[]> pmap, String key)
           throws ServletException {
        String[] val = pmap.get(key);
        if (val == null) {
            throw new ServletException("pmap missing key: " + key);
        } else if (val.length > 1) {
            throw new ServletException("pmap value array length > 1 for key: " + key);
        }

        if (val.length == 0) {
            return null;
        } else {
            return val[0];
        }
    }


}
