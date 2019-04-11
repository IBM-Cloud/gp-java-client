/*
 * Copyright IBM Corp. 2019
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
package com.ibm.g11n.pipeline.iam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Manages IAM token for the given IAM API key. By default a stored token will
 * be considered expired when 85% of its valid time has passed. To change this
 * default value, property IAM_TOKEN_EXPIRY_THRESHOLD can be set between
 * 0.1(excluding) and 1(excluding). For example, for token to be considered
 * expired when 75% of its valid time has passed, set it to 0.75.
 * 
 * @author Siddharth Jain
 *
 */
public class TokenLifeCycleManager implements TokenManager {
    private static class IAMToken{
        private String access_token;
        private String refresh_token;
        private String token_type;
        private long expires_in;
        private long expiration;
        private String scope;
    }
    private static final Gson GSON = new Gson();
    private static final Map<String,TokenLifeCycleManager> instances=new ConcurrentHashMap<>();
    private double tokenExpiryThreshold=0.85;
    private final String iamTokenApiUrl;
    private volatile String token;
    //Calculated according to the expiry threshold
    private volatile long expiresAt;
    private final String iamApiKey;
    static final String IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY="IAM_TOKEN_EXPIRY_THRESHOLD";

    private TokenLifeCycleManager(final String iamEndpoint,final String apiKey) {
        if(iamEndpoint==null||iamEndpoint.isEmpty()) {
            throw new IllegalArgumentException("Cannot initialize with null or empty IAM endpoint.");
        }
        if(apiKey==null||apiKey.isEmpty()) {
            throw new IllegalArgumentException("Cannot initialize with null or empty IAM apiKey.");
        }
        this.iamTokenApiUrl=iamEndpoint+"/identity/token";
        this.iamApiKey=apiKey;
        this.expiresAt=System.nanoTime();
        final double threshold=Double.parseDouble(System.getProperty(IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY,"0.85"));
        if(threshold>0.1 && threshold<1) {
            this.tokenExpiryThreshold=threshold;
        }
        else {
            throw new IllegalArgumentException("IAM_TOKEN_EXPIRY_THRESHOLD can be set between 0.1(excluding) and 1(excluding");
        }

    }


    /**
     * Logic for getting token:<br>
     * 1. If has a valid token stored, send it.<br>
     * 2. If the stored token has expired, replace it with a new fetched token.<br>
     * Note: This method is thread-safe and makes only one call to IAM API to replace an expired IAM token.
     */
    @Override
    public String getToken() throws TokenManagerException {
        if (hasTokenExpired()) {
            synchronized (this) {
                if (hasTokenExpired()) {
                    try {
                        final IAMToken iamToken = invokeTokenApi();
                        expiresAt = (long) (TimeUnit.SECONDS.toNanos(
                                iamToken.expires_in) * tokenExpiryThreshold)
                                + System.nanoTime();
                        token = iamToken.access_token;
                    } catch (final IAMTokenException e) {
                        throw new TokenManagerException("Failed getting Token.",
                                e);
                    }

                }
            }
        }
        return token;
    }

    private boolean hasTokenExpired() {
        return expiresAt-System.nanoTime()<0;
    }

