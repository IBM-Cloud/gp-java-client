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
package com.ibm.g11n.pipeline.client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.ibm.g11n.pipeline.client.rb.CloudResourceBundleControl;
import com.ibm.g11n.pipeline.iam.TokenManager;
import com.ibm.g11n.pipeline.iam.TokenManagerException;
import com.ibm.g11n.pipeline.iam.TokenManagerFactory;

/**
 * <code>ServiceAccount</code> is a class representing IBM Globalization Pipeline
 * service account information, including service URL and credentials. An instance
 * of <code>ServiceAccount</code> is required to create an instance of
 * {@link CloudResourceBundleControl}, which enables Java's {@link java.util.ResourceBundle}
 * to access resources though IBM Globalization Pipelineservice.
 * <p>
 * There are currently 3 factory methods available:
 *  <ul>
 *      <li>{@link ServiceAccount#getInstance()}: This method is recommended
 *      for an application running on Bluemix. The method loads an account configuration
 *      from environment variables and VCAP_SERVICES. If an instance of IBM Globalization
 *      Pipeline service is bound to a Bluemix application, then the configuration is
 *      automatically detected.</li>
 *
 *      <li>{@link ServiceAccount#getInstance(String, String, String, String)}: This method is
 *      recommended for an application running outside of Bluemix environment. You have
 *      to supply a valid service URL, service instance ID, user ID and password.</li>
 *
 *      <li>{@link ServiceAccount#getInstanceByVcapServices(String, String)}: This method
 *      might be used by an application running on Bluemix, when multiple IBM Globalization
 *      Pipeline service instances are bound to the application.</li>
 *  </ul>
 *
 * @author Yoshito Umaoka
 */
public class ServiceAccount {
    private static final Logger logger = Logger.getLogger(ServiceAccount.class.getName());

    /**
     * The environment variable name for specifying a service URL.
     */
    public static final String GP_URL = "GP_URL";

    /**
     * The environment variable name for specifying an instance ID.
     */
    public static final String GP_INSTANCE_ID = "GP_INSTANCE_ID";

    /**
     * The environment variable name for specifying a user ID.
     */
    public static final String GP_USER_ID = "GP_USER_ID";

    /**
     * The environment variable name for specifying a password.
     */
    public static final String GP_PASSWORD = "GP_PASSWORD";
    
    /**
     * The environment variable name for specifying a IAM API Key.
     */
    public static final String GP_IAM_API_KEY = "GP_IAM_API_KEY";
    
    /**
     * The environment variable name for specifying a IAM bearer token.
     */
    public static final String GP_IAM_BEARER_TOKEN = "GP_IAM_BEARER_TOKEN";
    
    /**
     * The environment variable name for specifying IAM endpoint.
     */
    public static final String GP_IAM_ENDPOINT = "GP_IAM_ENDPOINT";


    /**
     * The environment variable name for specifying a service name used
     * by Globalization Pipeline service. The value is used for searching
     * valid credentials in VCAP_SERVICES. If this environment variable
     * is not defined, the default name pattern ("gp-*" or "g11n-pipeline*")
     * is used for look up.
     */
    public static final String GP_SERVICE_NAME = "GP_SERVICE_NAME";

    /**
     * The environment variable name for specifying a service instance
     * name used by an instance of Globalization Pipeline service.
     * The value is used for searching valid credentials in VCAP_SERVICES.
     * If this environment variable is not defined, the first service
     * instance found in VCAP_SERVICES will be used.
     */
    public static final String GP_SERVICE_INSTANCE_NAME = "GP_SERVICE_INSTANCE_NAME";

    /**
     * Default service name pattern used for locating Globalization Pipeline service
     * instances.
     */
    private static final Pattern GP_SERVICE_NAME_PATTERN = Pattern.compile("gp-.+|g11n-pipeline.*");

    private String url; // No trailing '/'
    private String instanceId;
    private String userId;
    private String password;
    private TokenManager tokenManager;


