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
package com.ibm.gaas.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ibm.gaas.ServiceAccount;
import com.ibm.gaas.impl.pojo.ResourceData;
import com.ibm.gaas.impl.pojo.ResourceDataResponse;
import com.ibm.gaas.impl.pojo.ServiceResponse;

/**
 * GaaS REST service client implementation.
 * 
 * For now, there are only two methods necessary for CloudResourceBundle
 * implementation.
 * 
 * @author Yoshito Umaoka
 */
public class GaasRestServiceClient {
    private static final Logger logger = Logger.getLogger(GaasRestServiceClient.class.getName()); 

    private static final String API_KEY_HEADER = "api-key";

    private final ServiceAccount serviceAccount;

    public GaasRestServiceClient(ServiceAccount serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public ResourceData getResourceData(String projectId, Locale locale) {
        String relativePath = "v1/projects/" + projectId + "/" + locale.toLanguageTag();
        URL resUrl = concat(serviceAccount.getServiceUrl(), relativePath);
        if (resUrl == null) {
            logger.warning("Failed to compose URL - base url: " + serviceAccount.getServiceUrl()
                    + ", relative path: " + relativePath);
            return null;
        }

        ResourceData resData = null;
        String response = get(resUrl);
        if (response != null) {
            Gson gson = new Gson();
            try {
                ResourceDataResponse resDataResponse = gson.fromJson(response, ResourceDataResponse.class);
                if (resDataResponse.getStatus().equals("success")) {
                    resData = resDataResponse.getResourceData();
                }
            } catch (JsonSyntaxException e) {
                logger.warning("Failed to parse GET /v1/projects/{projectID} response - " + e.getMessage());
                return null;
            }
        }
        return resData;
    }

    public Map<String, Set<String>> getSupportedTranslation() {
        URL svcUrl = concat(serviceAccount.getServiceUrl(), "v1/service");
        if (svcUrl == null) {
            logger.warning("Failed to compose URL - base url: " + serviceAccount.getServiceUrl()
                    + ", relative path: v1/service");
            return null;
        }

        Map<String, Set<String>> supportedTranslation = null;
        String response = get(svcUrl);
        if (response != null) {
            Gson gson = new Gson();
            try {
                ServiceResponse serviceResponse = gson.fromJson(response, ServiceResponse.class);
                if (serviceResponse.getStatus().equals("success")) {
                    supportedTranslation = serviceResponse.getSupportedTranslation();
                }
            } catch (JsonSyntaxException e) {
                logger.warning("Failed to parse GET /v1/service response - " + e.getMessage());
                return null;
            }
        }
        return supportedTranslation;
    }

    private String get(URL target) {
        StringBuilder buf = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)target.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty(API_KEY_HEADER, serviceAccount.getApiKey());

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.warning("HTTP response code " + conn.getResponseCode() + " was returned by GET "
                        + target);
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line);
            }
            conn.disconnect();
        } catch (IOException e) {
            logger.warning("Failed to access " + target + " [GET]: " + e.getMessage());
            return null;
        }

        return buf.toString();
    }

    private static URL concat(URL base, String extra) {
        try {
            URI uri = base.toURI();
            String newPath = uri.getPath() + "/" + extra;
            URI newUri = uri.resolve(newPath);
            return newUri.toURL();
        } catch (URISyntaxException e) {
            // fall through
        } catch (MalformedURLException e) {
            // fall through
        }
        return null;
    }
}