    private IAMToken invokeTokenApi() throws IAMTokenException {
        try {
            final byte[] reqBody = generateRequestBody();
            final HttpURLConnection conn = generateRequest(reqBody);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(reqBody);
            }
            final int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                return GSON.fromJson(getResponseBody(conn), IAMToken.class);
            } else {
                throw new IAMTokenException(
                        "Error in fetching token from IAM token API:"
                                + iamTokenApiUrl + ",Token API response status:"
                                + status + ",content type:"
                                + conn.getContentType() + ", body:"
                                + getResponseBody(conn));
            }
        } catch (final Exception e) {
            throw new IAMTokenException(
                    "Could not complete fetching token from token API:"
                            + iamTokenApiUrl,
                            e);
        }
    }

    private HttpURLConnection generateRequest(final byte[] reqBody) throws IOException {
        final URL targetUrl = new URL(iamTokenApiUrl);
        final HttpURLConnection conn = (HttpURLConnection)targetUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-Length", Integer.toString(reqBody.length));
        return conn;
    }


    private String getResponseBody(final HttpURLConnection conn) throws IOException {
        int bodyLen = conn.getContentLength();
        if (bodyLen < 0) {
            bodyLen = 2048; // default length for initial byte array
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(bodyLen);
        try(InputStream is = conn.getErrorStream()==null?conn.getInputStream():conn.getErrorStream()){
            final byte[] buf = new byte[2048];
            int bytes;
            while ((bytes = is.read(buf)) != -1) {
                baos.write(buf, 0, bytes);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }

    }

    /**
     * Get an instance of TokenLifeCylceManager. Single instance is maintained
     * for each unique pair of iamEndpoint and apiKey. The method is thread
     * safe.
     * 
     * @param iamEndpoint
     *            IAM endpoint.
     * @param apiKey
     *            IAM API Key.
     * @return Instance of TokenLifeCylceManager
     */
    static TokenLifeCycleManager getInstance(final String iamEndpoint,
            final String apiKey) {
        if (iamEndpoint == null || iamEndpoint.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot initialize with null or empty IAM endpoint.");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot initialize with null or empty IAM apiKey.");
        }
        return getInstanceUnchecked(iamEndpoint, apiKey);
    }


    private static TokenLifeCycleManager getInstanceUnchecked(
            final String iamEndpoint, final String apiKey) {
        final String storeKey = iamEndpoint + apiKey;
        if (!instances.containsKey(storeKey)) {
            instances.putIfAbsent(storeKey,
                    new TokenLifeCycleManager(iamEndpoint, apiKey));
        }
        return instances.get(storeKey);
    }

    /**
     * Get an instance of TokenLifeCylceManager. Single instance is maintained
     * for each unique pair of iamEndpoint and apiKey. The method and the returned instance is thread
     * safe.
     * 
     * @param jsonCredentials
     *            Credentials in JSON format. The credentials should at minimum
     *            have these <key>:<value> pairs in the credentials json:
     *            {
     *              "apikey":"<IAM_API_KEY>",
     *              "iam_endpoint":"<IAM_ENDPOINT>"
     *            }
     * @return Instance of TokenLifeCylceManager.
     */
    static TokenLifeCycleManager getInstance(final String jsonCredentials) {
        final JsonObject credentials = new JsonParser().parse(jsonCredentials)
                .getAsJsonObject();
        if(credentials.get("apikey")==null || credentials.get("apikey").isJsonNull()||credentials.get("apikey").getAsString().isEmpty()) {
            throw new IllegalArgumentException(
                    "IAM API Key value is either not available, or null or is empty in credentials JSON.");
        }
        if(credentials.get("iam_endpoint")==null || credentials.get("iam_endpoint").isJsonNull()||credentials.get("iam_endpoint").getAsString().isEmpty()) {
            throw new IllegalArgumentException(
                    "IAM endpoint Key value is either not available, or null or is empty in credentials JSON.");
        }
        final String apiKey = credentials.get("apikey").getAsString();
        final String iamEndpoint = credentials.get("iam_endpoint").getAsString();

        return getInstanceUnchecked(iamEndpoint, apiKey);
    }

    private byte[] generateRequestBody() throws UnsupportedEncodingException {
        final StringBuilder builder=new StringBuilder();
        builder.append(URLEncoder.encode("grant_type","UTF-8")).append("=").append(URLEncoder.encode("urn:ibm:params:oauth:grant-type:apikey","UTF-8"))
        .append("&")
        .append(URLEncoder.encode("response_type","UTF-8")).append("=").append(URLEncoder.encode("cloud_iam", "UTF-8"))
        .append("&")
        .append(URLEncoder.encode("apikey","UTF-8")).append("=").append(URLEncoder.encode(iamApiKey, "UTF-8"));
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
