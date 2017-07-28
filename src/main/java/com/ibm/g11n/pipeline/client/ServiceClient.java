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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ibm.g11n.pipeline.client.impl.ServiceClientImpl;

/**
 * <code>ServiceClient</code> provides public REST API access to
 * IBM Globalization Pipeline service.
 * 
 * @author Yoshito Umaoka
 */
public abstract class ServiceClient {

    /**
     * Authentication scheme used for accessing Globalization Pipeline's
     * service endpoints.
     */
    public enum AuthScheme {
        /**
         * HMAC authentication. This authentication scheme is used
         * by default.
         */
        HMAC,
        /**
         * HTTP Basic authentication. This authentication scheme can
         * be used only for {@link UserType#READER READER} access.
         */
        BASIC
    };

    protected final ServiceAccount account;
    protected AuthScheme scheme = AuthScheme.HMAC;

    /**
     * Protected constructor for a subclass extending <code>ServiceClient</code>.
     * 
     * @param account   The service account.
     */
    protected ServiceClient(ServiceAccount account) {
        this.account = account;
    }

    /**
     * Returns an instance of ServiceClient for the specified ServiceAccount.
     * 
     * @param account   The service account. Must not be null.
     * @return  An instance of ServiceClient.
     */
    public static ServiceClient getInstance(ServiceAccount account) {
        Objects.requireNonNull(account, "account must not be null");
        return new ServiceClientImpl(account);
    }

    /**
     * Returns an instance of ServiceClient.
     * <p>
     * This factory method only works if necessary account information is
     * provided via environment variables or VCAP_SERVICES on Bluemix.
     * 
     * @return An instance of ServiceClient, or null if sufficient configuration
     * is not provided in the runtime environment.
     */
    public static ServiceClient getInstance() {
        ServiceAccount account = ServiceAccount.getInstance();
        if (account == null) {
            return null;
        }
        return getInstance(account);
    }

    /**
     * Returns the service account used by this service client.
     * 
     * @return The service account used by this service client.
     */
    public ServiceAccount getServiceAccount() {
        return account;
    }

    /**
     * Returns the authentication scheme used for accessing IBM Globalization
     * Pipeline service's REST endpoints.
     * <p>
     * By default, {@link AuthScheme#HMAC HMAC} is used.
     * 
     * @return The authentication scheme.
     */
    public AuthScheme getAuthScheme() {
        return scheme;
    }

