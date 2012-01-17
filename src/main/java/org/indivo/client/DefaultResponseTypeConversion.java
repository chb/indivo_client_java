package org.indivo.client;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.Header;

import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 *
 * @author nate
 */
public class DefaultResponseTypeConversion implements ResponseTypeConversion {

    private Log logger = null;

    private DocumentBuilderFactory documentBuilderFactory = null;
    private final DocumentBuilder documentBuilder;


    public DefaultResponseTypeConversion() throws IndivoClientException {
        logger = LogFactory.getLog(this.getClass());
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new IndivoClientException(pce);
        }
    }


    public Object responseToObject(HttpEntity entity)
            throws IndivoClientException {
        Object retVal = null;
        Document docForContent = null;
        Header contentTypeH = entity.getContentType();
        String contentType = null;
        if (contentTypeH != null) { contentType = contentTypeH.getValue(); }
        logger.info("contentType header: " + contentType);
        Header encodingH = entity.getContentEncoding();
        String encoding = null;

        InputStream istrm = null;
        try {
            istrm = entity.getContent();
        } catch (IOException ioe) {
                throw new IndivoClientException(ioe);
        }
        String istrmdata = dataFromStream(istrm);

        logger.debug("coercing: " + istrmdata);

         // workaround for Pivotal 2172901
        if (contentType.startsWith("application/xml") || contentType.startsWith("text/xml")) {
            try {
                String[] parsedProlog = Utils.getEncoding(istrmdata);
                String prologEncoding = "UTF-8";
                if (parsedProlog.length == 3) {
                    prologEncoding = parsedProlog[1];
                }

                synchronized(documentBuilder) {
                    //System.out.println(Utils.toHex(istrmdata));
                    //logger.debug("istrmdata: " + Utils.toHex(istrmdata));
                    //System.out.println("getBytes encoding: " + encoding);
                    retVal = documentBuilder.parse(new ByteArrayInputStream(/* FIXME trim()*/istrmdata.trim().getBytes(prologEncoding)));
                }
            } catch (org.xml.sax.SAXException sxe) {
                logger.debug(istrmdata, sxe);
                throw new IndivoClientException(istrmdata, sxe);
            } catch (IOException ioe) {
                throw new IndivoClientException(istrmdata, ioe);
            }

        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            //String encodedString = dataFromStream(istrm);
            retVal = mapFromFormEncodedString(istrmdata);
        } else if (contentType.startsWith("application/json")) {
            //String cnctnData = dataFromStream(istrm);
            Gson gson = new Gson();
            java.lang.reflect.Type listMapStringString = new TypeToken<List<Map<String,String>>>() {}.getType();
            retVal = gson.fromJson(istrmdata, listMapStringString);
            return retVal;
        } else if (contentType.startsWith("text/plain")) {
            retVal = istrmdata;
        } else {
            retVal = new String[2];
            ((String[])retVal)[0] = contentType;
            ((String[])retVal)[1] = istrmdata;
        }
        return retVal;
    }

    String dataFromStream(InputStream inputStrm) throws IndivoClientException {
        String xstr = null;
        try {
            int xcc = inputStrm.read();
            StringBuffer xstrb = new StringBuffer();
            StringBuffer unexpectedBuffer = new StringBuffer();
            while (xcc != -1) {
                xstrb.append((char) xcc);
                xcc = inputStrm.read();
            }
            xstr = xstrb.toString();

            /*
            // indivo bug workaround  FIXME   remove this when data clean, UTF-8 default added to Pha and Admin
            if (xstr.startsWith("<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<")) {
                xstrb.replace(34,36,"8");
                logger.warn("replacing " + xstr.substring(0,50) + " with " + xstrb.substring(0,49));
                xstr = xstrb.toString();
            }
            */

        } catch (java.io.IOException ioe) {
            throw new IndivoClientException(ioe);
        }
        return xstr;
    }

        /**
    * given a form-urlencoded response, return that response in the
    * form of a key-value map.
    */
    public Map<String,String> mapFromFormEncodedString(String hresp)
            throws IndivoClientException {

        Map<String,String> retVal = new HashMap<String,String>();

        String hresp0 = StringEscapeUtils.unescapeHtml(hresp);
        logger.info("encoded entitied response body: " + hresp
                + "         encoded response body: " + hresp0);
        String[] pairs = hresp0.split("&");
        for (int tt = 0; tt < pairs.length; tt++) {
            logger.info("apair: " + pairs[tt]);
        }

        for (int ii = 0; ii < pairs.length; ii++) {
            int eix = pairs[ii].indexOf('=');
            if (eix == -1) {
                throw new IndivoClientException("did not find '=' in param: " + pairs[ii] + "\n" + hresp0);
            }
            String pName = pairs[ii].substring(0,eix);
            if (retVal.get(pName) != null) {
                throw new IndivoClientException("found multiple '" + pName + "' params." + hresp0);
            }
            retVal.put(pName, pairs[ii].substring(eix +1));
        }
        return retVal;
    }


}
