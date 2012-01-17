/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//testing origin
package org.indivo.client.codegen;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


/**
 *
 * @author nate
 */
public class GenClientFromWadl {

    private DocumentBuilder documentBuilder = null;
    private Document wadl = null;
    private XPath xpath = null;

    private InputStream clientShell = null;

    private OutputStream clientJava = null;
    private OutputStream testerPhaJava = null;
    private OutputStream testerAdminJava = null;
    private OutputStream testerChromeJava = null;
    private OutputStream testerAppSpecificJava = null;
    //private GenClient genClient = null;

    private GenClientUtils genClientUtils = null;

    private boolean carenetsMinimal = false;
    private boolean recordsMinimal = false;
    private List<String> once = null;

    private JsonObject grantsToLegged = null;

    private Map<String,List<String>> exceptions = null;
    String[][] exceptionsArr = {
        //records_X_documents_X_replace_external_X_XPUT
        {"PUT /records/{record_id}/documents/{document_id}/label", "send_textPlain"},
        {"PUT /records/{record_id}/documents/external/{app_id}/{external_id}/label", "send_textPlain"},
        {"PUT /apps/{app_id}/documents/{document_id}/label","send_textPlain"},
        {"PUT /records/{record_id}/apps/{app_id}/documents/{document_id}/label","send_textPlain"},
        //{"PUT /records/{record_id}/documents/{document_id}/replace/external/{app_id}/{external_id}","noBody"},

        {"PUT /records/{record_id}/documents/special/demographics","send_xml"},
        {"PUT /records/{record_id}/documents/special/contact","send_xml"},

        {"GET /records/{record_id}/documents/{document_id}", "anyType"},
        {"GET /apps/{app_id}/documents/{document_id}", "anyType"},
        {"GET /records/{record_id}/apps/{app_id}/documents/{document_id}", "anyType"},
        {"GET /apps/{app_id}/inbox/{message_id}/attachments/{attachment_num}", "anyType"},

        {"GET /codes/systems/", "jsonType"},
        {"GET /codes/systems/{short_name}/query?q={query}","jsonType"},
        {"POST /records/{record_id}/apps/{app_id}/setup", "formUrlEncodedType"},
        {"POST /oauth/internal/session_create","formUrlEncodedType"},
        {"POST /oauth/internal/request_tokens/{request_token}/approve","formUrlEncodedType"},

        //{"POST /accounts/{account_id}/authsystems/password/change","notInAdmin"},

        {"POST /accounts/{account_id}/secret-resend","noBody"},
        {"POST /accounts/{account_id}/reset","noBody"},
        {"POST /oauth/internal/request_tokens/{request_token}/claim","noBody"},
        {"POST /records/{record_id}/documents/{document_id}/carenets/{carenet_id}/autoshare-revert","noBody"},
        {"POST /records/{record_id}/documents/{document_id}/set-status","once"},
        {"POST /records/{record_id}/shares/{account_id}/delete", "noBody"},
        {"POST /accounts/{account_id}/inbox/{message_id}/attachments/{attachment_num}/accept", "noBody"},
//        {"GET /carenets/{carenet_id}/documents/?type={indivo_document_type_url}", "omit"},
//        {"GET /carenets/{carenet_id}/reports/minimal/immunizations/", "omit"},

        {"PUT /records/{record_id}/inbox/{message_id}","skipfirst"},
        {"PUT /records/{record_id}/documents/{document_id}/rels/{rel_type}/{other_document_id}","noBody"},
        {"PUT /carenets/{carenet_id}/apps/{pha_email}","noBody"},
        {"PUT /records/{record_id}/documents/{document_id}/carenets/{carenet_id}","noBody"},
        {"PUT /records/{record_id}/documents/{document_id}/carenets/{carenet_id}","noBody"},
        {"PUT /records/{record_id}/documents/{document_id}/nevershare","noBody"}
    };


    public static void main(String[] args) {
        System.out.println("in main");
        if (args.length != 3) {
            throw new RuntimeException("GenClient <fullPathToWadl> <shellFromWhere> <directory_for_outputs>");
        }

        String wadlFromWhere = args[0];
        String shellFromWhere = args[1];
//        String testerStarts = args[2];
        String workDir = args[2];
        System.out.println("about to construct");
        GenClientFromWadl instance = new GenClientFromWadl(wadlFromWhere, shellFromWhere, workDir);

        System.out.println("about to Gen");
        instance.generate();
    }

