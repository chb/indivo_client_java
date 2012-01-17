package org.indivo.oauth;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.security.GeneralSecurityException;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
*   Some utility methods for doing Indivo OAuth.
*/
public class AuthUtils implements Comparator<String[]> {

    private static Log logger = LogFactory.getLog(AuthUtils.class);

    /** strictly for testing */
    public static void main(String[] args) {
        System.err.println("just testing AuthUtils");
        String toEncode = "tilde:~~~~.";
        String encoded = null;
        try {
            encoded = encode(toEncode);
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException(gse);
        }
        System.err.println(toEncode + "    " + encoded);
    }

    /**
    * make a Mac object.
    * @param encodedSecret should be the output of AuthUtils.encode(String)
    *     or the '&' delimited concatenation of outputs of AuthUtils.encode(String)
    * @return the Mac object
    */
    public static Mac makeMac(String encodedSecret) throws GeneralSecurityException {
        logger.info("string for secret Mac key: " + encodedSecret);
        Mac mac = Mac.getInstance("HmacSHA1");
        Key secretKey = new SecretKeySpec(
                (/*AuthUtils.encode(consumerSecret) + '&'*/ encodedSecret).getBytes(), "HmacSHA1" );
        mac.init(secretKey);
        return mac;
    }


    
    /** RFC3986 unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
    * @param plain the un-encoded string
    * @return the encoded string.
    */
    public static String encode(String plain) throws GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < plain.length();ii++) {
            char charat = plain.charAt(ii);
            if ( (charat < 'a' || charat > 'z')
                    && (charat < 'A' || charat > 'Z')
                    && (charat < '0' || charat > '9')
                    && charat != '-' && charat != '.' && charat != '_' && charat != '~' ) {
                int hx = (((int)charat) & 0x000000ff) / 16;
                int lx = (((int)charat) & 0x000000ff) % 16;
                char hxc, lxc;
                if (hx < 10) {
                    hxc = (char) ('0' + hx);
                } else {
                    hxc = (char) ('A' + (hx -10));
                }
    
                if (lx < 10) {
                    lxc = (char) ('0' + lx);
                } else {
                    lxc = (char) ('A' + (lx -10));
                }
                
                if ( ((hxc < '0' || hxc > '9') && (hxc < 'A' || hxc > 'F'))
                     || ((lxc < '0' || lxc > '9') && (lxc < 'A' || lxc > 'F')) ) {
                    throw new RuntimeException("" + (((int)charat) & 0x000000ff) + "  hxc_lxc: " + hxc + lxc);
                }
                
                sb.append('%'); sb.append(hxc); sb.append(lxc);
            }
            else {
                sb.append(charat);
            }
        }
        return sb.toString();
    }

    /** RFC3986 unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
    * @param coded the encoded string
    * @return the decoded string.
    */
    public static String decode(String coded) throws GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < coded.length(); ii++) {
            char decoded;
            char charat = coded.charAt(ii);
            if (charat == '%') {
                if (ii > coded.length() -3) {
                    throw new GeneralSecurityException(
                            "unexpected coded string, late %: " + coded);
                }
                try {
                decoded = (char) (
                        (hexDig(coded.charAt(ii +1)) * 16)
                        + hexDig(coded.charAt(ii +2))  );
                } catch (GeneralSecurityException gse) {
                    throw new GeneralSecurityException(coded, gse);
                }
                ii += 2;
            } else {
                decoded = charat;
            }
            sb.append(decoded);
        }
        return sb.toString();
    }

    private static int hexDig(int dig) throws GeneralSecurityException {
        if (dig >= '0' && dig <= '9') {
            return dig - '0';
        } else if (dig >= 'a' && dig <= 'f') {
            return (dig - 'a') + 10;
        } else if (dig >= 'A' && dig <= 'F') {
            return (dig - 'A') + 10;
        } else {
            throw new GeneralSecurityException("unrecognized % code");
        }
    }
    
    /**
    * @param siglist a list of key,value pairs
    * (each pair represented by a String array of length 2)
    * return the values in the form of a String formatted for use in an OAuth signature
    * base String.  The caller is responsible for ommitting the 'realm' and 'signature'
    * parameters, as saListToAmpString simply sorts and concatenates all pairs.
    * @return string that can be used as the parameters portion of a signature base string.
    */
    public static String saListToAmpString(List<String[]> siglist) {
        AuthUtils auInst = new AuthUtils();
        String[][] tosort = siglist.toArray(new String[0][0]);
        Arrays.sort(tosort, auInst);
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < tosort.length; ii++) {
            String[] fromsort = tosort[ii];
            sb.append(fromsort[0] + '=' + fromsort[1]);
            if (ii < tosort.length -1) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    /** Same as getStringFromPmap(...) except that if key is not present in map,
    * return null;
    * @param pmap parameter map
    * @param key key of the parameter who's value is to be returned
    * @return parameter value or, if parameter of given key not present, null
    */
    public static String optionalFromPmap(Map<String,String[]> pmap, String key)
           throws GeneralSecurityException {
        String[] val = pmap.get(key);
        if (val == null) {
            return null;
        } else {
            return getStringFromPmap(pmap, key);
        }
    }

    /**
    * In cases where a map, such as a parameter map, has values in the form of String[],
    * and you know a particular map key must be present, and you know there must be exactly
    * zero or one value for that key, this will return that value (or return null if
    * zero values for that key.  Key must be present even if there length of the value
    * array is 0.
    *
    * @param pmap parameter map
    * @param key key of the parameter who's value is to be returned
    * @return parameter value
    * @throws GeneralSecurityException if no parameter of given key
    *    is in the map, or if more than one parameter value for the given key.
    */    
    public static String getStringFromPmap(Map<String,String[]> pmap, String key)
           throws GeneralSecurityException {
        String[] val = pmap.get(key);
        if (val == null) {
            throw new GeneralSecurityException("pmap missing key: " + key);
        } else if (val.length > 1) {
            throw new GeneralSecurityException("pmap value array length > 1 for key: " + key);
        }
        
        if (val.length == 0) {
            return null;
        } else {
            return val[0];
        }
    }

      
    /**
    * This method not intended for application use, it is public in order to implement
    * the Comparator interface.
    *
    * This method is meant to be used by Arrays.sort(...)
    *
    * From OAuth Spec:
    * "parameters MUST be encoded as described in Parameter Encoding (Parameter Encoding)
    * prior to constructing the Signature Base String."
    *
    * Not entirely clear, but seems that "prior" means before sorting, so that the sort
    * should be based on the % encoded values, not on the order of the plain text values
    */
    public int compare(String[] o1, String[] o2) {
        String o1a = null, o1b = null, o2a = null, o2b = null;
            // seems that "prior" means before sorting, don't decode, should test this!
            o1a = /*decode(*/o1[0]/*)*/;
            o1b = /*decode(*/o1[1]/*)*/;
            o2a = /*decode(*/o2[0]/*)*/;
            o2b = /*decode(*/o2[1]/*)*/;

        int retVal = o1a.compareTo(o2a);
        if (retVal == 0) {
            retVal = o1b.compareTo(o2b);
        }
        return retVal;
    }      
}
