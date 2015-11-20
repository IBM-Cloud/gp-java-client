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
package com.ibm.g11n.pipeline.client.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import com.ibm.g11n.pipeline.client.BundleData;
import com.ibm.g11n.pipeline.client.BundleDataChangeSet;
import com.ibm.g11n.pipeline.client.BundleMetrics;
import com.ibm.g11n.pipeline.client.LanguageMetrics;
import com.ibm.g11n.pipeline.client.NewBundleData;
import com.ibm.g11n.pipeline.client.NewUserData;
import com.ibm.g11n.pipeline.client.ResourceEntryData;
import com.ibm.g11n.pipeline.client.ResourceEntryDataChangeSet;
import com.ibm.g11n.pipeline.client.ReviewStatusMetrics;
import com.ibm.g11n.pipeline.client.ServiceAccount;
import com.ibm.g11n.pipeline.client.ServiceClient;
import com.ibm.g11n.pipeline.client.ServiceException;
import com.ibm.g11n.pipeline.client.ServiceInfo;
import com.ibm.g11n.pipeline.client.TranslationStatus;
import com.ibm.g11n.pipeline.client.UserData;
import com.ibm.g11n.pipeline.client.UserDataChangeSet;
import com.ibm.g11n.pipeline.client.impl.ServiceResponse.Status;