    /**
     * Private constructor.
     * 
     * @param url           Service URL, no trailing '/'
     * @param instanceId    Service instance ID
     * @param userId        User ID
     * @param password      Password
     */
    private ServiceAccount(String url, String instanceId, String userId, String password) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.password = Objects.requireNonNull(password, "password mut not be null");
    }
    
    /**
     * Private constructor.
     * 
     * @param url
     *            Service URL, no trailing '/'
     * @param instanceId
     *            Service instance ID
     * @param tokenManager
     *            IAM token manager
     */
    private ServiceAccount(String url, String instanceId, TokenManager tokenManager) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId must not be null");
        this.tokenManager = Objects.requireNonNull(tokenManager, "tokenManager must not be null");
    }

    /**
     * Returns an instance of ServiceAccount for the specified IBM Globalization
     * Pipeline service URL and credentials.
     * <p>
     * All arguments must not be null.
     * 
     * @param url           The service URL of Globlization Pipeline service.
     *                      (e.g. https://gp-rest.ng.bluemix.net/translate/rest)
     * @param instanceId    The instance ID of the service instance.
     *                      (e.g. d3f537cd617f34c86ac6b270f3065e73)
     * @param userId        The user ID for the service instance.
     *                      (e.g. e92a1282a0e4f97bec93aa9f56fdb838)
     * @param password      The password for the service instance.
     *                      (e.g. zg5SlD+ftXYRIZDblLgEA/ILkkCNqE1y)
     * @return An instance of ServiceAccount
     */
    public static ServiceAccount getInstance(String url, String instanceId,
            String userId, String password) {

        if (url.endsWith("/")) {
            // trim off trailing slash
            url = url.substring(0, url.length() - 1);
        }

        return new ServiceAccount(url, instanceId, userId, password);
    }

    /**
     * Returns an instance of ServiceAccount for the specified IBM Globalization
     * Pipeline service URL and credentials.
     * <p>
     * All arguments must no be null.
     * 
     * @param url
     *            The service URL of Globlization Pipeline service. (e.g.
     *            https://gp-rest.ng.bluemix.net/translate/rest)
     * @param instanceId
     *            The instance ID of the service instance. (e.g.
     *            d3f537cd617f34c86ac6b270f3065e73)
     * @param tokenManager
     *            IAM Token Manager.
     * 
     * @return An instance of ServiceAccount
     */
    public static ServiceAccount getInstance(String url, String instanceId,
            TokenManager tokenManager) {

        if (url.endsWith("/")) {
            // trim off trailing slash
            url = url.substring(0, url.length() - 1);
        }

        return new ServiceAccount(url, instanceId, tokenManager);
    }

    /**
     * Returns an instance of ServiceAccount. This factory method tries below in order.
     *  <ol>
     *      <li>Checks environment variables <code>GP_URL</code> (service URL - e.g.
     *      https://gp-rest.ng.bluemix.net/translate/rest), <code>GP_INSTANCE_ID</code>
     *      (service instance ID - e.g. d3f537cd617f34c86ac6b270f3065e73),
     *      <code>GP_USER_ID</code> (user ID - e.g. e92a1282a0e4f97bec93aa9f56fdb838)
     *      and <code>GP_PASSWORD</code> (password - e.g. zg5SlD+ftXYRIZDblLgEA/ILkkCNqE1y).
     *      If all of above are defined, this method calls
     *      {@link #getInstance(String, String, String, String)} with these environment
     *      variable values.</li>
     *
     *      <li>Checks <code>VCAP_SERVICES</code> environment variable. If the variable
     *      is defined and an entry for IBM Globalization Pipeline service is available,
     *      use the values in the JSON <code>credentials</code> object. When multiple entries
     *      for IBM Globalization Pipeline service are found, the first one will be used.
     *      unless <code>GP_SERVICE_NAME</code> and/or <code>GP_SERVICE_INSTANCE_NAME</code>
     *      are not defined.</li>
     *
     *      <li>If both of above failed, returns null.</li>
     * </ol>
     * @return  An instance of ServiceAccount, or null if service configuration is
     *          not available.
     */
    public static ServiceAccount getInstance() {
        ServiceAccount account = getInstanceByEnvVars();
        if (account == null) {
            Map<String, String> env = System.getenv();
            String serviceName = env.get(GP_SERVICE_NAME);
            String serviceInstanceName = env.get(GP_SERVICE_INSTANCE_NAME);
            account = getInstanceByVcapServices(serviceName, serviceInstanceName);
        }
        return account;
    }

    /**
     * Returns an instance of ServiceAccount for the specified IBM Globalization
     * Pipeline service name and service instance name from VCAP_SERVICES environment
     * variable.
     * <p>
     * If no matching service instance entry is found, this method returns null.
     * 
     * @param serviceName
     *          The name of IBM Globalization Pipeline service.
     *          When null, the default service name pattern will be used.
     * @param serviceInstanceName
     *          The name of IBM Globalization Pipeline service instance.
     *          When null, the first matching entry will be used.
     * @return  An instance of ServiceAccount for the specified service
     *          name and service instance name, or null if the matching
     *          entry was not found.
     */
    public static ServiceAccount getInstanceForService(String serviceName, String serviceInstanceName) {
        return getInstanceByVcapServices(serviceName, serviceInstanceName);
    }

    /**
     * Returns an instance of ServiceAccount from the environment variables -
     * GP_URL, GP_INSTANCE_ID, GP_USER_ID and GP_PASSWORD.
     * <p>
     * If any of these are not defined, this method returns null.
     * 
     * @return  An instance of ServiceAccount initialized by the environment
     *          variables, or null if any of these are not defined.
     */
    private static ServiceAccount getInstanceByEnvVars() {
        Map<String, String> env = System.getenv();
        String url = env.get(GP_URL);
        String instanceId = env.get(GP_INSTANCE_ID);
        String userId = env.get(GP_USER_ID);
        String password = env.get(GP_PASSWORD);
        String apiKey = env.get(GP_IAM_API_KEY);
        String iamBearerToken=env.get(GP_IAM_BEARER_TOKEN);
        String iamEndpoint=env.get(GP_IAM_ENDPOINT);
        
        return getInstance(url, instanceId, userId, password, apiKey,
                iamBearerToken, iamEndpoint);
        
    }

    /**
     * @param url           
     *          The service URL of Globlization Pipeline service.
     *          (e.g. https://gp-rest.ng.bluemix.net/translate/rest)
     * @param instanceId    
     *          The instance ID of the service instance.
     *          (e.g. d3f537cd617f34c86ac6b270f3065e73)
     * @param userId        
     *          The user ID for the service instance.
     *          (e.g. e92a1282a0e4f97bec93aa9f56fdb838)
     * @param password      
     *          The password for the service instance.
     *          (e.g. zg5SlD+ftXYRIZDblLgEA/ILkkCNqE1y)
     * @param iamEndpoint
     *          IAM endpoint.
     * @param iamBearerToken
     *          IAM Bearer token.
     * @param apiKey
     *          IAM API Key.
     * @return An instance of ServiceAccount, or null if essential params are not available.
     */
     static ServiceAccount getInstance(String url, String instanceId,
            String userId, String password, String apiKey,
            String iamBearerToken, String iamEndpoint) {
        if(url == null || instanceId == null) {
            logger.config(
                    "Not enough environment variables to initialize an instance of ServiceAccount with either Globalization Pipeline Authentication or IAM Authorization. Globalization Pipeline URL and instance id are required.");
            return null;
        }
        if (userId == null || password == null) {
            logger.config("Not enough environment variables to initialize an instance of ServiceAccount supporting Globalization Pipeline Authentication.");
        }
        else {
            ServiceAccount account = getInstance(url, instanceId, userId, password);
            logger.config("A ServiceAccount is created from environment variables: GP_URL="
                    + url + ", GP_INSTANCE_ID=" + instanceId + ", GP_USER_ID=" +
                    userId + ", GP_PASSWORD=***");

            return account;
        }
        if(iamEndpoint==null||iamEndpoint.isEmpty()) {
            logger.config("Not enough environment variables to initialize an instance of ServiceAccount supporting IAM Authorization. IAM endpoint is either not set or blank.");
            return null;
        }
        if (apiKey==null||apiKey.isEmpty()) {
            logger.config("Not enough environment variables to initialize an instance of ServiceAccount supporting IAM Authorization using IAM API Key.");
        }
        else {
            ServiceAccount account = getInstance(url, instanceId, TokenManagerFactory.getTokenLifeCycleManager(iamEndpoint, apiKey));
            logger.config("A ServiceAccount is created from environment variables: GP_URL="
                    + url + ", GP_INSTANCE_ID=" + instanceId + ", GP_IAM_API_KEY=***");

            return account;
        }
        
        if (iamBearerToken==null||iamBearerToken.isEmpty()) {
            logger.config("Not enough environment variables to initialize an instance of ServiceAccount supporting IAM Authorization using IAM bearer token.");
            return null;
        }
        else {
            ServiceAccount account = getInstance(url, instanceId, TokenManagerFactory.getTokenManager(iamBearerToken));
            logger.config("A ServiceAccount is created from environment variables: GP_URL="
                    + url + ", GP_INSTANCE_ID=" +instanceId+ ", GP_IAM_BEARER_TOKEN=***");
            return account;
        }
    }

    /**
     * Returns an instance of ServiceAccount from the VCAP_SERVICES environment
     * variable.
     * <p>
     * When <code>serviceInstanceName</code> is null, this method returns
     * a ServiceAccount for the first valid IBM Globlization Pipeline service instance.
     * If <code>serviceInstanceName</code> is not null, this method look up a
     * matching service entry, and returns a ServiceAccount for the matching entry.
     * If <code>serviceInstanceName</code> is not null and there is no match,
     * this method returns null.
     * 
     * @param serviceName
     *          The name of IBM Globalization Pipeline service. If null,
     *          the first service with a name matching GP_SERVICE_NAME_PATTERN
     *          will be used.
     * @param serviceInstanceName
     *          The name of IBM Globalization Pipeline service instance, or null
     *          designating the first available service instance.
     * @return  An instance of ServiceAccount for the specified service instance
     *          name, or null.
     */
    private static ServiceAccount getInstanceByVcapServices(String serviceName, String serviceInstanceName) {
        Map<String, String> env = System.getenv();
        String vcapServices = env.get("VCAP_SERVICES");
        if (vcapServices == null) {
            return null;
        }

        try {
            JsonObject obj = new JsonParser().parse(vcapServices).getAsJsonObject();

            // When service name is specified,
            // this method returns only a matching entry
            if (serviceName != null) {
                JsonElement elem = obj.get(serviceName);
                if (elem != null && elem.isJsonArray()) {
                    return parseToServiceAccount(elem.getAsJsonArray(), serviceInstanceName);
                }
            }

            // Otherwise, lookup a valid entry with a name matching
            // the default pattern GP_SERVICE_NAME_PATTERN.
            for (Entry<String, JsonElement> entry : obj.entrySet()) {
                String name = entry.getKey();
                JsonElement elem = entry.getValue();
                if (elem.isJsonArray() && GP_SERVICE_NAME_PATTERN.matcher(name).matches()) {
                    ServiceAccount account = parseToServiceAccount(elem.getAsJsonArray(), serviceInstanceName);
                    if (account != null) {
                        return account;
                    }
                }
            }
        } catch (JsonParseException e) {
            // Fall through - will return null
        }

        return null;
    }

    /**
     * Returns an instance of ServiceAccount for a JsonArray in the VCAP_SERVICES environment.
     * <p>
     * This method is called from {@link #getInstanceByVcapServices(String, String)}.
     * 
     * @param jsonArray
     *          The candidate JSON array which may include valid credentials.
     * @param serviceInstanceName
     *          The name of IBM Globalization Pipeline service instance, or null
     *          designating the first available service instance.
     * @return  An instance of ServiceAccount. This method returns null if no matching service
     *          instance name was found (when serviceInstanceName is null), or no valid
     *          credential data was found. 
     */
    private static ServiceAccount parseToServiceAccount(JsonArray jsonArray, String serviceInstanceName) {
        ServiceAccount account = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject gpObj = jsonArray.get(i).getAsJsonObject();
            if (serviceInstanceName != null) {
                // When service instance name is specified,
                // this method returns only a matching entry
                JsonPrimitive name = gpObj.getAsJsonPrimitive("name");
                if (name == null || !serviceInstanceName.equals(name.getAsString())) {
                    continue;
                }
            }
            JsonObject credentials = gpObj.getAsJsonObject("credentials");
            if (credentials == null) {
                continue;
            }
            JsonPrimitive jsonUrl = credentials.getAsJsonPrimitive("url");
            JsonPrimitive jsonInstanceId = credentials.getAsJsonPrimitive("instanceId");
            JsonPrimitive jsonUserId = credentials.getAsJsonPrimitive("userId");
            JsonPrimitive jsonPassword = credentials.getAsJsonPrimitive("password");
            JsonPrimitive jsonIamEndpoint = credentials.getAsJsonPrimitive("iam_endpoint");
            JsonPrimitive jsonIamApiKey = credentials.getAsJsonPrimitive("apikey");


            if (jsonUrl != null && jsonInstanceId != null) {
                if (jsonUserId != null && jsonPassword != null) {
                    account = getInstance(jsonUrl.getAsString(),
                            jsonInstanceId.getAsString(),
                            jsonUserId.getAsString(),
                            jsonPassword.getAsString());
                    logger.config(
                            "A ServiceAccount is created from VCAP_SERVICES: url="
                                    + jsonUrl + ", instanceId=" + jsonInstanceId
                                    + ", userId=" + jsonUserId
                                    + ", password=***");
                    break;
                } else if (jsonIamEndpoint != null && jsonIamApiKey != null) {
                    account = getInstance(jsonUrl.getAsString(),
                            jsonInstanceId.getAsString(),
                            TokenManagerFactory.getTokenLifeCycleManager(
                                    jsonIamEndpoint.getAsString(),
                                    jsonIamApiKey.getAsString()));
                    logger.config(
                            "A ServiceAccount is created from VCAP_SERVICES: url="
                                    + jsonUrl + ", instanceId=" + jsonInstanceId
                                    + ", iam_endpoint=" + jsonIamEndpoint
                                    + ", apikey=***");
                    break;
                }
            }
        }

        return account;
    }
    
    /**
     * Is this is an IAM enabled account?
     * @return true if account is IAM enabled.
     */
    public boolean isIamEnabled() {
        return tokenManager!=null;
    }
    
    /**
     * IAM bearer token for the account.
     * @return IAM bearer token for the account
     */
    public String getIamToken() throws TokenManagerException  {
       return tokenManager.getToken();
    }
    
    /**
     * Returns the URL of IBM Globalization Pipeline service.
     * 
     * @return The URL of IBM Globalization Pipeline service.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the ID of IBM Globalization Pipeline service instance.
     * 
     * @return The ID of IBM Globalization Pipeline service instance.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Returns the user ID used for the service instance.
     * @return The user ID used for the service instance.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the password used for the service instance.
     * @return The password used for the service instance.
     */
    public String getPassword() {
        return password;
    }
}
