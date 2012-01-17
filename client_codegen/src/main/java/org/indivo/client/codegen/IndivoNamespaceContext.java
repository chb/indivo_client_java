/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.indivo.client.codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author nate
 */
public class IndivoNamespaceContext implements NamespaceContext {
    
    private Map<String,List<String>> nsPrefixMap = new HashMap<String,List<String>>();
    private Map<String,String> prefixNsMap = new HashMap<String,String>();

    public String getNamespaceURI(String pre) {
        return prefixNsMap.get(pre);
    }

    public String getPrefix(String ns) {
        List<String> prefixes = nsPrefixMap.get(ns);
        if (prefixes == null) { return null; }
        else { return prefixes.get(0); }
    }

    public Iterator getPrefixes(String ns) {
        final List<String> prefixes = nsPrefixMap.get(ns);
        if (prefixes == null) { return null; }
        else {
            Iterator retVal = new Iterator() {
                private int ii = 0;
                List<String> prefixes0 = prefixes;

                public Object next() {
                    String retVal0 = null;
                    if (ii < prefixes0.size()) {
                        retVal0 = prefixes.get(ii);
                        ii++;
                    } else {
                        throw new java.util.NoSuchElementException();
                    }

                    return retVal0;
                }

                public boolean hasNext() {
                    return (ii < prefixes0.size());
                }

                public void remove() { throw new UnsupportedOperationException("no remove()"); }
            };

            return retVal;
        }
    }

    public void setNamespacePrefix(String nameSpace, String prefix) {
        List<String> priorPrefixes = nsPrefixMap.get(nameSpace);
        if (priorPrefixes ==  null) {
            priorPrefixes = new ArrayList<String>();
            nsPrefixMap.put(nameSpace, priorPrefixes);
        }
        if (! priorPrefixes.contains(prefix)) {
            priorPrefixes.add(prefix);
        }

        prefixNsMap.put(prefix,nameSpace);
    }
}
