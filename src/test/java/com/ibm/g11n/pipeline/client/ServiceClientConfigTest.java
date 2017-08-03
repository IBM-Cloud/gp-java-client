/*  
 * Copyright IBM Corp. 2016
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ibm.g11n.pipeline.client.NewTranslationConfigData.NewMTServiceData;
import com.ibm.g11n.pipeline.client.TranslationConfigData.MTServiceData;

/**
 * Test cases for config operation APIs in ServiceClient.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceClientConfigTest extends AbstractServiceClientTest {
    private static final String WATSON_SERVICE_NAME = "language_translator";
    private static final String CAPITA_SERVICE_NAME = "$ext-capita_smartmate";

    private static final String BASIC_GUID = "$basic";
    private static String WATSON_GUID = null;
    private static String CAPITA_GUID = null;

    private static final String TEST_CONFIG_SRC = "en";
    private static final String TEST_CONFIG_TGT = "es";
    private static boolean TEST_CONFIG = false;

    static {
        if (client != null) {
            try {
                Map<String, MTServiceBindingData> allBindings = client.getAllMTServiceBindings();
                for (Entry<String, MTServiceBindingData> entry : allBindings.entrySet()) {
                    MTServiceBindingData bindingData = entry.getValue();
                    if (WATSON_GUID == null) {
                        if (WATSON_SERVICE_NAME.equals(bindingData.getServiceName())) {
                            WATSON_GUID = entry.getKey();
                        }
                    }
                    if (CAPITA_GUID == null) {
                        if (CAPITA_SERVICE_NAME.equals(bindingData.getServiceName())) {
                            CAPITA_GUID = entry.getKey();
                        }
                    }
                }

                // external service binding is available
                if (WATSON_GUID != null || CAPITA_GUID != null) {
                    Map<String, Map<String, NewTranslationConfigData>> allConfigs =
                            client.getAllTranslationConfigs();
                    Map<String, NewTranslationConfigData> langConfig = allConfigs.get(TEST_CONFIG_SRC);
                    if (langConfig == null) {
                        TEST_CONFIG = true;
                    } else {
                        // test cases which creates/updates/deletes translation configuration
                        // are enabled only when the configuration is not currently available,
                        TEST_CONFIG = !langConfig.containsKey(TEST_CONFIG_TGT);
                    }
                }
            } catch (ServiceException e) {
                // fall through
            }
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    @After
    public void cleanupTestTransConfig() throws ServiceException {
        if (TEST_CONFIG) {
            Map<String, Map<String, NewTranslationConfigData>> allConfigs =
                    client.getAllTranslationConfigs();
            Map<String, NewTranslationConfigData> langConfig = allConfigs.get(TEST_CONFIG_SRC);
            if (langConfig != null && langConfig.containsKey(TEST_CONFIG_TGT)) {
                client.deleteTranslationConfig(TEST_CONFIG_SRC, TEST_CONFIG_TGT);
            }
        }
    }

    //
    // getAllMTServiceBindings
    //

    @Test
    public void getAllMTServiceBindings_WatsonBinding_ShouldContainRequiredFields() throws ServiceException {
        assumeTrue(WATSON_GUID != null);

        Map<String, MTServiceBindingData> all = client.getAllMTServiceBindings();
        MTServiceBindingData bindData = all.get(WATSON_GUID);
        assertNotNull("All bindings should contain Watson Language Translation service instance GUID",
                bindData);

        checkWatsonBinding(bindData);
    }

    @Test
    public void getAllMTServiceBindings_CapitaBinding_ShouldContainRequiredFields() throws ServiceException {
        assumeTrue(CAPITA_GUID != null);

        Map<String, MTServiceBindingData> all = client.getAllMTServiceBindings();
        MTServiceBindingData bindData = all.get(CAPITA_GUID);
        assertNotNull("All bindings should contain Capita SmartMATE instance GUID",
                bindData);

        checkCapitaBinding(bindData);
    }

    //
    // getAvailableMTLanguages
    //

    @Test
    public void getAvailableMTLanguages_CheckCoverage_ShouldReflectAvailableServices() throws ServiceException {
        final Set<String> basicLanguages =
                new HashSet<>(Arrays.asList("de", "fr", "es", "it", "ja", "ko", "pt-BR", "zh-Hans", "zh-Hant"));

        Map<String, Map<String, Set<String>>> available = client.getAvailableMTLanguages();
        Map<String, Set<String>> enMap = available.get("en");
        assertNotNull("MT from English should be available", enMap);

        Set<String> basicAvailable = new HashSet<>();
        boolean hasWatson = false;
        boolean hasCapita = false;

        for (Entry<String, Set<String>> entry : enMap.entrySet()) {
            String targetLang = entry.getKey();
            Set<String> services = entry.getValue();
            if (services.contains(BASIC_GUID)) {
                basicAvailable.add(targetLang);
            }
            if (services.contains(WATSON_GUID)) {
                hasWatson = true;
            }
            if (services.contains(CAPITA_GUID)) {
                hasCapita = true;
            }
        }

        assertEquals("basic service coverage", basicLanguages, basicAvailable);
        assertEquals("Watson service availability", WATSON_GUID != null, hasWatson);
        assertEquals("Capita service availability", CAPITA_GUID != null, hasCapita);
    }

    //
    // getMTServiceBinding
    //

    @Test
    public void getMTServiceBinding_WatsonBinding_ShouldContainRequiredFields()
            throws ServiceException {
        assumeTrue(WATSON_GUID != null);
        MTServiceBindingData bindData = client.getMTServiceBinding(WATSON_GUID);
        checkWatsonBinding(bindData);
    }

    @Test
    public void getMTServiceBinding_CapitaBinding_ShouldContainRequiredFields()
            throws ServiceException {
        assumeTrue(CAPITA_GUID != null);
        MTServiceBindingData bindData = client.getMTServiceBinding(CAPITA_GUID);
        checkCapitaBinding(bindData);
    }

    //
    // getAllTranslationConfig
    //

    @Test
    public void getAllTranslationConfig_Configs_TestLangPairShouldNotExist() throws ServiceException {
        assumeTrue(TEST_CONFIG);
        Map<String, Map<String, NewTranslationConfigData>> allConfigs = client.getAllTranslationConfigs();
        assertNotNull("getAllTranslationConfigs should not return null", allConfigs);

        // When TEST_CONFIG is true, config for en-fr should not exist
        Map<String, NewTranslationConfigData> testSrcConfigs = allConfigs.get(TEST_CONFIG_SRC);
        if (TEST_CONFIG_SRC != null) {
            assertNull("language pair for testing should not exist",
                    testSrcConfigs.get(TEST_CONFIG_TGT));
        }
    }

    //
    // getConfiguredMTLanguages
    //

    @Test
    public void getConfiguredMTLanguages_CompareWithAllAvailable_ShouldBeSubset()
            throws ServiceException {
        final String srcLang = "en";
        Map<String, Map<String, Set<String>>> allAvailable = client.getAvailableMTLanguages();
        assertNotNull("all available MT languages should not be null", allAvailable);
        Map<String, Set<String>> allAvailableEn = allAvailable.get(srcLang);
        assertNotNull("all available MT languaegs for en should not be null", allAvailableEn);
        Set<String> allAvailableEnTargets = allAvailableEn.keySet();

        Map<String, Set<String>> allConfigured = client.getConfiguredMTLanguages();
        assertNotNull("all configured MT languages should not be null", allConfigured);
        Set<String> allConfiguredEnTargets = allConfigured.get(srcLang);

        assertTrue("all configured should be equal or subset of all available MT languages",
                allAvailableEnTargets.containsAll(allConfiguredEnTargets));
    }

    //
    // putTranslationConfig
    //

    @Test
    public void putTranslationConfig_Watson_ShouldBeSaved() throws ServiceException {
        assumeTrue(TEST_CONFIG && WATSON_GUID != null);
        Map<String, Object> params = Collections.singletonMap("model", (Object)"news");
        putTestTransConfig(TEST_CONFIG_SRC, TEST_CONFIG_TGT, WATSON_GUID, params);

        TranslationConfigData transConfig =
                client.getTranslationConfig(TEST_CONFIG_SRC, TEST_CONFIG_TGT);
        checkTestTransConfig(transConfig, WATSON_GUID, params, true);
    }

    @Test
    public void putTranslationConfig_Capita_ShouldBeSaved() throws ServiceException {
        assumeTrue(TEST_CONFIG && CAPITA_GUID != null);
        Map<String, Object> params = new HashMap<>(2);
        params.put("param1", (Object)"val1");
        params.put("param2", (Object)"val2");
        putTestTransConfig(TEST_CONFIG_SRC, TEST_CONFIG_TGT, CAPITA_GUID, params);

        TranslationConfigData transConfig =
                client.getTranslationConfig(TEST_CONFIG_SRC, TEST_CONFIG_TGT);
        checkTestTransConfig(transConfig, CAPITA_GUID, params, true);
        
    }

    //
    // getTranslationConfig
    //

    @Test
    public void getTranslationConfig_NonExisting_ShouldFail() throws ServiceException {
        // Translation configuration for such combination should not exist
        final String sourceLang = "zxx";
        final String targetLang = "qru";

        expectedException.expect(ServiceException.class);
        client.getTranslationConfig(sourceLang, targetLang);
    }

    //
    // deleteTranslationConfig
    //

    @Test
    public void deleteTranslationConfig_NonExisting_ShouldFail() throws ServiceException {
        // Translation configuration for such combination should not exist
        final String sourceLang = "zxx";
        final String targetLang = "qru";

        expectedException.expect(ServiceException.class);
        client.deleteTranslationConfig(sourceLang, targetLang);
    }

    //
    // private utility methods
    //

    private static void checkWatsonBinding(MTServiceBindingData bindData) {
        // These are all required
        assertEquals("service name should be " + WATSON_SERVICE_NAME,
                WATSON_SERVICE_NAME, bindData.getServiceName());
        assertNotNull("service ID should not be null", bindData.getServiceId());
        assertNotNull("updatedBy should not be null", bindData.getUpdatedBy());
        assertNotNull("updatedAt should not be null", bindData.getUpdatedAt());

        // Credentials should not be null, and need all required fields
        Map<String, Object> creds = bindData.getServiceCredentials();
        assertNotNull("serviceCredentials should not be null", creds);
        assertTrue("serviceCredentials should contain 'url'", creds.containsKey("url"));
        assertTrue("serviceCredentials should contain 'username'", creds.containsKey("username"));
        assertTrue("serviceCredentials should contain 'password'", creds.containsKey("password"));
    }

    private static void checkCapitaBinding(MTServiceBindingData bindData) {
        // These are all required
        assertEquals("service name should be " + CAPITA_SERVICE_NAME,
                CAPITA_SERVICE_NAME, bindData.getServiceName());
        assertNotNull("service ID should not be null", bindData.getServiceId());
        assertNotNull("updatedBy should not be null", bindData.getUpdatedBy());
        assertNotNull("updatedAt should not be null", bindData.getUpdatedAt());

        // Credentials should not be null, and need all required fields
        Map<String, Object> creds = bindData.getServiceCredentials();
        assertNotNull("serviceCredentials should not be null", creds);
        assertTrue("serviceCredentials should contain 'base_url'", creds.containsKey("base_url"));
        assertTrue("serviceCredentials should contain 'api_version'", creds.containsKey("api_version"));
        assertTrue("serviceCredentials should contain 'client_id'", creds.containsKey("client_id"));
        assertTrue("serviceCredentials should contain 'client_secret'", creds.containsKey("client_secret"));
    }

    private static void putTestTransConfig(String srcLang, String tgtLang, 
            String mtServiceInstanceId, Map<String, Object> params) throws ServiceException {
        NewMTServiceData mtService = new NewMTServiceData(mtServiceInstanceId);
        if (params != null) {
            mtService.setParams(params);
        }
        NewTranslationConfigData transConfig = new NewTranslationConfigData();
        transConfig.setMTServiceData(mtService);
        client.putTranslationConfig(srcLang, tgtLang, transConfig);
    }

    private static void checkTestTransConfig(TranslationConfigData transConfig,
            String expInstanceId, Map<String, Object> expParams, boolean checkDateRecent) {
        assertEquals("the updated by filed should be the test user",
                getUpdatedByValue(), transConfig.getUpdatedBy());
        if (checkDateRecent) {
            assertTrue("the updated at field should be recent",
                    isRecent(transConfig.getUpdatedAt()));
        }

        MTServiceData mtServiceData = transConfig.getMTServiceData();
        assertNotNull("MT service data should not be null", mtServiceData);
        assertEquals("MT service instance ID should match", expInstanceId,
                mtServiceData.getServiceInstanceId());
        assertEquals("MT service params should match", expParams, mtServiceData.getParams());
    }
}