/**
 * ServiceClient implementation by GSON and JDK's HttpURLConnection.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceClientImpl extends ServiceClient {

    public ServiceClientImpl(ServiceAccount account) {
        super(account);
    }

    private static class GetServiceInfoResponse extends ServiceResponse {
        Map<String, Set<String>> supportedTranslation;
    }

    @Override
    public ServiceInfo getServiceInfo() throws ServiceException {
        GetServiceInfoResponse resp = invokeApi(
                "GET",
                "$service/v2/info",
                null,
                GetServiceInfoResponse.class,
                true);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new ServiceInfoImpl(resp.supportedTranslation);
    }

    private static class GetBundleListResponse extends ServiceResponse {
        Set<String> bundleIds;
    }

    @Override
    public Set<String> getBundleIds() throws ServiceException {
        GetBundleListResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/bundles",
                null,
                GetBundleListResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return resp.bundleIds;
    }

    @Override
    public void createBundle(String bundleId, NewBundleData newBundleData)
            throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (newBundleData == null) {
            throw new IllegalArgumentException("newBundleData must be specified.");
        }

        Gson gson = createGson(NewBundleData.class.getName());
        String jsonBody = gson.toJson(newBundleData, NewBundleData.class);
        ServiceResponse resp = invokeApi(
                "PUT",
                account.getInstanceId() + "/v2/bundles/" + bundleId,
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    private static class GetBundleInfoResponse extends ServiceResponse {
        RestBundle bundle;
    }

    @Override
    public BundleData getBundleInfo(String bundleId) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }

        GetBundleInfoResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/bundles/" + bundleId,
                null,
                GetBundleInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new BundleDataImpl(resp.bundle);
    }

    private static class GetBundleMetricsResponse extends ServiceResponse {
        private Map<String, EnumMap<TranslationStatus, Integer>> translationStatusMetricsByLanguage;
        private Map<String, ReviewStatusMetrics> reviewStatusMetricsByLanguage;
        private Map<String, Map<String, Integer>> partnerStatusMetricsByLanguage;
    }

    @Override
    public BundleMetrics getBundleMetrics(String bundleId) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }

        GetBundleMetricsResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/bundles/" + bundleId
                    + "?fields=translationStatusMetricsByLanguage,reviewStatusMetricsByLanguage,partnerStatusMetricsByLanguage",
                null,
                GetBundleMetricsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new BundleMetricsImpl(
                resp.translationStatusMetricsByLanguage,
                resp.reviewStatusMetricsByLanguage,
                resp.partnerStatusMetricsByLanguage);
    }

    @Override
    public void updateBundle(String bundleId, BundleDataChangeSet changeSet)
            throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (changeSet == null) {
            throw new IllegalArgumentException("changeSet must be specified.");
        }

        Gson gson = createGson(BundleDataChangeSet.class.getName());
        String jsonBody = gson.toJson(changeSet, BundleDataChangeSet.class);
        ServiceResponse resp = invokeApi(
                "POST",
                account.getInstanceId() + "/v2/bundles/" + bundleId,
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    @Override
    public void deleteBundle(String bundleId) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }

        GetBundleInfoResponse resp = invokeApi(
                "DELETE",
                account.getInstanceId() + "/v2/bundles/" + bundleId,
                null,
                GetBundleInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    private static class GetResourceStringsResponse extends ServiceResponse {
        Map<String, String> resourceStrings;
    }

    @Override
    public Map<String, String> getResourceStrings(String bundleId,
            String language, boolean fallback) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }

        StringBuilder endpoint = new StringBuilder();
        endpoint
            .append(account.getInstanceId())
            .append("/v2/bundles/")
            .append(bundleId)
            .append("/")
            .append(language);
        if (fallback) {
            endpoint.append("?fallback=true");
        }

        GetResourceStringsResponse resp = invokeApi(
                "GET",
                endpoint.toString(),
                null,
                GetResourceStringsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return resp.resourceStrings;
    }

    private static class GetResourceEntriesResponse extends ServiceResponse {
        Map<String, RestResourceEntry> resourceEntries;
    }

    @Override
    public Map<String, ResourceEntryData> getResourceEntries(String bundleId,
            String language) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }

        GetResourceEntriesResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/bundles/" + bundleId + "/" + language
                    + "?fields=resourceEntries",
                null,
                GetResourceEntriesResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Map<String, ResourceEntryData> resultEntries = new TreeMap<String, ResourceEntryData>();
        if (resp.resourceEntries != null && !resp.resourceEntries.isEmpty()) {
            for (Entry<String, RestResourceEntry> entry : resp.resourceEntries.entrySet()) {
                resultEntries.put(entry.getKey(),
                        new ResourceEntryDataImpl(entry.getValue()));
            }
        }
        return resultEntries;
    }

    private static class GetLanguageMetricsResponse extends ServiceResponse {
        private EnumMap<TranslationStatus, Integer> translationStatusMetrics;
        private ReviewStatusMetrics reviewStatusMetrics;
        private Map<String, Integer> partnerStatusMetrics;
    }

    @Override
    public LanguageMetrics getLanguageMetrics(String bundleId, String language)
            throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }

        GetLanguageMetricsResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/bundles/" + bundleId + "/" + language
                    + "?fields=translationStatusMetrics,reviewStatusMetrics,partnerStatusMetrics",
                null,
                GetLanguageMetricsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new LanguageMetricsImpl(resp.translationStatusMetrics,
                resp.reviewStatusMetrics, resp.partnerStatusMetrics);
    }


    @Override
    public void uploadResourceStrings(String bundleId, String language,
            Map<String, String> strings) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }
        if (strings == null || strings.isEmpty()) {
            throw new IllegalArgumentException("strings must be specified.");
        }

        Gson gson = createGson(Map.class.getName());
        String jsonBody = gson.toJson(strings, Map.class);
        ServiceResponse resp = invokeApi(
                "PUT",
                account.getInstanceId() + "/v2/bundles/" + bundleId + "/" + language,
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    @Override
    public void updateResourceStrings(String bundleId, String language,
            Map<String, String> strings, boolean resync)
                    throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }
        if ((strings == null || strings.isEmpty()) && !resync) {
            throw new IllegalArgumentException("strings must be specified when resync is false.");
        }

        String jsonBody = null;
        if (strings == null || strings.isEmpty()) {
            jsonBody = "{}";
        } else {
            Gson gson = createGson(Map.class.getName());
            jsonBody = gson.toJson(strings, Map.class);
        }

        ServiceResponse resp = invokeApi(
                "POST",
                account.getInstanceId() + "/v2/bundles/" + bundleId + "/" + language,
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    private static class GetResourceEntryResponse extends ServiceResponse {
        RestResourceEntry resourceEntry;
    }

    @Override
    public ResourceEntryData getResourceEntry(String bundleId, String language,
            String resKey) throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }
        if (resKey == null || resKey.isEmpty()) {
            throw new IllegalArgumentException("resKey must be specified.");
        }

        GetResourceEntryResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/bundles/" + bundleId + "/" + language
                    + "/" + resKey,
                null,
                GetResourceEntryResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new ResourceEntryDataImpl(resp.resourceEntry);
    }

    @Override
    public void updateResourceEntry(String bundleId, String language,
            String resKey, ResourceEntryDataChangeSet changeSet)
                    throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }
        if (resKey == null || resKey.isEmpty()) {
            throw new IllegalArgumentException("resKey must be specified.");
        }
        if (changeSet == null) {
            throw new IllegalArgumentException("changeSet must be specified.");
        }

        Gson gson = createGson(ResourceEntryDataChangeSet.class.getName());
        String jsonBody = gson.toJson(changeSet, ResourceEntryDataChangeSet.class);
        ServiceResponse resp = invokeApi(
                "POST",
                account.getInstanceId() + "/v2/bundles/" + bundleId + "/" + language
                    + "/" + resKey,
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    private static class GetUsersResponse extends ServiceResponse {
        Map<String, RestUser> users;
    }

    @Override
    public Map<String, UserData> getUsers() throws ServiceException {
        GetUsersResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/users",
                null,
                GetUsersResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Map<String, UserData> resultUsers = new TreeMap<String, UserData>();
        for (Entry<String, RestUser> userEntry : resp.users.entrySet()) {
            String id = userEntry.getKey();
            RestUser user = userEntry.getValue();
            resultUsers.put(id, new UserDataImpl(user));
        }

        return resultUsers;
    }

    private static class UserResponse extends ServiceResponse {
        @SuppressWarnings("unused")
        String id;
        RestUser user;
    }

    @Override
    public UserData createUser(NewUserData newUserData)
            throws ServiceException {
        if (newUserData == null) {
            throw new IllegalArgumentException("newUserData must be specified.");
        }

        Gson gson = createGson(NewUserData.class.getName());
        String jsonBody = gson.toJson(newUserData, NewUserData.class);
        UserResponse resp = invokeApi(
                "POST",
                account.getInstanceId() + "/v2/users/new",
                jsonBody,
                UserResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new UserDataImpl(resp.user);
    }

    @Override
    public UserData getUser(String userId) throws ServiceException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must be specified.");
        }

        UserResponse resp = invokeApi(
                "GET",
                account.getInstanceId() + "/v2/users/" + userId,
                null,
                UserResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new UserDataImpl(resp.user);
    }

    @Override
    public UserData updateUser(String userId, UserDataChangeSet changeSet,
            boolean resetPassword) throws ServiceException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must be specified.");
        }
        if (changeSet == null && !resetPassword) {
            throw new IllegalArgumentException("changeSet must be specified when resetPassword is false");
        }

        StringBuilder endpoint = new StringBuilder();
        endpoint
            .append(account.getInstanceId())
            .append("/v2/users/")
            .append(userId);
        if (resetPassword) {
            endpoint.append("?resetPassword=true");
        }

        String jsonBody = null;
        if (changeSet == null) {
            jsonBody = "{}";
        } else {
            Gson gson = createGson(UserDataChangeSet.class.getName());
            jsonBody = gson.toJson(changeSet, UserDataChangeSet.class);
        }

        UserResponse resp = invokeApi(
                "POST",
                endpoint.toString(),
                jsonBody,
                UserResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new UserDataImpl(resp.user);
    }

    @Override
    public void deleteUser(String userId) throws ServiceException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must be specified.");
        }

        ServiceResponse resp = invokeApi(
                "DELETE",
                account.getInstanceId() + "/v2/users/" + userId,
                null,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }


    //
    // Private method used for calling REST endpoints
    //

    private <T> T invokeApi(String method, String apiPath, String inJson, Class<T> classOfT)
        throws ServiceException {
        return invokeApi(method, apiPath, inJson, classOfT, false);
    }

    private <T> T invokeApi(String method, String apiPath, String inJson, Class<T> classOfT,
            boolean anonymous) throws ServiceException {

        T responseObj = null;

        try (StringWriter out = new StringWriter()
        ) {
            String urlStr = account.getUrl() + "/" + apiPath;
            URL targetUrl = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)targetUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Accept", "application/json");

            // Date header
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateHeader = sdf.format(new Date());

            conn.setRequestProperty("Date", dateHeader);

            // Request body in UTF-8
            byte[] requestBody = null;
            if (inJson != null) {
                requestBody = inJson.getBytes("UTF-8");
            }

            // Authorization header
            if (!anonymous) {
                StringBuilder authHeader = new StringBuilder();
                String uid = account.getUserId();
                String secret = account.getPassword();

                switch (scheme) {
                case BASIC:
                    authHeader.append("Basic ");
                    authHeader.append(getBasicCredential(uid, secret));
                    break;

                case HMAC:
                    authHeader.append("GaaS-HMAC ");
                    authHeader.append(
                            getHmacCredential(uid, secret,
                                    method, urlStr, dateHeader, requestBody));
                    break;
                }

                conn.setRequestProperty("Authorization", authHeader.toString());
            }

            // write the JSON body
            if (requestBody != null) {
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.getOutputStream().write(requestBody);
            }

            // receiving response

            // int httpStatus = conn.getResponseCode();

            String ctype = conn.getContentType();
            if (!ctype.equalsIgnoreCase("application/json")) {
                throw new ServiceException("Received non-JSON response from " + method + " " + urlStr);
            } else {
                InputStream is = conn.getErrorStream();
                if (is == null) {
                    is = conn.getInputStream();
                }

                try (BufferedReader br =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"))
                ) {
                    char[] buf = new char[1024];
                    int numChars;

                    while ((numChars = br.read(buf)) != -1) {
                        out.write(buf, 0, numChars);
                    }
                }
            }

            out.flush();
            String strResponse = out.toString();
            StringReader resReader = new StringReader(strResponse);
            Gson gson = createGson(classOfT.getName());
            responseObj = gson.fromJson(resReader, classOfT);

        } catch (Exception e) {
            // Error handling
            String errMsg = "Error while processing API request " + method + " " + apiPath;
            throw new ServiceException(errMsg, e);
        }

        return responseObj;
    }


    private static final char SEP = ':';

    //
    // Basic credential
    //
    private static String getBasicCredential(String uid, String secret) {
        if (uid == null || secret == null) {
            throw new IllegalArgumentException("uid and secrent must not be null");
        }
        String token = uid + ":" + secret;
        try {
            return DatatypeConverter.printBase64Binary(token.getBytes("ISO-8859-1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //
    // Globalization Pipeline HMAC credential
    //

    private static final byte LINE_SEP = 0x0A;
    private static final String ENC = "ISO-8859-1";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static String getHmacCredential(String uid, String secret, String method, String url,
            String rfc1123Date, byte[] body) {
        if (uid == null || method == null || url == null || rfc1123Date == null) {
            throw new IllegalArgumentException("uid, secret, method, url and rfc1123Date must not be null");
        }

        StringBuilder credential = new StringBuilder(uid);
        credential.append(SEP);

        try {
            // Actual secret used by GaaS looks like: "zg5SlD+ftXYRIZDblLgEA/ILkkCNqE1y"
            // This is actually a base64 encoded random bytes. Although we could
            // get original random bytes by decoding base64, but we don't do it because
            // it can be any 'String' in future. We simply get byte[] expression of the
            // secret 'String' (which is restricted to a subset of US-ASCII).
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(ENC), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(key);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(method.getBytes(ENC));
            baos.write(LINE_SEP);
            baos.write(url.getBytes(ENC));
            baos.write(LINE_SEP);
            baos.write(rfc1123Date.getBytes(ENC));
            baos.write(LINE_SEP);
            if (body != null) {
                baos.write(body);
            }
            byte[] msg = baos.toByteArray();

            // signing
            byte[] hmac = mac.doFinal(msg);
            credential.append(DatatypeConverter.printBase64Binary(hmac));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        return credential.toString();
    }

    //
    // Custom JSON deserialization code supporting Java Enum
    //

    private static class EnumMapInstanceCreator<K extends Enum<K>, V>
            implements InstanceCreator<EnumMap<K, V>> {
        private final Class<K> enumClazz;

        public EnumMapInstanceCreator(final Class<K> enumClazz) {
            super();
            this.enumClazz = enumClazz;
        }

        @Override
        public EnumMap<K, V> createInstance(final Type type) {
            return new EnumMap<K, V>(enumClazz);
        }
    }

    private static Gson createGson(String className) {
        GsonBuilder builder = new GsonBuilder();
        // ISO8601 date format support
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        if (className.equals("com.ibm.g11n.pipeline.client.impl.ServiceClientImpl$GetBundleMetricsResponse")
                || className.equals("com.ibm.g11n.pipeline.client.impl.ServiceClientImpl$GetLanguageMetricsResponse")){
            // Custom EnumMap mapping
            builder.registerTypeAdapter(
                    new TypeToken<EnumMap<TranslationStatus, Integer>>() {
                    }.getType(),
                    new EnumMapInstanceCreator<TranslationStatus, Integer>(
                            TranslationStatus.class));
        }
        return builder.create();
    }
}
