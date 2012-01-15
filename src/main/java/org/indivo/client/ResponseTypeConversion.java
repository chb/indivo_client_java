package org.indivo.client;

import org.apache.http.HttpEntity;

/**
 *
 * @author nate
 */
public interface ResponseTypeConversion {

        Object responseToObject(HttpEntity entity) throws IndivoClientException;

}
