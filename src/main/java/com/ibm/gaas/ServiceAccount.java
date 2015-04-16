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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * <code>ServiceAccount</code> is a class representing IBM Globalization service
 * account information, including service URL and API key. An instance of <code>ServiceAccount</code>
 * is required to create an instance of {@link CloudResourceBundleControl}, which enables
 * Java's {@link java.util.ResourceBundle} to access resources though IBM Globalization service.
 * <p>
 * There are currently 3 factory methods below:
 *  <ul>
 *      <li>{@link ServiceAccount#getInstance()}: This method is recommended
 *      for an application running on Bluemix. The method loads an account configuration
 *      from environment variables and VCAP_SERVICES. If an instance of IBM Globalization
 *      service is bound to a Bluemix application, then the configuration is automatically
 *      detected.</li>
 *
 *      <li>{@link ServiceAccount#getInstance(String, String)}: This method is
 *      recommended for an application running outside of Bluemix environment. You have
 *      to supply a valid service URL and an API key explicitly.</li>
 *
 *      <li>{@link ServiceAccount#getInstanceByVcapServices(String)}: This method
 *      might be necessary by an application running on Bluemix, if multiple IBM Globalization
 *      service instances are bound to the application.</li>
 *  </ul>
 *
 * @author Yoshito Umaoka
 */
public class ServiceAccount {
    private static final Logger logger = Logger.getLogger(ServiceAccount.class.getName());

    /**
     * The environment variable name for specifying an IBM Globalization service URL
     */
    public static final String GAAS_URL = "GAAS_URL";

    /**
     * The environment variable name for specifying an IBM Globalization service API key.
     */
    public static final String GAAS_API_KEY = "GAAS_API_KEY";

    private static final String GAAS_SERVICE_NAME = "IBM Globalization";

    private URL url;
    private String apiKey;

    /**
     * A package local constructor. This constructor does not validate
     * input parameters and only used by the GaaS client implementation.
     * 
     * @param url The service URL
     * @param apiKey The API key, which might be null
     */
    ServiceAccount(URL url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
    }

    /**
     * Returns an instance of ServiceAccount for the specified IBM Globalization
     * service URL and the API key.
     * 
     * @param serviceUrl    The IBM Globlization service URL (e.g. https://gaas.mybluemix.net/translate)
     * @param apiKey        The API key (e.g. 4f326ce7-8fec-4709-84f7-e5ff72689539)
     * @return An instance of ServiceAccount
     * @throws IllegalArgumentException if either serviceUrl or apiKey is null. 
     */
    public static ServiceAccount getInstance(String serviceUrl, String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("apiKey is null");
        }
        if (serviceUrl == null) {
            throw new IllegalArgumentException("serviceUrl is null");
        }

        URL theUrl = null;
        try {
            theUrl = new URL(serviceUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed serviceUrl", e);
        }

        return new ServiceAccount(theUrl, apiKey);
    }

    /**
     * Returns an instance of ServiceAccount. This factory method tries below in order.
     *  <ol>
     *      <li>Check environment variables <code>GAAS_URL</code> (service URL - e.g.
     *      https://gaas.mybluemix.net/translate) and <code>GAAS_API_KEY</code> (API key -
     *      e.g. 4f326ce7-8fec-4709-84f7-e5ff72689539). If both of above are defined,
     *      this method calls {@link #getInstance(String, String)} with the service URL
     *      and the API key and return the result.</li>
     *
     *      <li>Check <code>VCAP_SERVICES</code> environment variable. If the variable
     *      is available and an entry for IBM Globalization is included, use the service
     *      URL and the API key included in the JSON <code>credentials</code> object.
     *      When multiple entries for IBM Globalization are available, the first one is
     *      used.</li>
     *
     *      <li>If above failed, returns null.</li>
     * </ol>
     * @return  An instance of ServiceAccount, or null if service configuration is
     *          not available.
     */
    public static ServiceAccount getInstance() {
        ServiceAccount account = getInstanceByEnvVars();
        if (account == null) {
            account = getInstanceByVcapServices(null);
        }
        return account;
    }

    /**
     * Returns an instance of ServiceAccount for the specified IBM Globalization
     * service instance name from VCAP_SERVICES environment variable. If the
     * specified service name was not found, this method returns null.
     * 
     * @param serviceInstanceName
     *          The name of the IBM Globalization service instance.
     * @return  An instance of ServiceAccount for the specified service
     *          instance name, or null if the name was not found.
     * @throws IllegalArgumentException if serviceName is null.
     */
    public static ServiceAccount getInstanceForService(String serviceInstanceName) {
        if (serviceInstanceName == null) {
            throw new IllegalArgumentException("serviceName is null");
        }
        return getInstanceByVcapServices(serviceInstanceName);
    }

    /**
     * Returns an instance of ServiceAccount from the environment variables -
     * GAAS_URL and GAAS_API_KEY. If either of them is not defined, this method
     * returns null.
     * 
     * @return  An instance of ServiceAccount initialized by the environment
     *          variables - GAAS_URL and GAAS_API_KEY, or null if either of them
     *          is not defined.
     */
    private static ServiceAccount getInstanceByEnvVars() {
        Map<String, String> env = System.getenv();
        String gaasUrl = env.get(GAAS_URL);
        String gaasApiKey = env.get(GAAS_API_KEY);
        if (gaasUrl == null || gaasApiKey == null) {
            return null;
        }
        logger.info("A ServiceAccount is created from environment variables: GAAS_URL="
                + gaasUrl + ", GAAS_API_KEY=***");
        return getInstance(gaasUrl, gaasApiKey);
    }

    /**
     * Returns an instance of ServiceAccount from the VCAP_SERVICES environment
     * variable. When <code>serviceInstanceName</code> is null, this method returns
     * a ServiceAccount for the first valid IBM Globlization service instance.
     * If <code>serviceInstanceName</code> is not null, this method look up a
     * matching service entry, and returns a ServiceAccount for the matching entry.
     * If <code>serviceInstanceName</code> is not null and there is no match,
     * this method returns null.
     * 
     * @param serviceInstanceName
     *          The name of the IBM Globalization service instance, or null
     *          designating the first available service instance.
     * @return  An instance of ServiceAccount for the specified service instance
     *          name, or null.
     */
    private static ServiceAccount getInstanceByVcapServices(String serviceInstanceName) {
        Map<String, String> env = System.getenv();
        String vcapServices = env.get("VCAP_SERVICES");
        if (vcapServices == null) {
            return null;
        }

        ServiceAccount account = null;
        try {
            JsonObject obj = new JsonParser().parse(vcapServices).getAsJsonObject();
            JsonArray gaasArray = obj.getAsJsonArray(GAAS_SERVICE_NAME);
            if (gaasArray != null) {
                for (int i = 0; i < gaasArray.size(); i++) {
                    JsonObject gaasEntry = gaasArray.get(i).getAsJsonObject();
                    if (serviceInstanceName != null) {
                        // When service instance name is specified,
                        // this method returns only a matching entry
                        JsonPrimitive name = gaasEntry.getAsJsonPrimitive("name");
                        if (name == null || !serviceInstanceName.equals(name.getAsString())) {
                            continue;
                        }
                    }
                    JsonObject credentials = gaasEntry.getAsJsonObject("credentials");
                    if (credentials == null) {
                        continue;
                    }
                    JsonPrimitive jsonUri = credentials.getAsJsonPrimitive("uri");
                    JsonPrimitive jsonApiKey = credentials.getAsJsonPrimitive("api_key");
                    if (jsonUri != null && jsonApiKey != null) {
                        logger.info("A ServiceAccount is created from VCAP_SERVICES: uri="
                                + jsonUri + ", api_key=***");
                        account = getInstance(jsonUri.getAsString(), jsonApiKey.getAsString());
                        break;
                    }
                }
            }
        } catch (JsonParseException e) {
            // Fall through - will return null
        }

        return account;
    }

    /**
     * Returns the IBM Globalization URL.
     * @return The IBM Globalization URL.
     */
    public URL getServiceUrl() {
        return url;
    }

    /**
     * Returns the API key.
     * @return The API key.
     */
    public String getApiKey() {
        return apiKey;
    }
}

