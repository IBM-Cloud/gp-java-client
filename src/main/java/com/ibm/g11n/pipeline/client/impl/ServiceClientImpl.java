/*  
 * Copyright IBM Corp. 2015, 2018
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ibm.g11n.pipeline.client.BundleData;
import com.ibm.g11n.pipeline.client.BundleDataChangeSet;
import com.ibm.g11n.pipeline.client.BundleMetrics;
import com.ibm.g11n.pipeline.client.DocumentData;
import com.ibm.g11n.pipeline.client.DocumentDataChangeSet;
import com.ibm.g11n.pipeline.client.DocumentTranslationRequestData;
import com.ibm.g11n.pipeline.client.DocumentTranslationRequestDataChangeSet;
import com.ibm.g11n.pipeline.client.DocumentType;
import com.ibm.g11n.pipeline.client.LanguageMetrics;
import com.ibm.g11n.pipeline.client.MTServiceBindingData;
import com.ibm.g11n.pipeline.client.NewBundleData;
import com.ibm.g11n.pipeline.client.NewDocumentData;
import com.ibm.g11n.pipeline.client.NewDocumentTranslationRequestData;
import com.ibm.g11n.pipeline.client.NewResourceEntryData;
import com.ibm.g11n.pipeline.client.NewTranslationConfigData;
import com.ibm.g11n.pipeline.client.NewTranslationRequestData;
import com.ibm.g11n.pipeline.client.NewUserData;
import com.ibm.g11n.pipeline.client.ResourceEntryData;
import com.ibm.g11n.pipeline.client.ResourceEntryDataChangeSet;
import com.ibm.g11n.pipeline.client.ReviewStatusMetrics;
import com.ibm.g11n.pipeline.client.SegmentData;
import com.ibm.g11n.pipeline.client.ServiceAccount;
import com.ibm.g11n.pipeline.client.ServiceClient;
import com.ibm.g11n.pipeline.client.ServiceException;
import com.ibm.g11n.pipeline.client.ServiceInfo;
import com.ibm.g11n.pipeline.client.ServiceInstanceInfo;
import com.ibm.g11n.pipeline.client.TranslationConfigData;
import com.ibm.g11n.pipeline.client.TranslationRequestData;
import com.ibm.g11n.pipeline.client.TranslationRequestDataChangeSet;
import com.ibm.g11n.pipeline.client.TranslationRequestStatus;
import com.ibm.g11n.pipeline.client.TranslationStatus;
import com.ibm.g11n.pipeline.client.UserData;
import com.ibm.g11n.pipeline.client.UserDataChangeSet;
import com.ibm.g11n.pipeline.client.impl.BundleDataImpl.RestBundle;
import com.ibm.g11n.pipeline.client.impl.DocumentDataImpl.RestDocument;
import com.ibm.g11n.pipeline.client.impl.DocumentTranslationRequestDataImpl.RestDocumentTranslationRequest;
import com.ibm.g11n.pipeline.client.impl.DocumentTranslationRequestDataImpl.RestInputDocumentTranslationRequestData;
import com.ibm.g11n.pipeline.client.impl.MTServiceBindingDataImpl.RestMTServiceBinding;
import com.ibm.g11n.pipeline.client.impl.ResourceEntryDataImpl.RestResourceEntry;
import com.ibm.g11n.pipeline.client.impl.SegmentDataImpl.RestSegmentData;
import com.ibm.g11n.pipeline.client.impl.ServiceInfoImpl.ExternalServiceInfoImpl.RestExternalServiceInfo;
import com.ibm.g11n.pipeline.client.impl.ServiceInstanceInfoImpl.RestServiceInstanceInfo;
import com.ibm.g11n.pipeline.client.impl.ServiceResponse.Status;
import com.ibm.g11n.pipeline.client.impl.TranslationConfigDataImpl.RestTranslationConfigData;
import com.ibm.g11n.pipeline.client.impl.TranslationRequestDataImpl.RestInputTranslationRequestData;
import com.ibm.g11n.pipeline.client.impl.TranslationRequestDataImpl.RestTranslationRequest;
import com.ibm.g11n.pipeline.client.impl.UserDataImpl.RestUser;

/**
 * ServiceClient implementation by GSON and JDK's HttpURLConnection.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceClientImpl extends ServiceClient {

    public ServiceClientImpl(ServiceAccount account) {
        super(account);
    }

    //
    // Service API
    //

    private static class GetServiceInfoResponse extends ServiceResponse {
        Map<String, Set<String>> supportedTranslation;
        Collection<RestExternalServiceInfo> externalServices;
    }

    @Override
    public ServiceInfo getServiceInfo() throws ServiceException {
        GetServiceInfoResponse resp = invokeApiJson(
                "GET",
                "$service/v2/info",
                null,
                GetServiceInfoResponse.class,
                true);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new ServiceInfoImpl(resp.supportedTranslation, resp.externalServices);
    }

    //
    // Instance API
    //

    private static class GetServiceInstanceInfoResponse extends ServiceResponse {
        RestServiceInstanceInfo instance;
    }

    @Override
    public ServiceInstanceInfo getServiceInstanceInfo() throws ServiceException {
        GetServiceInstanceInfoResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/instance/info",
                null,
                GetServiceInstanceInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new ServiceInstanceInfoImpl(resp.instance);
    }

    //
    // Bundle API
    //

    private static class GetBundleListResponse extends ServiceResponse {
        Set<String> bundleIds;
    }

    @Override
    public Set<String> getBundleIds() throws ServiceException {
        GetBundleListResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles",
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
        ServiceResponse resp = invokeApiJson(
                "PUT",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId),
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

        GetBundleInfoResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId),
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

        GetBundleMetricsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId)
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
        ServiceResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId),
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

        GetBundleInfoResponse resp = invokeApiJson(
                "DELETE",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId),
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
            .append(escapePathSegment(account.getInstanceId()))
            .append("/v2/bundles/")
            .append(escapePathSegment(bundleId))
            .append("/")
            .append(language);
        if (fallback) {
            endpoint.append("?fallback=true");
        }

        GetResourceStringsResponse resp = invokeApiJson(
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

        GetResourceEntriesResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language
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

        GetLanguageMetricsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language
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
        if (strings == null || strings.isEmpty()) {
            throw new IllegalArgumentException("strings must be specified.");
        }
        Map<String, NewResourceEntryData> newResourceEntries = new HashMap<>(strings.size());
        for (Entry<String, String> res : strings.entrySet()) {
            String key = res.getKey();
            String value = res.getValue();
            if (value == null) {
                newResourceEntries.put(key, null);
            } else {
                NewResourceEntryData newEntry = new NewResourceEntryData(value);
                newResourceEntries.put(key, newEntry);
            }
        }
        uploadResourceEntries(bundleId, language, newResourceEntries);
    }

    @Override
    public void uploadResourceEntries(String bundleId, String language,
            Map<String, NewResourceEntryData> newResourceEntries)
            throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }
        if (newResourceEntries == null || newResourceEntries.isEmpty()) {
            throw new IllegalArgumentException("newResourceEntries must be specified.");
        }

        Gson gson = createGson(Map.class.getName());
        String jsonBody = gson.toJson(newResourceEntries, Map.class);
        ServiceResponse resp = invokeApiJson(
                "PUT",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language,
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
        if ((strings == null || strings.isEmpty()) && !resync) {
            throw new IllegalArgumentException("strings must be specified when resync is false.");
        }

        Map<String, ResourceEntryDataChangeSet> resourceEntries = null;
        if (strings != null) {
            resourceEntries = new HashMap<>(strings.size());
            for (Entry<String, String> stringRes : strings.entrySet()) {
                String key = stringRes.getKey();
                String value = stringRes.getValue();
                if (value == null) {
                    resourceEntries.put(key, null);
                } else {
                    ResourceEntryDataChangeSet entry = new ResourceEntryDataChangeSet();
                    entry.setValue(value);
                    resourceEntries.put(key, entry);
                }
            }
        }
        updateResourceEntries(bundleId, language, resourceEntries, resync);
    }

    @Override
    public void updateResourceEntries(String bundleId, String language,
            Map<String, ResourceEntryDataChangeSet> resourceEntries, boolean resync)
            throws ServiceException {
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("language must be specified.");
        }

        String jsonBody = null;
        if (resourceEntries == null || resourceEntries.isEmpty()) {
            jsonBody = "{}";
        } else {
            Gson gson = createGson(Map.class.getName());
            jsonBody = gson.toJson(resourceEntries, Map.class);
        }

        ServiceResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language,
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

        GetResourceEntryResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language
                    + "/" + escapePathSegment(resKey),
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
        ServiceResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language
                    + "/" + escapePathSegment(resKey),
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    //
    // Document API
    //

    private static class GetDocumentListResponse extends ServiceResponse {
        Set<RestDocument> documentDataSet;
    }
    
    @Override
    public Set<String> getDocumentIds(DocumentType type) throws ServiceException {
        GetDocumentListResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/documents/" + type.toString().toLowerCase(),
                null,
                GetDocumentListResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Set<String> result = new TreeSet<>();
        if (resp.documentDataSet != null) {
            for (RestDocument doc : resp.documentDataSet) {
                result.add(doc.getDocumentId());
            }
        }
        return result;
    }

    @Override
    public void createDocument(String documentId, DocumentType type, NewDocumentData newDocumentData)
            throws ServiceException {
        if (newDocumentData == null) {
            throw new IllegalArgumentException("newDocumentData must be specified.");
        }

        Gson gson = createGson(NewBundleData.class.getName());
        String jsonBody = gson.toJson(newDocumentData, NewDocumentData.class);
        ServiceResponse resp = invokeApiJson(
                "PUT",
                escapePathSegment(account.getInstanceId()) + "/v2/documents/"
                    + type.toString().toLowerCase() + "/"
                    + escapePathSegment(documentId),
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }
    
    private static class GetDocumentInfoResponse extends ServiceResponse {
        RestDocument documentData;
    }

    @Override
    public DocumentData getDocumentInfo(String documentId, DocumentType type) throws ServiceException {
        if (Strings.isNullOrEmpty(documentId)) {
            throw new IllegalArgumentException("documentId must be specified.");
        }

        GetDocumentInfoResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/documents/"
                    + type.toString().toLowerCase() + "/"
                    + escapePathSegment(documentId),
                null,
                GetDocumentInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new DocumentDataImpl(resp.documentData);
    }
    
    @Override
    public void updateDocument(String documentId, DocumentType type, DocumentDataChangeSet changeSet)
            throws ServiceException {
        if (Strings.isNullOrEmpty(documentId)) {
            throw new IllegalArgumentException("documentId must be specified.");
        }
        if (changeSet == null) {
            throw new IllegalArgumentException("changeSet must be specified.");
        }

        Gson gson = createGson(DocumentDataChangeSet.class.getName());
        String jsonBody = gson.toJson(changeSet, DocumentDataChangeSet.class);
        ServiceResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/documents/"
                    + type.toString().toLowerCase() + "/"
                    + escapePathSegment(documentId),
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }
    
    @Override
    public void deleteDocument(String documentId, DocumentType type) throws ServiceException {

        GetDocumentInfoResponse resp = invokeApiJson(
                "DELETE",
                escapePathSegment(account.getInstanceId()) + "/v2/documents/"
                    + type.toString().toLowerCase() + "/"
                    + escapePathSegment(documentId),
                null,
                GetDocumentInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    @Override
    public void updateDocumentContent(String documentId, DocumentType type, String language,
            File file)
            throws ServiceException {
        if (Strings.isNullOrEmpty(documentId)) {
            throw new IllegalArgumentException("documentId must be specified.");
        }
        if (Strings.isNullOrEmpty(language)) {
            throw new IllegalArgumentException("language must be specified.");
        }
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("file must be a regular file.");
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the document content from "
                    + file.getName() + ": " + e.getMessage(), e);
        }

        ServiceResponse resp = invokeApiInputStream(
                "PUT",
                escapePathSegment(account.getInstanceId()) + "/v2/documents/"
                        + type.toString().toLowerCase() + "/"
                        + documentId + "/"
                        + language,
                type == DocumentType.HTML ? "text/html" : "text/plain",
                fis,
                ServiceResponse.class,
                false);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

    }
    @Override
    public byte[] getDocumentContent(String documentId, DocumentType type, String language)
            throws ServiceException {
        if (Strings.isNullOrEmpty(documentId)) {
            throw new IllegalArgumentException("documentId must be specified.");
        }
        if (Strings.isNullOrEmpty(language)) {
            throw new IllegalArgumentException("language must be specified.");
        }

        ApiResponse resp;
        String method = "GET";
        String apiPath = escapePathSegment(account.getInstanceId()) + "/v2/documents/"
                        + type.toString().toLowerCase() + "/"
                        + documentId + "/"
                        + language;
                
        try {
            resp = invokeApi(method,apiPath,null,null,false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request GET " + apiPath;
            throw new ServiceException(errMsg, e);
        }
        
        if (resp.status >= 300) {
            String bodyStr = resp.body != null ? new String(resp.body, StandardCharsets.UTF_8) : null;
            throw new ServiceException("Received HTTP status: " + resp.status + " from " + method
                    + " " + apiPath + ", body: " + bodyStr);
        }
        return resp.body;
    }

    //
    // User API
    //

    private static class GetUsersResponse extends ServiceResponse {
        Map<String, RestUser> users;
    }

    @Override
    public Map<String, UserData> getUsers() throws ServiceException {
        GetUsersResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/users",
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
        UserResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/users/new",
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

        UserResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/users/"
                    + escapePathSegment(userId),
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
            .append(escapePathSegment(account.getInstanceId()))
            .append("/v2/users/")
            .append(escapePathSegment(userId));
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

        UserResponse resp = invokeApiJson(
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

        ServiceResponse resp = invokeApiJson(
                "DELETE",
                escapePathSegment(account.getInstanceId()) + "/v2/users/"
                    + escapePathSegment(userId),
                null,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }


    //
    // Config API
    //

    private static class MTBindingsResponse extends ServiceResponse {
        Map<String, RestMTServiceBinding> mtServiceBindings;
    }

    @Override
    public Map<String, MTServiceBindingData> getAllMTServiceBindings() throws ServiceException {
        MTBindingsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/config/mt",
                null,
                MTBindingsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Map<String, MTServiceBindingData> resultBindings = new TreeMap<>();
        for (Entry<String, RestMTServiceBinding> entry : resp.mtServiceBindings.entrySet()) {
            resultBindings.put(entry.getKey(), new MTServiceBindingDataImpl(entry.getValue()));
        }
        return resultBindings;
    }

    private static class AvailableMTLanguagesResponse extends ServiceResponse {
        Map<String, Map<String, Set<String>>> availableLanguages;
    }

    @Override
    public Map<String, Map<String, Set<String>>> getAvailableMTLanguages()
            throws ServiceException {
        AvailableMTLanguagesResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/config/mt",
                null,
                AvailableMTLanguagesResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return resp.availableLanguages;
    }

    private static class GetMTServiceBindingResponse extends ServiceResponse {
        RestMTServiceBinding mtServiceBinding;
    }

    @Override
    public MTServiceBindingData getMTServiceBinding(String mtServiceInstanceId)
            throws ServiceException {
        GetMTServiceBindingResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/config/mt/"
                    + escapePathSegment(mtServiceInstanceId),
                null,
                GetMTServiceBindingResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new MTServiceBindingDataImpl(resp.mtServiceBinding);
    }

    private static class TranslationConfigsResponse extends ServiceResponse {
        Map<String, Map<String, NewTranslationConfigData>> translationConfigs;
    }

    @Override
    public Map<String, Map<String, NewTranslationConfigData>> getAllTranslationConfigs()
            throws ServiceException {
        TranslationConfigsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/config/trans",
                null,
                TranslationConfigsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return resp.translationConfigs;
    }

    private static class ConfiguredMTLanguagesResponse extends ServiceResponse {
        Map<String, Set<String>> mtLanguages;
    }

    @Override
    public Map<String, Set<String>> getConfiguredMTLanguages()
            throws ServiceException {
        ConfiguredMTLanguagesResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/config/trans",
                null,
                ConfiguredMTLanguagesResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return resp.mtLanguages;
    }

    @Override
    public void putTranslationConfig(String sourceLanguage, String targetLanguage,
            NewTranslationConfigData configData) throws ServiceException {
        if (configData == null) {
            throw new IllegalArgumentException("configData must be specified");
        }

        Gson gson = createGson(NewTranslationConfigData.class.getName());
        String jsonBody = gson.toJson(configData, NewTranslationConfigData.class);

        ServiceResponse resp = invokeApiJson(
                "PUT",
                escapePathSegment(account.getInstanceId()) + "/v2/config/trans/"
                    + sourceLanguage + "/" + targetLanguage,
                jsonBody,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }

    private static class TranslationConfigResponse extends ServiceResponse {
        RestTranslationConfigData config;
    }

    @Override
    public TranslationConfigData getTranslationConfig(String sourceLanguage,
            String targetLanguage) throws ServiceException {
        TranslationConfigResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/config/trans/"
                    + sourceLanguage + "/" + targetLanguage,
                null,
                TranslationConfigResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new TranslationConfigDataImpl(resp.config);
    }


    @Override
    public void deleteTranslationConfig(String sourceLanguage,
            String targetLanguage) throws ServiceException {
        ServiceResponse resp = invokeApiJson(
                "DELETE",
                escapePathSegment(account.getInstanceId()) + "/v2/config/trans/"
                    + sourceLanguage + "/" + targetLanguage,
                null,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }


    //
    // Translation Request APIs
    //

    private static class GetTranslationRequestsResponse extends ServiceResponse {
        Map<String, RestTranslationRequest> translationRequests;
    }

    @Override
    public Map<String, TranslationRequestData> getTranslationRequests()
            throws ServiceException {
        GetTranslationRequestsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/trs",
                null,
                GetTranslationRequestsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Map<String, TranslationRequestData> resultTRs = new TreeMap<>();
        for (Entry<String, RestTranslationRequest> trEntry : resp.translationRequests.entrySet()) {
            String id = trEntry.getKey();
            RestTranslationRequest tr = trEntry.getValue();
            resultTRs.put(id, new TranslationRequestDataImpl(id, tr));
        }

        return resultTRs;
    }


    private static class TranslationRequestResponse extends ServiceResponse {
        String id;
        RestTranslationRequest translationRequest;
    }

    @Override
    public TranslationRequestData getTranslationRequest(
            String trId) throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }

        TranslationRequestResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/trs/"
                    + escapePathSegment(trId),
                null,
                TranslationRequestResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new TranslationRequestDataImpl(resp.id, resp.translationRequest);
    }

    @Override
    public TranslationRequestData createTranslationRequest(
            NewTranslationRequestData newTranslationRequestData)
            throws ServiceException {
        if (newTranslationRequestData == null) {
            throw new IllegalArgumentException("Non-empty newTranslationRequestData must be specified.");
        }

        RestInputTranslationRequestData newRestTRData = new RestInputTranslationRequestData(newTranslationRequestData);
        Gson gson = createGson(RestInputTranslationRequestData.class.getName());
        String jsonBody = gson.toJson(newRestTRData, RestInputTranslationRequestData.class);
        TranslationRequestResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/trs/new",
                jsonBody,
                TranslationRequestResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new TranslationRequestDataImpl(resp.id, resp.translationRequest);
    }


    @Override
    public TranslationRequestData updateTranslationRequest(
            String trId,
            TranslationRequestDataChangeSet changeSet) throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (changeSet == null) {
            throw new IllegalArgumentException("Non-null changeSet must be specified.");
        }

        RestInputTranslationRequestData restChangeSet = new RestInputTranslationRequestData(changeSet);
        Gson gson = createGson(RestInputTranslationRequestData.class.getName());
        String jsonBody = gson.toJson(restChangeSet, RestInputTranslationRequestData.class);
        TranslationRequestResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/trs/"
                        + escapePathSegment(trId),
                jsonBody,
                TranslationRequestResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new TranslationRequestDataImpl(resp.id, resp.translationRequest);
    }


    @Override
    public void deleteTranslationRequest(String trId)
            throws ServiceException {
        ServiceResponse resp = invokeApiJson(
                "DELETE",
                escapePathSegment(account.getInstanceId()) + "/v2/trs/"
                    + trId,
                null,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
    }


    @Override
    public BundleData getTRBundleInfo(String trId,
            String bundleId) throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty bundleId must be specified.");
        }

        GetBundleInfoResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/trs/"
                    + escapePathSegment(trId) + "/" + escapePathSegment(bundleId),
                null,
                GetBundleInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new BundleDataImpl(resp.bundle);
    }

    @Override
    public Map<String, ResourceEntryData> getTRResourceEntries(
            String trId, String bundleId, String language)
            throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Non-empty languageId must be specified.");
        }

        GetResourceEntriesResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/trs/"
                    + escapePathSegment(trId) + "/"
                    + escapePathSegment(bundleId) + "/" + language,
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


    @Override
    public ResourceEntryData getTRResourceEntry(String trId,
            String bundleId, String language, String resKey)
            throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (bundleId == null || bundleId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty bundleId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Non-empty language must be specified.");
        }
        if (resKey == null || resKey.isEmpty()) {
            throw new IllegalArgumentException("Non-empty resKey must be specified.");
        }

        GetResourceEntryResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/bundles/"
                    + escapePathSegment(bundleId) + "/" + language
                    + "/" + escapePathSegment(resKey),
                null,
                GetResourceEntryResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new ResourceEntryDataImpl(resp.resourceEntry);
    }


    //
    // XLIFF APIs
    //

    @Override
    public void getXliffFromBundles(String srcLanguage,
            String trgLanguage, Set<String> bundleIds,
            OutputStream outputXliff) throws ServiceException, IOException {
        if (srcLanguage == null || srcLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty srcLanguage must be specified.");
        }
        if (trgLanguage == null || trgLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trgLanguage must be specified.");
        }

        StringBuilder urlBuf = new StringBuilder();
        urlBuf
            .append(escapePathSegment(account.getInstanceId()))
            .append("/v2/xliff/bundles/")
            .append(srcLanguage)
            .append("/")
            .append(trgLanguage);

        if (bundleIds != null && !bundleIds.isEmpty()) {
            urlBuf.append("?bundles=");
            boolean first = true;
            for (String bundleId : bundleIds) {
                if (first) {
                    first = false;
                } else {
                    urlBuf.append(",");
                }
                urlBuf.append(bundleId);
            }
        }

        ApiResponse resp = null;
        try {
            resp = invokeApi("GET", urlBuf.toString(), null, null, false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request GET " + urlBuf;
            throw new ServiceException(errMsg, e);
        }
        assert resp != null;

        if (resp.contentType == null || !resp.contentType.equalsIgnoreCase("application/xliff+xml")) {
            throw new ServiceException("Received HTTP status: " + resp.status
                    + " with non-XLIFF response (" + resp.contentType + ") from GET"
                    + " " + urlBuf.toString());
        }
        assert resp.body != null;
        outputXliff.write(resp.body);
    }


    @Override
    public void updateBundlesWithXliff(InputStream inputXliff)
            throws ServiceException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[2048];
        int bytes;
        while ((bytes = inputXliff.read(buf)) != -1) {
            baos.write(buf, 0, bytes);
        }
        byte[] inputXliffBytes = baos.toByteArray();

        String method = "POST";
        String apiPath = escapePathSegment(account.getInstanceId())
                + "/v2/xliff/bundles";

        ApiResponse resp = null;
        try {
            resp = invokeApi(method, apiPath, "application/xliff+xml", inputXliffBytes, false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request " + method + " " + apiPath;
            throw new ServiceException(errMsg, e);
        }
        if (resp.status >= 300) {
            String bodyStr = resp.body != null ? new String(resp.body, StandardCharsets.UTF_8) : null;
            throw new ServiceException("Received HTTP status: " + resp.status + " from " + method
                    + " " + apiPath + ", body: " + bodyStr);
        }
    }


    @Override
    public void getXliffFromTranslationRequest(String trId,
            String srcLanguage, String trgLanguage,
            OutputStream outputXliff) throws ServiceException, IOException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (srcLanguage == null || srcLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty srcLanguage must be specified.");
        }
        if (trgLanguage == null || trgLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trgLanguage must be specified.");
        }

        StringBuilder urlBuf = new StringBuilder();
        urlBuf
            .append(escapePathSegment(account.getInstanceId()))
            .append("/v2/xliff/trs/")
            .append(trId)
            .append("/")
            .append(srcLanguage)
            .append("/")
            .append(trgLanguage);

        ApiResponse resp = null;
        try {
            resp = invokeApi("GET", urlBuf.toString(), null, null, false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request GET " + urlBuf;
            throw new ServiceException(errMsg, e);
        }
        assert resp != null;

        if (resp.contentType == null || !resp.contentType.equalsIgnoreCase("application/xliff+xml")) {
            throw new ServiceException("Received HTTP status: " + resp.status
                    + " with non-XLIFF response (" + resp.contentType + ") from GET"
                    + " " + urlBuf.toString());
        }
        assert resp.body != null;
        outputXliff.write(resp.body);
    }


    //
    // Private method used for calling REST endpoints
    //

    private <T> T invokeApiJson(String method, String apiPath, String inJson, Class<T> classOfT)
        throws ServiceException {
        return invokeApiJson(method, apiPath, inJson, classOfT, false);
    }

    private <T> T invokeApiJson(String method, String apiPath, String inJson, Class<T> classOfT,
            boolean anonymous) throws ServiceException {

        T responseObj = null;
        try {
            // Request body in UTF-8
            String contentType = null;
            byte[] requestBody = null;
            if (inJson != null) {
                requestBody = inJson.getBytes(StandardCharsets.UTF_8);
                contentType = "application/json";
            }

            ApiResponse resp = invokeApi(method, apiPath, contentType, requestBody, anonymous);

            if (resp.contentType == null || !resp.contentType.equalsIgnoreCase("application/json")) {
                throw new ServiceException("Received HTTP status: " + resp.status
                        + " with non-JSON response from " + method + " " + apiPath);
            }

            Reader reader = new InputStreamReader(new ByteArrayInputStream(resp.body), StandardCharsets.UTF_8);
            Gson gson = createGson(classOfT.getName());
            responseObj = gson.fromJson(reader, classOfT);
            

        } catch (Exception e) {
            // Error handling
            String errMsg = "Error while processing API request " + method + " " + apiPath;
            throw new ServiceException(errMsg, e);
        }

        return responseObj;
    }
    
    private <T> T invokeApiInputStream(String method, String apiPath, String contentType, FileInputStream fis, Class<T> classOfT,
            boolean anonymous) throws ServiceException {

        byte[] requestBody = null;
        T responseObj = null;
        try {
            requestBody = ByteStreams.toByteArray((InputStream) fis);
            ApiResponse resp = invokeApi(method, apiPath, contentType, requestBody, anonymous);

            Reader reader = new InputStreamReader(new ByteArrayInputStream(resp.body), StandardCharsets.UTF_8);
            Gson gson = createGson(classOfT.getName());
            responseObj = gson.fromJson(reader, classOfT);

        } catch (Exception e) {
            // Error handling
            String errMsg = "Error while processing API request " + method + " " + apiPath;
            throw new ServiceException(errMsg, e);
        }

        return responseObj;
    }

    private static class ApiResponse {
        int status;
        String contentType;
        byte[] body;
    }

    private ApiResponse invokeApi(String method, String apiPath, String inContentType, byte[] inBody,
            boolean anonymous) throws IOException {
        String urlStr = account.getUrl() + "/" + apiPath;
        URL targetUrl = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)targetUrl.openConnection();
        conn.setRequestMethod(method);

        // Date header
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateHeader = sdf.format(new Date());

        conn.setRequestProperty("Date", dateHeader);

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
                                method, urlStr, dateHeader, inBody));
                break;
            }

            conn.setRequestProperty("Authorization", authHeader.toString());
        }

        // write the request body
        if (inBody != null) {
            conn.setRequestProperty("Content-Type", inContentType);
            conn.setDoOutput(true);
            conn.getOutputStream().write(inBody);
        }

        // receiving response
        ApiResponse resp = new ApiResponse();

        resp.status = conn.getResponseCode();
        resp.contentType = conn.getContentType();

        // response body
        int bodyLen = conn.getContentLength();
        if (bodyLen < 0) {
            bodyLen = 2048; // default length for initial byte array
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bodyLen);
        InputStream is = conn.getErrorStream();
        if (is == null) {
            is = conn.getInputStream();
        }

        byte[] buf = new byte[2048];
        int bytes;
        while ((bytes = is.read(buf)) != -1) {
            baos.write(buf, 0, bytes);
        }

        resp.body = baos.toByteArray();

        return resp;
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
            return BaseEncoding.base64().encode(token.getBytes("ISO-8859-1"));
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
            credential.append(BaseEncoding.base64().encode(hmac));
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
    // Custom JSON serialization/deserialization supporting Java Enum
    //

    // Custom type adapter for serializing/deserializing Java enum with mapping.
    // This method assumes all enum constants are in upper case.
    static class EnumWithFallbackAdapter<T extends Enum<T>> extends TypeAdapter<T> {
        private final T fallback;

        public EnumWithFallbackAdapter(T fallback) {
            this.fallback = fallback;
        }

        T toEnumWithFallback(String s) {
            if (s == null) {
                return null;
            }

            T value = null;
            try {
                value = (T) Enum.valueOf(fallback.getDeclaringClass(), s.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                // fallback
                value = fallback;
            }
            return value;
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return toEnumWithFallback(in.nextString());
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.name());
        }
    }

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

    // TypeAdapterFactory used for serializing map entries with null value.
    // updateResourceStrings and some other REST APIs handles properties
    // with null value as deletion directive.
    static class NullMapValueTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            // The special handling is only applicable to Map objects.
            Class<?> rawType = typeToken.getRawType();
            if (!Map.class.isAssignableFrom(rawType)) {
                return null;
            }

            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    boolean serializeNulls = out.getSerializeNulls();
                    if (!serializeNulls) {
                        // force null serialization
                        out.setSerializeNulls(true);
                    }
                    delegate.write(out, value);
                    if (!serializeNulls) {
                        // reset
                        out.setSerializeNulls(false);
                    }
                }
                public T read(JsonReader in) throws IOException {
                    return delegate.read(in);
                }
            };
        }
    }
    /**
     * Creates a new Gson object
     * 
     * @param className A class name used for serialization/deserialization.
     *                  <p>Note: This implementation does not use this argument
     *                  for now. If we need different kinds of type adapters
     *                  depending on class, the implementation might be updated
     *                  to set up appropriate set of type adapters.
     * @return  A Gson object
     */
    private static Gson createGson(String className) {
        GsonBuilder builder = new GsonBuilder();

        // ISO8601 date format support
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        builder.registerTypeAdapter(TranslationStatus.class,
                new EnumWithFallbackAdapter<TranslationStatus>(TranslationStatus.UNKNOWN));

        builder.registerTypeAdapter(TranslationRequestStatus.class,
                new EnumWithFallbackAdapter<TranslationRequestStatus>(TranslationRequestStatus.UNKNOWN));

        builder.registerTypeAdapter(
                new TypeToken<EnumMap<TranslationStatus, Integer>>() {}.getType(),
                new EnumMapInstanceCreator<TranslationStatus, Integer>(TranslationStatus.class));

        builder.registerTypeAdapterFactory(new NullMapValueTypeAdapterFactory());

       // builder.registerTypeAdapter(DocumentData.class, new DocumentDataInstanceCreator());
        
        return builder.create();
    }

    private static String escapePathSegment(String pathSegment) {
        return UrlEscapers.urlPathSegmentEscaper().escape(pathSegment);
    }

    //Document Translation Request APIs
    
    private static class GetDocumentTranslationRequestsResponse extends ServiceResponse {
        Map<String, RestDocumentTranslationRequest> translationRequests;
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getDocumentTranslationRequests()
     */
    @Override
    public Map<String, DocumentTranslationRequestData> getDocumentTranslationRequests()
            throws ServiceException {

        GetDocumentTranslationRequestsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs",
                null,
                GetDocumentTranslationRequestsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Map<String, DocumentTranslationRequestData> resultTRs = new TreeMap<>();
        for (Entry<String, RestDocumentTranslationRequest> trEntry : resp.translationRequests.entrySet()) {
            String id = trEntry.getKey();
            RestDocumentTranslationRequest tr = trEntry.getValue();
            resultTRs.put(id, new DocumentTranslationRequestDataImpl(id, tr));
        }

        return resultTRs;
    
    }
    
    private static class DocumentTranslationRequestResponse extends ServiceResponse {
        String id;
        RestDocumentTranslationRequest translationRequest;
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getDocumentTranslationRequest(java.lang.String)
     */
    @Override
    public DocumentTranslationRequestData getDocumentTranslationRequest(
            String trId) throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }

        DocumentTranslationRequestResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/"
                    + escapePathSegment(trId),
                null,
                DocumentTranslationRequestResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new DocumentTranslationRequestDataImpl(resp.id, resp.translationRequest);
    
    }


    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#createDocumentTranslationRequest(com.ibm.g11n.pipeline.client.NewDocumentTranslationRequestData)
     */
    @Override
    public DocumentTranslationRequestData createDocumentTranslationRequest(
            NewDocumentTranslationRequestData newTranslationRequestData)
            throws ServiceException {
        if (newTranslationRequestData == null) {
            throw new IllegalArgumentException("Non-empty newTranslationRequestData must be specified.");
        }

        RestInputDocumentTranslationRequestData newRestTRData = new RestInputDocumentTranslationRequestData(newTranslationRequestData);
        Gson gson = createGson(RestInputDocumentTranslationRequestData.class.getName());
        String jsonBody = gson.toJson(newRestTRData, RestInputDocumentTranslationRequestData.class);
        DocumentTranslationRequestResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/new",
                jsonBody,
                DocumentTranslationRequestResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new DocumentTranslationRequestDataImpl(resp.id, resp.translationRequest);
    
    }


    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#updateDocumentTranslationRequest(java.lang.String, com.ibm.g11n.pipeline.client.DocumentTranslationRequestDataChangeSet)
     */
    @Override
    public DocumentTranslationRequestData updateDocumentTranslationRequest(
            String trId, DocumentTranslationRequestDataChangeSet changeSet)
            throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (changeSet == null) {
            throw new IllegalArgumentException("Non-null changeSet must be specified.");
        }

        RestInputDocumentTranslationRequestData restChangeSet = new RestInputDocumentTranslationRequestData(changeSet);
        Gson gson = createGson(RestInputDocumentTranslationRequestData.class.getName());
        String jsonBody = gson.toJson(restChangeSet, RestInputDocumentTranslationRequestData.class);
        DocumentTranslationRequestResponse resp = invokeApiJson(
                "POST",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/"
                        + escapePathSegment(trId),
                jsonBody,
                DocumentTranslationRequestResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new DocumentTranslationRequestDataImpl(resp.id, resp.translationRequest);
    }


    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#deleteDocumentTranslationRequest(java.lang.String)
     */
    @Override
    public void deleteDocumentTranslationRequest(String trId)
            throws ServiceException {
        ServiceResponse resp = invokeApiJson(
                "DELETE",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/"
                    + trId,
                null,
                ServiceResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }
        
    }


    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getTRDocumentInfo(java.lang.String, java.lang.String, com.ibm.g11n.pipeline.client.DocumentType)
     */
    @Override
    public DocumentData getTRDocumentInfo(String trId, String documentId,
            DocumentType type) throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty documentId must be specified.");
        }

        GetDocumentInfoResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/"
                    + escapePathSegment(trId) + "/" + type.toString().toLowerCase() + "/" + escapePathSegment(documentId),
                null,
                GetDocumentInfoResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new DocumentDataImpl(resp.documentData);
    }

    private static class GetSegmentsResponse extends ServiceResponse {
        Map<String, RestSegmentData> segments;
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getTRSegments(java.lang.String, java.lang.String, com.ibm.g11n.pipeline.client.DocumentType, java.lang.String)
     */
    @Override
    public Map<String, SegmentData> getTRSegments(String trId,
            String documentId, DocumentType type, String language)
            throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty documentId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Non-empty languageId must be specified.");
        }

        GetSegmentsResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/"
                    + escapePathSegment(trId) + "/" + type.toString().toLowerCase() + "/"
                    + escapePathSegment(documentId) + "/" + language,
                null,
                GetSegmentsResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        Map<String, SegmentData> resultEntries = new TreeMap<String, SegmentData>();
        if (resp.segments != null && !resp.segments.isEmpty()) {
            for (Entry<String, RestSegmentData> entry : resp.segments.entrySet()) {
                resultEntries.put(entry.getKey(),
                        new SegmentDataImpl(entry.getValue()));
            }
        }
        return resultEntries;
    }
    
    private static class GetSegmentResponse extends ServiceResponse {
        RestSegmentData segmentData;
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getTRSegment(java.lang.String, java.lang.String, com.ibm.g11n.pipeline.client.DocumentType, java.lang.String, java.lang.String)
     */
    @Override
    public SegmentData getTRSegment(String trId, String documentId,
            DocumentType type, String language, String segmentKey)
            throws ServiceException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty documentId must be specified.");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("Non-empty language must be specified.");
        }
        if (segmentKey == null || segmentKey.isEmpty()) {
            throw new IllegalArgumentException("Non-empty segmentKey must be specified.");
        }

        GetSegmentResponse resp = invokeApiJson(
                "GET",
                escapePathSegment(account.getInstanceId()) + "/v2/doc-trs/" + escapePathSegment(trId) + 
                    "/" + type.toString().toLowerCase() + "/"
                    + escapePathSegment(documentId) + "/" + language
                    + "/" + escapePathSegment(segmentKey),
                null,
                GetSegmentResponse.class);

        if (resp.getStatus() == Status.ERROR) {
            throw new ServiceException(resp.getMessage());
        }

        return new SegmentDataImpl(resp.segmentData);
    
    }
    
    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getXliffFromDocuments(java.lang.String, java.lang.String, java.util.Map, java.io.OutputStream)
     */
    @Override
    public void getXliffFromDocuments(String srcLanguage, String trgLanguage,
            Map<DocumentType, Set<String>> documentsMap,
            OutputStream outputXliff) throws ServiceException, IOException {

        if (srcLanguage == null || srcLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty srcLanguage must be specified.");
        }
        if (trgLanguage == null || trgLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trgLanguage must be specified.");
        }

        StringBuilder urlBuf = new StringBuilder();
        urlBuf
            .append(escapePathSegment(account.getInstanceId()))
            .append("/v2/doc-xliff/")
            .append(srcLanguage)
            .append("/")
            .append(trgLanguage);

        if (documentsMap != null && !documentsMap.isEmpty()) {
            urlBuf.append("?");
            int count = documentsMap.size();
            for (Map.Entry<DocumentType, Set<String>> entry : documentsMap.entrySet()) {
                DocumentType type = entry.getKey();
                Set<String> documentIds = entry.getValue();
                urlBuf.append(type.toString().toLowerCase()).append("=");
                boolean first = true;
                for (String docId : documentIds) {
                    if (first) {
                        first = false;
                    } else {
                        urlBuf.append(",");
                    }
                    urlBuf.append(docId);
                }
                count--;
            }
            if (count != 0) {
                urlBuf.append("&");
            }
        }

        ApiResponse resp = null;
        try {
            resp = invokeApi("GET", urlBuf.toString(), null, null, false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request GET " + urlBuf;
            throw new ServiceException(errMsg, e);
        }
        assert resp != null;

        if (resp.contentType == null || !resp.contentType.equalsIgnoreCase("application/xliff+xml")) {
            throw new ServiceException("Received HTTP status: " + resp.status
                    + " with non-XLIFF response (" + resp.contentType + ") from GET"
                    + " " + urlBuf.toString());
        }
        assert resp.body != null;
        outputXliff.write(resp.body);
    }
    

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#updateDocumentsWithXliff(java.io.InputStream)
     */
    @Override
    public void updateDocumentsWithXliff(InputStream inputXliff)
            throws ServiceException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[2048];
        int bytes;
        while ((bytes = inputXliff.read(buf)) != -1) {
            baos.write(buf, 0, bytes);
        }
        byte[] inputXliffBytes = baos.toByteArray();

        String method = "POST";
        String apiPath = escapePathSegment(account.getInstanceId())
                + "/v2/doc-xliff";

        ApiResponse resp = null;
        try {
            resp = invokeApi(method, apiPath, "application/xliff+xml", inputXliffBytes, false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request " + method + " " + apiPath;
            throw new ServiceException(errMsg, e);
        }
        if (resp.status >= 300) {
            String bodyStr = resp.body != null ? new String(resp.body, StandardCharsets.UTF_8) : null;
            throw new ServiceException("Received HTTP status: " + resp.status + " from " + method
                    + " " + apiPath + ", body: " + bodyStr);
        }
    }


    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.client.ServiceClient#getXliffFromDocumentTranslationRequest(java.lang.String, java.lang.String, java.lang.String, java.io.OutputStream)
     */
    @Override
    public void getXliffFromDocumentTranslationRequest(String trId,
            String srcLanguage, String trgLanguage, OutputStream outputXliff)
            throws ServiceException, IOException {
        if (trId == null || trId.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trId must be specified.");
        }
        if (srcLanguage == null || srcLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty srcLanguage must be specified.");
        }
        if (trgLanguage == null || trgLanguage.isEmpty()) {
            throw new IllegalArgumentException("Non-empty trgLanguage must be specified.");
        }

        StringBuilder urlBuf = new StringBuilder();
        urlBuf
            .append(escapePathSegment(account.getInstanceId()))
            .append("/v2/doc-xliff/trs/")
            .append(trId)
            .append("/")
            .append(srcLanguage)
            .append("/")
            .append(trgLanguage);

        ApiResponse resp = null;
        try {
            resp = invokeApi("GET", urlBuf.toString(), null, null, false);
        } catch (Exception e) {
            String errMsg = "Error while processing API request GET " + urlBuf;
            throw new ServiceException(errMsg, e);
        }
        assert resp != null;

        if (resp.contentType == null || !resp.contentType.equalsIgnoreCase("application/xliff+xml")) {
            throw new ServiceException("Received HTTP status: " + resp.status
                    + " with non-XLIFF response (" + resp.contentType + ") from GET"
                    + " " + urlBuf.toString());
        }
        assert resp.body != null;
        outputXliff.write(resp.body);
    }
}
