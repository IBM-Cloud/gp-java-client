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

import java.util.Map;
import java.util.ResourceBundle.Control;
import java.util.spi.ResourceBundleControlProvider;

/**
 * <code>CloudResourceBundleControlProvider</code> implements
 * {@link java.util.spi.ResourceBundleControlProvider} introduced in Java 8.
 * The IBM Globalization Java client jar file contains the Java extension
 * configuration file (<code>META-INF/services/java.util.spi.ResoruceBundleControlProvider</code>),
 * so this implementation is automatically enabled by putting the jar file
 * in the JRE's extension directory.
 * <p>
 * When using IBM Globalization Java client though the Java extension mechanism,
 * you need to provide IBM Globalization client configuration by following environment
 * variables.
 * 
 *  <ul>
 *      <li><code>GAAS_URL</code> [required]: The IBM Globalization service URL
 *      (e.g. https://gaas.mybluemix.net/translate)</li>
 *
 *      <li><code>GAAS_API_KEY</code> [required]: The API key for accessing the service.
 *      You can use a reader key. (e.g. 4f326ce7-8fec-4709-84f7-e5ff72689539)</li>
 *
 *      <li><code>GAAS_CACHE_EXPIRATION</code>: The resource bundle cache expiration time in milliseconds.
 *      The default value is 60000 (10 minutes). Use -1 for disabling resource bundle cache, and -2
 *      to disabling cache expiration (once cached, it won't be updated).</li>
 *  </ul>
 *
 * If these required environment variables are not available, IBM Globalization Java client is disabled
 * and this service provider implementation will return null.
 * 
 * @author Yoshito Umaoka
 */
public final class CloudResourceBundleControlProvider implements
        ResourceBundleControlProvider {

    /**
     * The environment variable name for specifying resource bundle cache expiration time.
     */
    public static final String GAAS_CACHE_EXPIRATION = "GAAS_CACHE_EXPIRATION";

    private static final CloudResourceBundleControl INSTANCE;

    static {
        ServiceAccount gaasAccount = ServiceAccount.getInstance();
        if (gaasAccount == null) {
            INSTANCE = null;
        } else {
            long cacheExpiration = CloudResourceBundleControl.DEFAULT_CACHE_EXPIRATION;

            Map<String, String> env = System.getenv();
            String envCacheExp = env.get(GAAS_CACHE_EXPIRATION);

            if (envCacheExp != null) {
                try {
                    cacheExpiration = Long.parseLong(envCacheExp);
                } catch (NumberFormatException e) {
                    // Fall through
                }
            }

            INSTANCE = new CloudResourceBundleControl(gaasAccount, cacheExpiration, null);
        }
    }

    @Override
    public Control getControl(String baseName) {
        return INSTANCE;
    }
}