    public GenClientFromWadl(String wadlInWhere, String shellFromWhere, String workingDirectory) {
        if (! workingDirectory.endsWith("/")) {
            workingDirectory += "/";
        }

        String testerStarts = "../../main/forCodeGen/";
        //logger = LogFactory.getLog(this.getClass());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
        xpath = XPathFactory.newInstance().newXPath();
        IndivoNamespaceContext namespaceContext = new IndivoNamespaceContext();
        namespaceContext.setNamespacePrefix("http://org.indivo.server/indivoServerWadlExtension", "iwe");
        namespaceContext.setNamespacePrefix("http://wadl.dev.java.net/2009/02", "wadl");
        namespaceContext.setNamespacePrefix("http://www.w3.org/1999/xhtml", "xhtml");
        xpath.setNamespaceContext(namespaceContext);

        File wadlIn = new File(wadlInWhere);
        try {
           wadl = documentBuilder.parse(wadlIn);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (org.xml.sax.SAXException sxe) {
            throw new RuntimeException(sxe);
        }


        try {
            clientShell = new FileInputStream(shellFromWhere);
            clientJava = new FileOutputStream(workingDirectory + "Rest.java");
            readToStartAuto(clientShell, clientJava);

            testerPhaJava = new FileOutputStream(workingDirectory + "TesterPha.java");
            FileInputStream tempIS = new FileInputStream(testerStarts + "TesterPha_START.java");
            copystream(tempIS, testerPhaJava);
            testerAdminJava = new FileOutputStream(workingDirectory + "TesterAdmin.java");
            tempIS = new FileInputStream(testerStarts + "TesterAdmin_START.java");
            copystream(tempIS, testerAdminJava);
            testerChromeJava = new FileOutputStream(workingDirectory + "TesterChrome.java");
            tempIS = new FileInputStream(testerStarts + "TesterChrome_START.java");
            copystream(tempIS, testerChromeJava);
            testerAppSpecificJava = new FileOutputStream(workingDirectory + "TesterAppSpecific.java");
            tempIS = new FileInputStream(testerStarts + "TesterAppSpecific_START.java");
            copystream(tempIS, testerAppSpecificJava);

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }


        exceptions = new HashMap<String,List<String>>();
        for (int ii = 0; ii < exceptionsArr.length; ii++) {
            String[] anException = exceptionsArr[ii];
            List<String> xcptnsForThis = new ArrayList<String>();
            for (int jj = 0; jj < (anException.length -1); jj++) {
                xcptnsForThis.add(anException[jj +1]);
            }
            exceptions.put(exceptionsArr[ii][0],xcptnsForThis);
        }

        once = new ArrayList<String>();

        String jsonText = null;
        try {
            // we maintain, in our wadl resources/doc a json formatted table
            // relating access grants to descriptive text
            jsonText = xpath.evaluate("/wadl:application/wadl:resources/wadl:doc", wadl);
        } catch (XPathException xpee) {
            throw new RuntimeException(xpee);
        }
        JsonParser jsonParser = new JsonParser();
        System.err.println("jsonText: " + jsonText);
        JsonObject allJson = (JsonObject) jsonParser.parse(jsonText);
        grantsToLegged = (JsonObject) allJson.get("grantsOauthLegged");

        genClientUtils = new GenClientUtils();
    }

    private void copystream(InputStream cis, OutputStream cos) {
        try {
            int cc = cis.read();
            while (cc != -1) {
                cos.write(cc);
                cc = cis.read();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void generate() {
        System.out.println("in generate");
        try {
            NodeList resources = (NodeList)
                    xpath.evaluate("/wadl:application/wadl:resources/wadl:resource", wadl, XPathConstants.NODESET);
            int[] countMinimal = {0};
            for (int ii = 0; ii < resources.getLength(); ii++) {
                Element aRes = (Element) resources.item(ii);
        System.out.println("in generate 2: " + aRes.getAttribute("id"));
                processRecursively(aRes, "", countMinimal);
            }

            close();
        } catch (javax.xml.xpath.XPathExpressionException xpee) {
            throw new RuntimeException(xpee);
        }
    }


    public void close() {
            try {
            readToStartAuto(clientShell, clientJava);
            clientJava.close();
            testerPhaJava.write("\n    }\n}\n".getBytes());
            testerPhaJava.close();
            testerAdminJava.write("\n    }\n}\n".getBytes());
            testerAdminJava.close();
            testerChromeJava.write("\n    }\n}\n".getBytes());
            testerChromeJava.close();
            testerAppSpecificJava.write("\n    }\n}\n".getBytes());
            testerAppSpecificJava.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void readToStartAuto(InputStream shell, OutputStream java)
            throws IOException {
        String aLine = getLine(shell);
        while (aLine != null
                && (! aLine.endsWith("/***START AUTO GENERATED FROM WIKI*/"))) {
            if (! aLine.trim().startsWith("/*_SHELL_DROP*/")) {
                int shix = aLine.indexOf("_SHELL");
                if (shix > -1) {
                   aLine = aLine.substring(0,shix) + aLine.substring(shix +"_SHELL".length());
                }
                java.write((aLine + "\n").getBytes());
            }
            aLine = getLine(shell);
        }
        if (aLine != null) {
            java.write((aLine + "\n").getBytes());
        }
    }


    private void processRecursively(Element aResource, String pathSoFar, int[] countMinimal)
            throws javax.xml.xpath.XPathExpressionException {
        String path = aResource.getAttribute("path");
        System.out.println("path: " + path);
        boolean ttt = false;
        if (path.equals("/{records_OR_carenets}/{record_OR_carenet_id}/documents/types/{type}/")) { ttt = true; }
        if (pathSoFar.length() == 0 && path.length() > 0) {
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            } else {
                throw new RuntimeException("first path element not starting with '/': " + path);
            }
        }
        String type = aResource.getAttribute("type");
        String path0 = pathSoFar + path;

        NodeList pres = (NodeList) xpath.evaluate("wadl:doc/xhtml:span/xhtml:pre", aResource, XPathConstants.NODESET);
//        System.out.println(domToString(pres.item(0)));
//        System.out.println("----------------");
//        pres = (NodeList) xpath.evaluate("wadl:doc/xhtml:span/xhtml:p", aResource, XPathConstants.NODESET);
//        System.out.println(domToString(pres.item(0)));
//        System.exit(0);

//        NodeList pres = (NodeList) xpath.evaluate("wadl:doc/xhtml:span/pre", aResource, XPathConstants.NODESET);


        NodeList meths = (NodeList) xpath.evaluate("wadl:method", aResource, XPathConstants.NODESET);
        if (meths.getLength() > 1) {
            throw new RuntimeException("more than one method in a resource: " + domToString_nopre(aResource));
        }

        System.out.println("in processRecursively: " + aResource.getAttribute("id") + "   meths: " + meths.getLength());
        if (meths.getLength() > 0) {
            System.out.println("meths length: " + meths.getLength());
            List<String> tokenList = new ArrayList<String>();
            int ix = 0;
            String patht = path0;
            boolean hasTrailSlash = false;
            while (patht.length() > 0) {
                ix = patht.indexOf('/');
                String token = null;
                if (ix == -1) {
                    token = patht.trim();
                    patht = "";                    //  GET /records/{record_id}/carenets/
                } else {
                    token = patht.substring(0,ix);
                    patht = patht.substring(ix +1).trim(); // drop trailing '/'
                    if (patht.length() == 0) { hasTrailSlash = true; }
                }

                tokenList.add(token);
            }

            for (int ii = 0; ii < meths.getLength(); ii++) {
                Element ameth = (Element) meths.item(ii);
//                String notimplemented = ameth.getAttributeNS("http://org.indivo.server/indivoServerWadlExtension", "not_implemented");
//                if (notimplemented.length() == 0) {
//                    System.out.println("implemented");
//                } else {
//                    System.out.println("not_implemented " + notimplemented);
//                }

                String httpMeth = ameth.getAttribute("name");
                String wikiLine = null;
                Element methExt = null;
                if (tokenList.get(0).equals("{records_OR_carenets}")
                        && tokenList.get(1).equals("{record_OR_carenet_id}")) {

                    boolean isMinimal = false;
                    int tlsz = tokenList.size();
                    if (tlsz == 5
                            && tokenList.get(3).equals("minimal")
                            && tokenList.get(2).equals("reports")) {
                        countMinimal[0]++;
                        isMinimal = true;
                        tokenList.set(4, "{type_of_minimal_eg_medications}");
                    }

                    if ((! isMinimal) || countMinimal[0] == 1) {  // do only first minimal
                        tokenList.set(0, "records");
                        tokenList.set(1, "{record_id}");
                        wikiLine = getWikiLine(httpMeth + " /" + path0.replace(
                                "{record_OR_carenet_id}","{record_id}").replace("{records_OR_carenets}","records"), pres);
                        methExt = (Element) xpath.evaluate(
                                "iwe:method_extension[@iwe:records_or_carenets='records']", ameth, XPathConstants.NODE);
                        processAMeth(ameth, methExt, path0, wikiLine, type, tokenList, hasTrailSlash);

                        tokenList.set(0, "carenets");
                        tokenList.set(1, "{carenet_id}");
                        wikiLine = getWikiLine(httpMeth + " /" + path0.replace(
                                "{record_OR_carenet_id}","{carenet_id}").replace("{records_OR_carenets}","carenets"), pres);
                        System.out.println("minimal: " + path0);
                        methExt = (Element) xpath.evaluate(
                                "iwe:method_extension[@iwe:records_or_carenets='carenets']", ameth, XPathConstants.NODE);
                        processAMeth(ameth, methExt, path0, wikiLine, type, tokenList, hasTrailSlash);
                    }
                }
                else {
                    System.out.println("not minimal: " + path0);
                    wikiLine = getWikiLine(httpMeth + " /" + path0, pres);
                    if (((NodeList) xpath.evaluate(
                            "iwe:method_extension", ameth, XPathConstants.NODESET)).getLength() != 1) {
                        throw new RuntimeException("unexpected number of method_extension: "
                                + wikiLine + "\n"+ domToString_nopre(ameth));
                    }
                    methExt = (Element) xpath.evaluate("iwe:method_extension", ameth, XPathConstants.NODE);
                    processAMeth(ameth, methExt, path0, wikiLine, type, tokenList, hasTrailSlash);
                }
            }
        }


        NodeList nested = (NodeList) xpath.evaluate("wadl:resource", aResource, XPathConstants.NODESET);
        //System.out.println("nested resources: " + nested.getLength() + " -- " + domToString_nopre(aResource));
        for (int ii = 0; ii < nested.getLength(); ii++) {
            processRecursively((Element) nested.item(ii), path0, countMinimal);
        }
    }

    private String getWikiLine(String path, NodeList pres) {
        String retVal = "";
        for (int ii = 0; ii < pres.getLength(); ii++) {
            Element apre = (Element) pres.item(ii);
            String content = apre.getTextContent().trim();
            int bix = content.indexOf(' ');

            // fix for double blank after DELETE
            if (bix > 0 && bix < content.length()) {
                while (content.charAt(bix +1) == ' ') {
                    content = content.substring(0, bix) + content.substring(bix +1);
                }
            }
            int nlix = content.indexOf('\n');
            if (nlix > 0) { content = content.substring(0, nlix); }
            if (content.contains(path + '?')
                    || content.endsWith(path) ) {
                retVal = content;
                break;
            }
        }

        if (retVal.length() == 0) {
            System.out.println("wikiLine: " + path);
            for (int ii = 0; ii < pres.getLength(); ii++) {
                System.out.println("    " + ((Element) pres.item(ii)).getTextContent());
            }
        }

        return retVal;
    }

    private void processAMeth(
            Element ameth, Element methExt, String path, String wikiLine, String type, List<String> tokenList, boolean hasTrailSlash)
            throws javax.xml.xpath.XPathExpressionException {
//        boolean reportsMinimal = false;
        if (methExt == null) {
            throw new RuntimeException("no method_extension: " + domToString_nopre(ameth));
        }

        //boolean skipMinimal = false;
        List<String> paramTypedList = new ArrayList<String>();
        List<String> urlParamList = new ArrayList<String>();
        StringBuffer javaDoc = new StringBuffer();
        StringBuffer javaDocQ = new StringBuffer();
        StringBuffer methSig = new StringBuffer("\n    public Object ");
        StringBuffer requestURL_SB = new StringBuffer();
        int state = GenClientUtils.STATE_START;
        boolean carenets = false;

/*<method iwe:grants="accesstoken(ar_share_record_or_pha) account(accessrule_carenet_account)" iwe:server_method_name="carenet_document_list" iwe:shortname="read_documents get_carenet_documents" name="GET">
<request>
<param name="type" style="query" type="xsd:string">
<doc>{type_url}</doc>
</param>
</request>
</method>
*/

        NodeList requestParams = (NodeList) xpath.evaluate("wadl:request//wadl:param[@style='query']", ameth, XPathConstants.NODESET);
        System.out.println("requestParams length: " + requestParams.getLength());
        System.out.println("   " + domToString_nopre(ameth));
        if (requestParams.getLength() > 0) {
            for (int ii = 0; ii < requestParams.getLength(); ii++) {
                Element aReqParam = (Element) requestParams.item(ii);
                String aReqParamName = aReqParam.getAttribute("name");
                String aReqParamDoc = xpath.evaluate("wadl:doc", aReqParam);
                if (aReqParamDoc == null) {
                    aReqParamDoc = "";
                }

                urlParamList.add(aReqParamName);
                paramTypedList.add("String " + newnew(aReqParamName));
                javaDocQ.append("\n    * @param "
                        + newnew(aReqParamName) + " " + aReqParamDoc);
            }
        }


//        String[][] requiredURLSplit = (String[][]) callDescription.get("urlEncodedParams");
//        if (requiredURLSplit != null) {
//            for (int ii = 0; ii < requiredURLSplit.length; ii++) {
//                paramList.add("String " + requiredURLSplit[ii][0]);
//                javaDocQ.append("\n    * @param "
//                        + requiredURLSplit[ii][0] + " " + requiredURLSplit[ii][1]);
//            }
//        }

        //boolean reportsMinimal = false;
        int[] stateA = new int[] { state };
        boolean[] carenetsA = new boolean[] { carenets };
        genClientUtils.tokensToSig(
                tokenList, methSig, paramTypedList, javaDoc, requestURL_SB, path, stateA, carenetsA);
        state = stateA[0];
        carenets = carenetsA[0];






        //Map principals = new HashMap<String,String>();
//        org.w3c.dom.NamedNodeMap nnm = ameth.getOwnerDocument().getDocumentElement().getAttributes();
//        for (int ttt = 0; ttt < nnm.getLength(); ttt++) {
//            org.w3c.dom.Node nnmn = nnm.item(ttt);
//            System.err.println("nnm " + ttt + "  " + nnmn.getNamespaceURI() + "   " +  nnmn.getNodeName()
//                    + "   " + ameth.lookupNamespaceURI("iwe") + "   "
//                    + ameth.lookupPrefix("http://org.indivo.server/indivoServerWadlExtension")
//                    + "   " + ameth.lookupNamespaceURI("ns0") + "   " + ameth.lookupPrefix("ns0") );
//        }

        /*
        String grants = ameth.getAttributeNS("http://org.indivo.server/indivoServerWadlExtension", "grants");
///        String grants = ameth.getAttribute("{http://org.indivo.server/indivoServerWadlExtension}grants");
        System.err.println(grants + "      " + wikiLine);
        int gix = grants.indexOf("accesstoken(");
        if (gix > -1) {
            principals.put("accesstoken", grants.substring(gix + "accesstoken(".length(), grants.indexOf(')',gix)));
        }
        gix = grants.indexOf("account(");
        if (gix > -1) {
            principals.put("account", grants.substring(gix + "account(".length(), grants.indexOf(')',gix)));
        }
        gix = grants.indexOf("machineapp(");
        if (gix > -1) {
            principals.put("machineapp", grants.substring(gix + "machineapp(".length(), grants.indexOf(')',gix)));
        }
        gix = grants.indexOf("userapp(");
        if (gix > -1) {
            principals.put("userapp", grants.substring(gix + "userapp(".length(), grants.indexOf(')',gix)));
        }
*/

         String httpMeth = ameth.getAttribute("name");
        List<String> exception = exceptions.get(wikiLine);
        if (exception == null
                && path.startsWith("{records_OR_carenets}/{record_OR_carenet_id}")
                && tokenList.get(0).equals("carenets") ) {
            exception = exceptions.get(wikiLine.replace(" /carenets/{carenet_id}", " /records/{record_id}"));
        }

        if (exception == null) { exception = new ArrayList<String>(); }
        
        boolean dowrite = true;
        if (exception.contains("once")) {
            if (once.contains(wikiLine)) {
                dowrite = false;
            } else {
                once.add(wikiLine);
            }
        } else if (exception.contains("skipfirst")) {
            if (! once.contains(wikiLine)) {
                dowrite = false;
                once.add(wikiLine);
            }
        }

        if (dowrite) {
            //String notimplemented = ameth.getAttributeNS("http://org.indivo.server/indivoServerWadlExtension", "not_implemented");
            writeMethod(
                state,
                ameth, //httpMeth,
                methExt,
                //notimplemented,
                //grants,
                hasTrailSlash,
                wikiLine,
                exception,
                new StringBuffer(methSig),
                carenets,
                //skipMinimal,
                new StringBuffer(requestURL_SB),
                urlParamList,
                paramTypedList,
                new StringBuffer(javaDoc),
                new StringBuffer(javaDocQ)
                //principals
                );
        }
    }



    private void writeMethod(
            int state,
            Element ameth, //String httpMeth,
            Element methExt,
            //String notimplemented,
            //String grants,
            boolean hasTrailSlash,
            String wikiLine,
            List<String> exception,
            StringBuffer methSig,
            boolean carenets,
            //boolean skipMinimal,
            StringBuffer requestURL_SB,
            List<String> urlParamList,
            List<String> paramTypedList,
            StringBuffer javaDoc,
            StringBuffer javaDocQ
            //Map<String,String> principals
            ) {


boolean hasBodyttt = false;
if (wikiLine.contains("/bytype/set")) {
    hasBodyttt = true;
    System.out.print("hasBody -- " + wikiLine + ": " + exception.size());
    for (String tttB : exception) {
        System.out.print(tttB + "   ");
    }
    System.out.println("");
}

//        Boolean inAdmin = (Boolean) callDescription.get("inAdmin");
//        if (inAdmin == null) { inAdmin = false; }
//        Boolean inPha = (Boolean) callDescription.get("inPha");
//        if (inPha == null) { inPha = false; }
//        Boolean inChrome = (Boolean) callDescription.get("inChrome");
//        if (inChrome == null) { inChrome = false; }

        //boolean hasTrailSlash = (Boolean) callDescription.get("hasTrailSlash");
        String javaDocPre = "\n\n    /** " + wikiLine;
        String grantsAsString = grantsToString(methExt);
        //System.err.println("grantsAsString: " + grantsAsString);
        if (grantsAsString.length() > 0) {
            javaDocPre += "\n    * ACCESSCONTROL   " + grantsAsString;
        }


        String notimplemented = methExt.getAttributeNS("http://org.indivo.server/indivoServerWadlExtension", "not_implemented");
        if (notimplemented.length() > 0) {
            javaDocPre += " -- not implemented: " + notimplemented;
        } else {

        }

        if (state == GenClientUtils.STATE_CONSTANT) {
            if (hasTrailSlash) {
                methSig.append("_");
                requestURL_SB.append("/");
            }
            requestURL_SB.append("\"");
        } else if (state == GenClientUtils.STATE_VARIABLE) {
            if (hasTrailSlash) {
                methSig.append("_");
                requestURL_SB.append(" + \"/\"");
            }
        }

        StringBuffer queryOutSB = new StringBuffer("String queryOut = ");
        //String rMeth = (String) callDescription.get("httpMethod");
        //String[][] requiredURLSplit = (String[][]) callDescription.get("urlEncodedParams");
        String httpMeth = ameth.getAttribute("name");
        if (httpMeth.equals("GET") && (urlParamList.size() > 0 || hasTrailSlash)) {
            if (urlParamList.size() > 0) {
                queryOutSB.append("buildFormURLEnc(new String[][] {");
                for (int ii = 0; ii < urlParamList.size(); ii++) {
                    if (ii > 0) {
                        queryOutSB.append(", ");
                    }
                    queryOutSB.append("{ \"" + urlParamList.get(ii) + "\", "
                            + newnew(urlParamList.get(ii)) + " }");
                }
                queryOutSB.append(" });");

            }
            if (httpMeth.equals("GET") && hasTrailSlash) {
                if (urlParamList.size() > 0) {
                    queryOutSB.append("\n        if (pagingOrderingQuery != null && pagingOrderingQuery.length() > 0) { queryOut += \"&\" + pagingOrderingQuery; }");
                } else {
                    queryOutSB.append("pagingOrderingQuery;");
                }

                int plix = 0;
                if (urlParamList.size() > 0) { plix = 1; }
                paramTypedList.add(plix, "String pagingOrderingQuery");
                javaDocQ.append("\n    * @param pagingOrderingQuery "
                        + "offset={offset}&limit={limit}&order_by={order_by}&status={document_status}&modified_since={modified_since}");
            }

            queryOutSB.append("\n");
        }
        else {
            queryOutSB.append(" null;\n");
        }

        methSig.append(httpMeth + "(\n            ");


        boolean[] twoThreeLegs = twoThreeLegs(methExt, wikiLine);
        //boolean justTwoLegged = twoThreeLegs[0] == true && twoThreeLegs[1] == false;
//                wikiLine.contains(" /apps/{app_id}/documents")
//                || wikiLine.contains(" /apps/{app_id}/inbox");//principals.get("userapp") != null;
        
        boolean hasBody = true;
        if (httpMeth.equals("GET") || httpMeth.equals("DELETE")) { hasBody = false; }
        else { // POST or PUT
            if (exception.contains("noBody")) { hasBody = false; }
//            else {
//                if (nextLine != null && nextLine.indexOf("={") != -1) {
//                    requiredURLSplit = splitFormEncoded(nextLine);
//                }
//            }
        }
//        boolean hasBody = (Boolean) callDescription.get("hasBody");


        if (twoThreeLegs[1]) {
            //System.err.println("TWO THREE: " + twoThreeLegs[0] + "." + twoThreeLegs[1] + ": " + domToString_nopre(ameth));
            paramTypedList.add("String accessToken");
            javaDoc.append("\n    * @param accessToken OAuth token.");
            if (twoThreeLegs[0]) { javaDoc.append("  null if from admin app."); }
            paramTypedList.add("String accessTokenSecret");
            javaDoc.append("\n    * @param accessTokenSecret OAuth secret.");
            if (twoThreeLegs[0]) { javaDoc.append("  null if from admin app."); }
        }
        
        else {
            System.err.println("two three: " + twoThreeLegs[0] + "." + twoThreeLegs[1] + ": " + domToString_nopre(ameth));
        }



        String bodyOut = "";
        //String tryuee = "";
        //String catchuee = "";
        if (hasBodyttt) { System.out.println(
            "hasBody ttt: " + hasBody + " " + httpMeth + " " + urlParamList.size() + " " + carenets);
        }
        if ((httpMeth.equals("POST") || httpMeth.equals("PUT")) && urlParamList.size() > 0) {
            //tryuee = "        try {\n";
            //catchuee = "        } catch (UnsupportedEncodingException uee) { throw new IndivoClientException(uee); }\n";
            StringBuffer bodyOutSB = new StringBuffer("buildFormURLEnc(new String[][] {");
            for (int ii = 0; ii < urlParamList.size(); ii++) {
                if (ii > 0) {
                    bodyOutSB.append(", ");
                }
                bodyOutSB.append("{ \"" + urlParamList.get(ii) + "\", " + newnew(urlParamList.get(ii)) + " }");
            }
            bodyOutSB.append(" })");

            bodyOut = bodyOutSB.toString() + ", \"application/x-www-form-urlencoded\", ";
        }
        else if (hasBody) {
            paramTypedList.add("Object body");
            javaDoc.append("\n    * @param body body of http request (data to send).");
            bodyOut = "body, ";
            if (exception.contains("send_textPlain")) {
                 bodyOut += "\"text/plain\", ";
            } else if (exception.contains("send_xml")) {
                bodyOut += "\"application/xml\", ";
            }
            else if (! carenets) {
                paramTypedList.add("String requestContentType");
                javaDoc.append("\n    * @param requestContentType of http request body (type of data to send).");
                bodyOut += "requestContentType, ";
            } else {
                bodyOut += "\"application/x-www-form-urlencoded\", ";
            }
            
        }

        if (exception.contains("anyType")) {
            paramTypedList.add("String responseContentType");
            javaDoc.append("\n    * @param responseContentType of http response body (expected type to get back, or null).");
        }

        paramTypedList.add("Map<String,Object> options");
        javaDoc.append("\n    * @param options see <strong>options</strong> above.");
        javaDoc.append("\n    */");
        StringBuffer testerSkeleton = new StringBuffer("// " + grantsAsString + "\n" + "// " + wikiLine + "\n");
        testerSkeleton.append("    System.out.println(\"" +
                methSig.substring("\n    public Object ".length(), methSig.indexOf("(")) + "\\n\" +\n" +
                "        \"" + wikiLine + "\");\n");
        if (exception.contains("anyType")) {
            testerSkeleton.append("        testResultObj = ");
        } else if (exception.contains("jsonType")) {
            testerSkeleton.append("        testResultJson = (List<Map<String,String>>) ");
        } else if (exception.contains("formUrlEncodedType")) {
            testerSkeleton.append("        testResultForm = (Map<String,String>) ");
        } else {
            testerSkeleton.append("        testResultDoc = (Document) ");
        }
        String phaOrAdmin = "client";
//        if (inAdmin) {
//            phaOrAdmin = "admin";
//        } else if (inChrome) {
//            phaOrAdmin = "chrome";
//        }
//        testerSkeleton.append("// " + wikiLine);
        testerSkeleton.append(phaOrAdmin + "."
                + methSig.substring("\n    public Object ".length(), methSig.indexOf("(") +1));

        for (int ii = 0; ii < paramTypedList.size(); ii++) {
            String aparam = paramTypedList.get(ii);
            methSig.append(aparam);
            testerSkeleton.append(aparam.substring(aparam.lastIndexOf(' ') +1));
            if (ii < paramTypedList.size() -1) {
                methSig.append(", ");
                testerSkeleton.append(", ");
            }
        }
        methSig.append(")\n            throws IndivoClientException {\n");
        testerSkeleton.append(");\n        System.out.println(");
//        testerSkeleton.append("                \""
//                //+ methSig.substring("\n    public Object ".length(), methSig.indexOf("("))
//                //+ "  --  " + wikiLine
//                + "\\n\"\n");
        if (exception.contains("anyType")) {
            testerSkeleton.append("testResultObj + \"\\n\\n\");\n\n");
        } else if (exception.contains("jsonType")) {
            testerSkeleton.append("printJson(testResultJson) + \"\\n\\n\");\n\n");
        } else if (exception.contains("formUrlEncodedType")) {
            testerSkeleton.append("printForm(testResultForm) + \"\\n\\n\");\n\n");
        } else {
            testerSkeleton.append("Utils.domToString(testResultDoc) + \"\\n\\n\");");
        }
        testerSkeleton.append(
                "\n        System.out.println(\"\\n==================================================\\n\\n\");\n\n");

        methSig.append("        " + queryOutSB.toString()/* + ";\n"*/);
//        if (inAdmin) {
            methSig.append("        Object fromRequest = null;\n" /*+ tryuee*/);
            methSig.append("        fromRequest = clientRequest(\n");
//        } else if (inPha) {
//            methSig.append("        Object fromPhaRequest = null;\n" /*+ tryuee*/);
//            methSig.append("        fromPhaRequest = phaRequest(\n");
//        } else if (inChrome) {
//            methSig.append("        Object fromChromeRequest = null;\n" /*+ tryuee*/);
//            methSig.append("        fromChromeRequest = chromeRequest(\n");
//        }
        methSig.append("               \"" + httpMeth + "\", " + requestURL_SB
                + ",\n               queryOut, ");

        String returnType = "";
        if (exception.contains("anyType")) {
            returnType = "responseContentType, ";
        } else if (exception.contains("jsonType")) {
            returnType = "\"application/json\", ";
        } else if (exception.contains("formUrlEncodedType")) {
            returnType = "new String[]{\"text/plain\", \"application/x-www-form-urlencoded\"}, ";
        }

//        if (inAdmin) {
//            methSig.append(bodyOut + returnType + "options);\n");
//            methSig.append(/*catchuee +*/ "        return fromAdminRequest;\n");
/*        } else*/
         if (! twoThreeLegs[1]) {
            methSig.append("null, null, " + bodyOut + returnType + "options);\n");
          } else {
            methSig.append("accessToken, accessTokenSecret, " + bodyOut + returnType + "options);\n");
          }
//            if (inPha) {
                methSig.append(/*catchuee +*/ "        return fromRequest;\n");
//            } else if (inChrome) {
//                methSig.append(/*catchuee +*/ "        return fromChromeRequest;\n");
//            }
//        } else {
//            methSig.append("accessToken, accessTokenSecret, " + bodyOut + returnType + "options);\n");
//            if (inPha) {
//                methSig.append(/*catchuee +*/ "        return fromPhaRequest;\n");
//            } else if (inChrome) {
//                methSig.append(/*catchuee +*/ "        return fromChromeRequest;\n");
//            }
//        }
        methSig.append("    }");

//        OutputStream javaOut = phaJava;
//        OutputStream testerJava = testerPhaJava;
//        if (inAdmin) {
//            javaOut = adminJava;
//            testerJava = testerAdminJava;
//        } else if (inChrome) {
//            javaOut = chromeJava;
//            testerJava = testerChromeJava;
//        }

//        if (skipMinimal) {
//            System.out.println("skipping minmal: "
//                    + methSig.toString().trim().substring(0, methSig.toString().trim().indexOf('\n')));
//        } else {
            try {
                clientJava.write((javaDocPre + javaDocQ + javaDoc + methSig + "\n").getBytes());

                /*
                String methSigStrTrm = methSig.toString().trim();
                if ((methSigStrTrm.startsWith(carenetAlsoPrefix + "documents")
                        || methSigStrTrm.startsWith(carenetAlsoPrefix + "reports"))
                     && (! methSigStrTrm.contains("carenet"))
                   ) {
                    int carenetPreIx = methSig.indexOf(carenetAlsoPrefix); // how much leading white space to copy later
                    String methSigCarenet = methSig.substring(0, carenetPreIx)
                            + carenetAlsoPrefix.substring(0, carenetAlsoPrefix.length() - "records_X_".length())
                            + "carenets_X_" + methSigStrTrm.substring(carenetAlsoPrefix.length());

                    javaOut.write((javaDocPre + javaDocQ + javaDoc + methSigCarenet).getBytes());
                }*/
                Element principal = (Element)
                        xpath.evaluate("iwe:grants[@iwe:principal='machineapp']", methExt, XPathConstants.NODE);
                System.err.println("princiapall: " + principal);
                if (principal != null) {
                    testerAdminJava.write(testerSkeleton.toString().getBytes());
                }

                principal = (Element)
                        xpath.evaluate("iwe:grants[@iwe:principal='account']", methExt, XPathConstants.NODE);
                if (principal != null) {
                    testerChromeJava.write(testerSkeleton.toString().getBytes());
                }

                principal = (Element)
                        xpath.evaluate("iwe:grants[@iwe:principal='accesstoken']", methExt, XPathConstants.NODE);
                if (principal != null) {
                    testerPhaJava.write(testerSkeleton.toString().getBytes());
                }

                principal = (Element)
                        xpath.evaluate("iwe:grants[@iwe:principal='userapp']", methExt, XPathConstants.NODE);
                if (principal != null) {
                    testerAppSpecificJava.write(testerSkeleton.toString().getBytes());
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } catch (javax.xml.xpath.XPathExpressionException xpee) {
                throw new RuntimeException(xpee);
            }
//        }

    }


    private String grantsToString(Element methExt) {
        try {
            return grantsToString0(methExt);
        } catch (javax.xml.xpath.XPathExpressionException xpee) {
            throw new RuntimeException(xpee);
        }
    }
    private String grantsToString0(Element methExt) throws javax.xml.xpath.XPathExpressionException {
        StringBuffer retval = new StringBuffer();
        NodeList grantNL = (NodeList) xpath.evaluate("iwe:grants", methExt, XPathConstants.NODESET);
        for (int ii = 0; ii < grantNL.getLength(); ii++) {
            if (ii > 0) { retval.append(" -- "); }
            Element pgrants = (Element) grantNL.item(ii);
            retval.append(pgrants.getAttributeNS("http://org.indivo.server/indivoServerWadlExtension", "principal"));
            NodeList grantsForP = (NodeList) xpath.evaluate("iwe:grant", pgrants, XPathConstants.NODESET);
            if (grantsForP.getLength() > 0) { retval.append(": "); }
            for (int jj = 0; jj < grantsForP.getLength(); jj++) {
                if (jj > 0) { retval.append(", "); }
                Element apgrant = (Element) grantsForP.item(jj);
                retval.append(apgrant.getTextContent());
            }
        }
        return retval.toString();
    }


    /** if no oauth_legged Element, guess based on path */
    private boolean[] twoThreeLegs(Element methExt, String wikiLine) {
        boolean[] retval = null;
        if (wikiLine.contains(" /apps/{app_id}/documents")
            || wikiLine.contains(" /apps/{app_id}/inbox")) {//principals.get("userapp") != null;
            retval = new boolean[] {true,false};
        } else {
            try {
                retval = twoThreeLegs0(methExt);
                if (retval == null) {
                    retval = new boolean[] { false, true };  // most common case
                }
            } catch (XPathExpressionException xpee) {
                throw new RuntimeException(xpee);
            }
        }
        return retval;
    }
    /** return null if no legged Element present */
    private boolean[] twoThreeLegs0(Element methExt) throws XPathExpressionException {
        boolean[] retval = null;

        NodeList grants = (NodeList) xpath.evaluate("iwe:grants", methExt, XPathConstants.NODESET);
        retval = new boolean[2]; retval[0] = false; retval[1] = false;
        if (grants.getLength() == 0) {
            retval[1] = true;  // default to assume 3-legged
        }
        for (int ii = 0;  ii < grants.getLength(); ii++) {
            Element agrants = (Element) grants.item(ii);
            String principal = agrants.getAttribute("iwe:principal");
//            System.out.println(domToString(agrants));
            NodeList pgrants = (NodeList) xpath.evaluate("iwe:grant", agrants, XPathConstants.NODESET);
            for (int jj = 0; jj < pgrants.getLength(); jj++) {
                String grantTxt = pgrants.item(jj).getTextContent();
                JsonElement jsone = grantsToLegged.get(grantTxt);
                String legged = "";
                if (jsone instanceof JsonObject) {
//                    System.out.println("Json object: " + jsone.toString());
//                    System.out.println("principal: " + principal);
                    legged = ((JsonObject) jsone).get(principal).getAsString();
                } else {
                    legged = jsone.getAsString();
                }
                if (legged.equals("2L")) { retval[0] = true; }
                if (legged.equals("3L")) { retval[1] = true; }
            }
        }

        System.out.println("two three leggs: " + retval[0] + ", " + retval[1]);
        return retval;
    }


//        if ((Boolean) xpath.evaluate("iwe:oauth_legged", methExt, XPathConstants.BOOLEAN)) {
//            retval = new boolean[2];
//            retval[0] = (Boolean) xpath.evaluate("iwe:oauth_legged/iwe:two_legged", methExt, XPathConstants.BOOLEAN);
//            retval[1] = (Boolean) xpath.evaluate("iwe:oauth_legged/iwe:three_legged", methExt, XPathConstants.BOOLEAN);
//        }

//        System.err.println(domToString_nopre(ameth));
//        System.err.println("two three: " + retval[0] + " " + retval[1]);


    /** avoid variable name that is a java reserved word */
    private String newnew(String newliteral) {
        if (newliteral.equals("new")) {
            return "newnew";
        } else {
            return newliteral;
        }
    }



    private String getLine(InputStream wis) {
        StringBuffer strb = new StringBuffer();
        int cc = 0;
        try {
            cc = wis.read();
            while (cc != -1 && cc != 10) {
                strb.append((char) cc);
                cc = wis.read();
            }
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if (cc == -1 && strb.length() == 0) {
            return null;
        } else {
            String retVal = strb.toString();
            return retVal;
        }
    }

    private String domToString_nopre(Node theNode) {
        String retVal = domToString(theNode);
        if (retVal.startsWith("<?xml ")) {
            int pix = retVal.indexOf("?>");
            retVal = retVal.substring(pix + 2).trim();
        }
        return retVal;
    }
    private synchronized String domToString(/*Document theDoc, */Node theNode) {
        Document theDoc = null;
        if (theNode instanceof Document) { theDoc = (Document) theNode; }
        else { theDoc = theNode.getOwnerDocument(); }

        DOMImplementation domI = theDoc.getImplementation();
        DOMImplementationLS domIls = (DOMImplementationLS) domI.getFeature("LS", "3.0");
        LSSerializer lss = domIls.createLSSerializer();
        String xmlstr = lss.writeToString(theNode);
        //<?xml version="1.0" encoding="UTF-16"?>
        return xmlstr;
    }

}
