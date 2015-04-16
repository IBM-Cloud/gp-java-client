/*  
 * Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.gaas;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.ibm.gaas.impl.GaasRestServiceClient;
import com.ibm.gaas.impl.pojo.ResourceData;

/**
 * <code>CloundResourceBundle</code> is a concrete subclass of {@link ResourceBundle}
 * that loads resource strings from IBM Globalization service.
 * 
 * @author Yoshito Umaoka
 */
public final class CloudResourceBundle extends ResourceBundle {

    private volatile Map<String, String> data;

    /**
     * Package local factory method creating a new CloundResourceBundle instance
     * for the specified service account, project ID and locale.
     * 
     * @param serviceAccount    The service account for IBM Globalization
     * @param projectId         The project ID
     * @param locale            The locale
     * @return An instance of CloundResourceBundle.
     */
    static CloudResourceBundle loadBundle(ServiceAccount serviceAccount, String projectId, Locale locale) {
        GaasRestServiceClient client = new GaasRestServiceClient(serviceAccount);
        ResourceData resData = client.getResourceData(projectId, locale);
        if (resData == null) {
            return null;
        }
        return new CloudResourceBundle(resData.getData());
    }

    /**
     * Private constructor, only called from {@link #loadBundle(ServiceAccount, String, Locale)}.
     * This class does not have any public constructors.
     * 
     * @param data  The resource string key/value pairs
     */
    private CloudResourceBundle(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(data.keySet());
    }

    @Override
    protected Object handleGetObject(String arg0) {
        return data.get(arg0);
    }
}