    /**
     * Sets the authentication scheme.
     * <p>
     * Note: {@link AuthScheme#BASIC BASIC} can be used only by
     * {@link UserType#READER READER} accounts.
     * 
     * @param scheme The authentication scheme.
     */
    public void setAuthScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }


    //
    // $service/v2 APIs
    //

    /**
     * Returns IBM Globalization Pipeline service's information.
     * 
     * @return The service information.
     * @throws ServiceException when the operation failed.
     */
    public abstract ServiceInfo getServiceInfo() throws ServiceException;

    //
    // {serviceInstanceId}/v2/instance APIs
    //

    /**
     * Returns the service instance information.
     * 
     * @return The service instance information.
     * @throws ServiceException when the operation failed.
     */
    public abstract ServiceInstanceInfo getServiceInstanceInfo() throws ServiceException;

    //
    // {serviceInstanceId}/v2/bundles APIs
    //

    /**
     * Returns a set of bundle IDs available in the service instance.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * 
     * @return A set of bundle IDs.
     * @throws ServiceException when the operation failed.
     */
    public abstract Set<String> getBundleIds() throws ServiceException;

    /**
     * Creates a new translation bundle.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * 
     * @param bundleId
     *          The new bundle's ID. Mut not be null or empty.
     *          <br>The bundle ID must match a regular expression pattern
     *          [a-zA-Z0-9][a-zA-Z0-9_.-]* and the length must be less than or equal
     *          to 255.
     * @param newBundleData
     *          The new bundle's configuration.
     * @throws ServiceException when the operation failed.
     */
    public abstract void createBundle(String bundleId,
            NewBundleData newBundleData) throws ServiceException;

    /**
     * Returns the bundle's configuration.
     * <p>
     * This operation is only allowed to all user types, but only
     * basic information (source language and target languages) is included
     * in the result if the requesting user type is {@link UserType#READER READER}.
     * of the service instance.
     * 
     * @param bundleId  The bundle ID. Must not be null or empty.
     * @return          The bundle's configuration.
     * @throws ServiceException when the operation failed.
     */
    public abstract BundleData getBundleInfo(String bundleId) throws ServiceException;

    /**
     * Returns the bundle's metrics information.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * and {@link UserType#TRANSLATOR TRANSLATOR} of the service instance.
     * 
     * @param bundleId  The bundle ID. Mut not be null or empty.
     * @return          The bundle's metrics information.
     * @throws ServiceException when the operation failed.
     * @see LanguageMetrics
     */
    public abstract BundleMetrics getBundleMetrics(String bundleId) throws ServiceException;

    /**
     * Updates the bundle's configuration.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * 
     * @param bundleId  The bundle ID.
     * @param changeSet The change set of bundle configuration.
     * @throws ServiceException when the operation failed.
     */
    public abstract void updateBundle(String bundleId,
            BundleDataChangeSet changeSet) throws ServiceException;

    /**
     * Deletes the bundle.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * 
     * @param bundleId  The bundle ID.
     * @throws ServiceException when the operation failed.
     */
    public abstract void deleteBundle(String bundleId) throws ServiceException;

    /**
     * Returns a map containing resource string key-value pairs in the specified
     * bundle and language.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param fallback  If the value in the source language is included when
     *                  translated value is not available.
     * @return          A map containing resource string key-value pairs.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, String> getResourceStrings(String bundleId,
            String language, boolean fallback) throws ServiceException;

    /**
     * Returns a map containing resource string entries indexed by resource key
     * in the bundle and the language.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @return          A map containing resource string entries indexed by resource key.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, ResourceEntryData> getResourceEntries(String bundleId,
            String language) throws ServiceException;


    /**
     * Returns per language metrics information
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * and {@link UserType#TRANSLATOR TRANSLATOR} of the service instance.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @return          The language metrics information.
     * @throws ServiceException when the operation failed.
     */
    public abstract LanguageMetrics getLanguageMetrics(String bundleId,
            String language) throws ServiceException;

    // TODO - review key/value restrictions
    /**
     * Uploads resource string key-value pairs.
     * <p>
     * Following restrictions are applied.
     * <ul>
     *  <li>Resource key must not be empty.</li>
     *  <li>Length of resource key must be less than or equal to 255.</li>
     *  <li>Length of resource value must be less than or equal to 8191.</li>
     *  <li>Total number of key-value pairs after upload must be less than or equal to 500.</li>
     * </ul>
     * <p>
     * When the specified language is the source language of the bundle:
     * <ul>
     *  <li>A value for a new key will be sent to a machine translation and the translated
     *  value will be automatically added to the bundle's target languages.</li>
     *  <li>An existing key with a new value will be also sent to a machine translation
     *  and the translation will be automatically updated.</li>
     *  <li>An existing key with a same value won't affect translation target languages.</li>
     *  <li>A key not included in the input map will be deleted from the bundle.</li>
     * </ul>
     * <p>
     * When the specified language is not the source language of the bundle:
     * <ul>
     *  <li>If the language is currently not available in the bundle, the language
     *  will be added to the bundle's configuration.</li>
     *  <li>A new key which does not exist in the source language will be ignored.</li>
     *  <li>A key not included in the input map, but available in the source language will
     *  be automatically inserted to the specified language with a machine translated value.</li>
     * </ul>
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param strings   The resource string key-value pairs to be uploaded.
     * @throws ServiceException when the operation failed.
     * @see #uploadResourceEntries(String, String, Map)
     */
    public abstract void uploadResourceStrings(String bundleId, String language,
            Map<String, String> strings) throws ServiceException;

    /**
     * Upload resource entries.
     * <p>
     * This method is similar to {@link #uploadResourceStrings(String, String, Map)},
     * but is able to other resource entry data along with resource string value.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param newResourceEntries    The map containing {@link NewResourceEntryData}
     *                  indexed by resource key to be uploaded.
     * @throws ServiceException when the operation failed.
     * @see #uploadResourceStrings(String, String, Map)
     */
    public abstract void uploadResourceEntries(String bundleId, String language,
            Map<String, NewResourceEntryData> newResourceEntries) throws ServiceException;

    /**
     * Update resource string key-value pairs.
     * <p>
     * The same restrictions explained in {@link #uploadResourceStrings(String, String, Map)} are applied.
     * <p>
     * When the specified language is the source language of the bundle:
     * <ul>
     *  <li>A value for a new key will be sent to a machine translation and the translated
     *  value will be automatically added to the bundle's target languages.</li>
     *  <li>An existing key with a new value will be also sent to a machine translation
     *  and the translation will be automatically updated.</li>
     *  <li>An existing key with a same value won't affect translation target languages.</li>
     *  <li>A key not included in the input map will be ignored. This behavior is different
     *  from {@link #uploadResourceStrings(String, String, Map)}. </li>
     * </ul>
     * <p>
     * When the specified language is not the source language of the bundle:
     * <ul>
     *  <li>If the language is currently not available in the bundle, A {@link ServiceException}
     *  will be thrown. This is different from {@link #uploadResourceStrings(String, String, Map)}.</li>
     *  <li>A new key which does not exist in the source language will be ignored.</li>
     *  <li>If the argument <code>resync</code> is <code>true</code>, all key-value pairs in
     *  the language will be compared with the source language and update out of sync
     *  key-value pairs. This is useful when previous translation was failed by a service
     *  problem and you want to fix the problem.</li>
     * </ul>
     * <p>
     * Updating resource strings in the bundle's source language is only allowed to
     * {@link UserType#ADMINISTRATOR ADMINISTRATOR} of the service instance.
     * Updating resource strings in a bundle's translation target language is
     * allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR}.
     * 
     * @param bundleId  The bundle ID
     * @param language  The language specified by BCP 47 language tag.
     * @param strings   The resource string key-value pairs to be uploaded.
     * @param resync    <code>true</code> to force the service to synchronize
     *                  resource string key-value pairs with the bundle's source
     *                  language. No effect if the specified language is the source
     *                  language of the bundle.
     * @throws ServiceException when the operation failed.
     * @see #updateResourceEntries(String, String, Map, boolean)
     */
    public abstract void updateResourceStrings(String bundleId, String language,
            Map<String, String> strings, boolean resync)
                    throws ServiceException;

    /**
     * Updates resource entries.
     * <p>
     * This method is similar to {@link #updateResourceStrings(String, String, Map, boolean)},
     * but is able to update other resource entry data.
     * <p>
     * Updating resource entries in the bundle's source language is only allowed to
     * {@link UserType#ADMINISTRATOR ADMINISTRATOR} of the service instance.
     * Updating resource entries in a bundle's translation target language is
     * allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR}.

     * @param bundleId  The bundle ID
     * @param language  The language specified by BCP 47 language tag.
     * @param resourceEntries    The map containing {@link ResourceEntryDataChangeSet}
     *                  indexed by resource key to be updated.
     * @param resync    <code>true</code> to force the service to synchronize
     *                  resource string key-value pairs with the bundle's source
     *                  language. No effect if the specified language is the source
     *                  language of the bundle.
     * @throws ServiceException when the operation failed.
     */
    public abstract void updateResourceEntries(String bundleId, String language,
            Map<String, ResourceEntryDataChangeSet> resourceEntries, boolean resync)
                    throws ServiceException;

    /**
     * Returns the resource entry specified by the bundle ID, the language and the resource key.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR} of the service instance.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param resKey    The resource key.
     * @return          The resource entry data.
     * @throws ServiceException when the operation failed.
     */
    public abstract ResourceEntryData getResourceEntry(String bundleId,
            String language, String resKey) throws ServiceException;

    /**
     * Updates the resource entry.
     * <p>
     * Updating a resource entry in the bundle's source language is only allowed to
     * {@link UserType#ADMINISTRATOR ADMINISTRATOR} of the service instance.
     * Updating a resource entry in a bundle's translation target language is
     * allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR}.
     * 
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param resKey    The resource key.
     * @param changeSet The change set of resource entry.
     * @throws ServiceException when the operation failed.
     */
    public abstract void updateResourceEntry(String bundleId, String language,
            String resKey, ResourceEntryDataChangeSet changeSet)
                    throws ServiceException;

    //
    // {serviceInstanceId}/v2/users APIs
    //

    /**
     * Returns a map containing user data objects in the service instance indexed
     * by user ID.
     * 
     * @return A map containing user data objects.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, UserData> getUsers() throws ServiceException;

    /**
     * Creates a new user.
     * <p>
     * This method returns <code>UserData</code> object with the generated user
     * ID and password. There is no way to access the password generated for the
     * new user later. A caller is responsible to store the password in a secure
     * place. If a password is lost, you can only request for a new password.
     * See {@link #updateUser(String, UserDataChangeSet, boolean)} about password
     * reset.
     * 
     * @param newUserData   The new user's configuration.
     * @return              The user data object created by this operation.
     * @throws ServiceException when the operation failed.
     */
    public abstract UserData createUser(NewUserData newUserData)
            throws ServiceException;

    /**
     * Returns the user data object.
     * <p>
     * Note: The password is not available in the returned user data object.
     * 
     * @param userId        The user ID.
     * @return              The user data object.
     * @throws ServiceException when the operation failed.
     */
    public abstract UserData getUser(String userId) throws ServiceException;

    /**
     * Updates the user data.
     * <p>
     * This operation allows you to reset password. When the argument <code>resetPassword</code>
     * is <code>true</code>, a new password will be set in the returned user data object.
     * Once password is reset, the old password cannot be used. When the argument
     * <code>resetPassword</code> is <code>false</code>, the user's password won't be returned
     * in the user data object.
     * 
     * @param userId        The user ID.
     * @param changeSet     The change set of user data.
     * @param resetPassword <code>true</code> to issue a new password.
     * @return              The user data object.
     * @throws ServiceException when the operation failed.
     */
    public abstract UserData updateUser(String userId,
            UserDataChangeSet changeSet, boolean resetPassword)
                    throws ServiceException;

    /**
     * Deletes the user.
     * 
     * @param userId        The user ID.
     * @throws ServiceException when the operation failed.
     */
    public abstract void deleteUser(String userId) throws ServiceException;


    //
    // {serviceInstanceId}/v2/config APIs
    //

    /**
     * Gets all machine translation service binding data.
     * 
     * @return  A map containing all available machine translation service binding data indexed by
     *          service instance IDs.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, MTServiceBindingData> getAllMTServiceBindings()
            throws ServiceException;

    /**
     * Gets all available machine translation source/target languages and
     * machine translation service instance IDs for each source/target pair.
     * <ul>
     *  <li>The top level keys represent available machine translation source languages.</li>
     *  <li>The second level keys represent available machine translation target languages.</li>
     *  <li>The leaf set contains machine translation service instance IDs available for the source-target
     *  language pair.</li>
     * </ul>
     * <p>
     * The results contains all configurable language pairs, but it does not
     * mean all of these pairs are currently active. See {@link #getConfiguredMTLanguages()}
     * for getting currently active machine translation language pairs.
     * 
     * @return  A map containing all available machine translation source/target languages
     *          and machine translation service instance IDs for each source/target pair.
     * @throws ServiceException when the operation failed.
     * @see #getConfiguredMTLanguages()
     */
    public abstract Map<String, Map<String, Set<String>>> getAvailableMTLanguages()
            throws ServiceException;

    /**
     * Gets the specified machine translation service binding data.
     * 
     * @param mtServiceInstanceId   The machine translation service's instance ID.
     * @return  The specified machine translation service binding data.
     * @throws ServiceException when the operation failed.
     */
    public abstract MTServiceBindingData getMTServiceBinding(String mtServiceInstanceId)
            throws ServiceException;


    /**
     * Gets all translation configuration data.
     * <ul>
     *  <li>The top level keys represent machine translation source languages.</li>
     *  <li>The second level keys represent machine translation target languages.</li>
     *  <li>The leaf value <code>TranslationCofigData</code> contains custom
     *  translation configuration data for the language pair.</li>
     * </ul>
     * <p>
     * Currently, translation configuration data contains a machine translation service
     * to be used for each source/target language pairs, and optional parameters to the
     * machine translation service.
     * 
     * @return  All translation configuration data indexed by translation source/target
     * languages.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, Map<String, NewTranslationConfigData>> getAllTranslationConfigs()
            throws ServiceException;

    /**
     * Gets all active machine translation source/target language pairs.
     * <ul>
     *  <li>The top level keys represent machine translation source languages.</li>
     *  <li>The leaf value is a set of machine translation target languages.</li>
     * </ul>
     * <p>
     * Unlike {@link #getAvailableMTLanguages()}, this method returns currently
     * active machine translation source/target language pairs.
     * 
     * @return  All active machine translation source/target language pairs.
     * @throws ServiceException when the operation failed.
     * @see #getAvailableMTLanguages()
     */
    public abstract Map<String, Set<String>> getConfiguredMTLanguages()
            throws ServiceException;

    /**
     * Puts the <code>NewTranslationConfigData</code> for the specified source/target language pairs.
     * <p>
     * If there is an existing <code>TranslationConfigData</code> for the language pair,
     * this method will overwrite the existing data with new one.
     * 
     * @param sourceLanguage    The translation source language.
     * @param targetLanguage    The translation target language.
     * @param configData        The new translation configuration data.
     * @throws ServiceException when the operation failed.
     */
    public abstract void putTranslationConfig(String sourceLanguage, String targetLanguage,
            NewTranslationConfigData configData) throws ServiceException;

    /**
     * Returns the <code>TranslationConfigData</code> for the specified source/target language pairs.
     * 
     * @param sourceLanguage    The translation source language.
     * @param targetLanguage    The translation target language.
     * @return  The translation configuration data.
     * @throws ServiceException when the operation failed.
     */
    public abstract TranslationConfigData getTranslationConfig(String sourceLanguage,
            String targetLanguage) throws ServiceException;


    /**
     * Deletes the <code>TranslationConfigData</code> for the specified source/target language pairs.
     * 
     * @param sourceLanguage    The translation source language.
     * @param targetLanguage    The translation target language.
     * @throws ServiceException when the operation failed.
     */
    public abstract void deleteTranslationConfig(String sourceLanguage, String targetLanguage)
           throws ServiceException;


    //
    // {serviceInstanceId}/v2/trs APIs
    //

    /**
     * Returns a map containing <code>TranslationRequestData</code> indexed by translation
     * request IDs.
     * 
     * @return A map containing <code>TranslationRequestData</code> indexed by translation.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, TranslationRequestData> getTranslationRequests() throws ServiceException;

    /**
     * Returns the translation request specified by the translation request ID.
     * 
     * @param trId  The translation request ID.
     * @return  The translation request data.
     * @throws ServiceException when the operation failed.
     */
    public abstract TranslationRequestData getTranslationRequest(String trId)
            throws ServiceException;

    /**
     * Creates a new translation request.
     * 
     * @param newTranslationRequestData The new translation request.
     * @return  The translation request created by this operation.
     * @throws ServiceException when the operation failed.
     */
    public abstract TranslationRequestData createTranslationRequest(NewTranslationRequestData newTranslationRequestData)
            throws ServiceException;

    /**
     * Updates the translation request.
     * 
     * @param trId      The translation request ID.
     * @param changeSet The change set of translation request data.
     * @return  The translation request updated by this operation.
     * @throws ServiceException when the operation failed.
     */
    public abstract TranslationRequestData updateTranslationRequest(String trId,
            TranslationRequestDataChangeSet changeSet) throws ServiceException;

    /**
     * Deletes the translation request.
     * 
     * @param trId  The translation request ID.
     * @throws ServiceException when the operation failed.
     */
    public abstract void deleteTranslationRequest(String trId) throws ServiceException;

    /**
     * Returns the bundle's information included in the translation request.
     * 
     * @param trId      The translation request ID.
     * @param bundleId  The bundle ID.
     * @return  The bundle's information
     * @throws ServiceException when the operation failed.
     */
    public abstract BundleData getTRBundleInfo(String trId, String bundleId)
            throws ServiceException;

    /**
     * Returns a map containing resource string entries indexed by resource key
     * in the bundle and the language included in the translation request.
     * 
     * @param trId      The translation request ID.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @return  A map containing resource string entries indexed by resource key.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, ResourceEntryData> getTRResourceEntries(String trId, String bundleId,
            String language) throws ServiceException;

    /**
     * Returns the resource entry specified by the bundle ID, the language and the resource key
     * included in the translation request.
     * 
     * @param trId      The translation request ID.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param resKey    The resource key.
     * @return  The resource entry data.
     * @throws ServiceException when the operation failed.
     */
    public abstract ResourceEntryData getTRResourceEntry(String trId, String bundleId, String language,
            String resKey) throws ServiceException;

    //
    // {serviceInstanceId}/v2/xliff APIs
    //

    /**
     * Returns bundle contents for the specified source-target language pair in XLIFF 2.0
     * format.
     * 
     * @param srcLanguage   The source language specified by BCP 47 language tag.
     * @param trgLanguage   The target language specified by BCP 47 language tag.
     * @param bundleIds     The set of bundle IDs, or null for all bundles.
     * @param outputXliff   The output XLIFF stream.
     * @throws ServiceException when the operation failed.
     * @throws IOException      when writing XLIFF data to the output stream failed.
     */
    public abstract void getXliffFromBundles(String srcLanguage, String trgLanguage, Set<String> bundleIds,
            OutputStream outputXliff) throws ServiceException, IOException;

    /**
     * Updates bundle contents with the input XLIFF 2.0 stream. The input XLIFF must contains
     * resource entries for a pair of source language and target language.
     * 
     * @param inputXliff    The input XLIFF stream.
     * @throws ServiceException when the operation failed.
     * @throws IOException      when reading XLIFF data from the input stream failed.
     */
    public abstract void updateBundlesWithXliff(InputStream inputXliff) throws ServiceException, IOException;

    /**
     * Returns bundle contents for the specified source-target language pair in XLIFF 2.0
     * format included in the translation request.
     * 
     * @param trId          The translation request ID.
     * @param srcLanguage   The source language specified by BCP 47 language tag.
     * @param trgLanguage   The target language specified by BCP 47 language tag.
     * @param outputXliff   The output XLIFF stream.
     * @throws ServiceException when the operation failed.
     * @throws IOException      when writing XLIFF data to the output stream failed.
     */
    public abstract void getXliffFromTranslationRequest(String trId, String srcLanguage,
            String trgLanguage, OutputStream outputXliff) throws ServiceException, IOException;
}
