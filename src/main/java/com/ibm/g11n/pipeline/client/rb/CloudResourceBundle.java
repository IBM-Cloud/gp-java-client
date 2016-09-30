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
package com.ibm.g11n.pipeline.client.rb;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.ibm.g11n.pipeline.client.ServiceAccount;
import com.ibm.g11n.pipeline.client.ServiceClient;
import com.ibm.g11n.pipeline.client.ServiceException;

/**
 * <code>CloundResourceBundle</code> is a concrete subclass of {@link ResourceBundle}
 * that loads resource strings from IBM Globalization Pipeline service.
 * 
 * @author Yoshito Umaoka
 */
public final class CloudResourceBundle extends ResourceBundle {

    private static final Logger logger = Logger.getLogger(CloudResourceBundle.class.getName());

    private volatile Map<String, String> data;

    /**
     * Package local factory method creating a new CloundResourceBundle instance
     * for the specified service account, bundle ID and locale.
     * 
     * @param serviceAccount    The service account for IBM Globalization Pipeline
     * @param bundleId          The bundle ID
     * @param locale            The locale
     * @return An instance of CloundResourceBundle.
     */
    static CloudResourceBundle loadBundle(ServiceAccount serviceAccount, String bundleId, Locale locale) {
        CloudResourceBundle crb = null;
        ServiceClient client = ServiceClient.getInstance(serviceAccount);
        try {
            Map<String, String> resStrings = client.getResourceStrings(bundleId, locale.toLanguageTag(), false);
            crb = new CloudResourceBundle(resStrings);
        } catch (ServiceException e) {
            logger.info("Could not fetch resource data for " + locale
                    + " from the translation bundle " + bundleId + ": " + e.getMessage());
        }
        return crb;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(data.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object handleGetObject(String arg0) {
        return data.get(arg0);
    }
}
