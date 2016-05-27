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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for bundle operation APIs in ServiceClient.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceClientBundleTest extends AbstractServiceClientTest {

    private static final String BUNDLE_PREFIX = "junit-bundle-";
    private static final String DUMMY_FAIL_LANG = "zxx";
    private static final String TRANSLIT_LANG = "qru";

    private static int WAIT_TIME = 100;             // 100ms
    private static int WAIT_TIME_LONG = 1000;       // 1 sec

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    @After
    public void cleanupBundles() throws ServiceException {
        if (client != null) {
            Set<String> bundleIds = client.getBundleIds();
            for (String bundleId : bundleIds) {
                if (isTestBundleId(bundleId)) {
                    client.deleteBundle(bundleId);
                }
            }
        }
    }

    //
    // getServiceInfo
    //

    @Test
    public void getServiceInfo_SupportedTranslation_ShouldBeConstant() throws ServiceException {
        Set<String> expectedTargetsEn = new HashSet<>(Arrays.asList(
                "de", "es", "fr", "it", "ja", "ko", "pt-BR", "zh-Hans", "zh-Hant"));
        Map<String, Set<String>> expectedMap = Collections.singletonMap("en", expectedTargetsEn);

        ServiceInfo serviceInfo = client.getServiceInfo();
        Map<String, Set<String>> supported = serviceInfo.getSupportedTranslation();
        assertEquals("Supported translation should be from English to 9 fixed languages",
                expectedMap, supported);
    }

    //
    // getBundleIds
    //

    @Test
    public void getBundleIds_InitialState_ShouldNotContainTestBundles() throws ServiceException {
        Set<String> bundleIds = client.getBundleIds();
        for (String bundleId : bundleIds) {
            assertFalse("Bundle ID list should not contain a bundle starts with " + BUNDLE_PREFIX,
                    isTestBundleId(bundleId));
        }
    }

    @Test
    public void getBundleIds_AfterCreate_ShouldContainTestBundles() throws ServiceException {
        createBundleWithLanguages(testBundleId("bundle1"), "en");
        createBundleWithLanguages(testBundleId("bundle2"), "en");

        Set<String> bundleIds = client.getBundleIds();
        int numTestBundles = 0;
        for (String bundleId : bundleIds) {
            if (isTestBundleId(bundleId)) {
                numTestBundles++;
            }
        }
        assertEquals("Number of test bundles should be 2", 2, numTestBundles);
    }

    //
    // createBundle
    //

    @Test
    public void createBundle_NonExisting_ShouldPass() throws ServiceException {
        final String bundleId = testBundleId("bundle1");
        final String source = "en";
        final Set<String> targets = new HashSet<>(Arrays.asList("de", "zh-Hans"));

        // Create a new Bundle with de/zh-Hans as target languages
        createBundleWithLanguages(bundleId, source, targets);

        // Get bundle information
        BundleData bundleData = client.getBundleInfo(bundleId);
        assertEquals("source language should be " + source,
                source, bundleData.getSourceLanguage());
        assertEquals("target langauges should be " + targets,
                targets, bundleData.getTargetLanguages());
    }

    @Test
    public void createBundle_ValidNonMTLanguage_ShouldPass() throws ServiceException {
        final String bundleId = testBundleId("bundle1");
        final String source = "uz";
        final Set<String> targets = new HashSet<>(Arrays.asList("hi", "kok", "sr-Latn"));

        createBundleWithLanguages(bundleId, source, targets);

        BundleData bundleData = client.getBundleInfo(bundleId);
        assertEquals("source language should be " + source,
                source, bundleData.getSourceLanguage());
        assertEquals("target langauges should be " + targets,
                targets, bundleData.getTargetLanguages());
    }

    @Test
    public void createBundle_Existing_ShouldFail() throws ServiceException {
        final String bundleId = testBundleId("bundle1");

        // Create a new Bundle
        createBundleWithLanguages(bundleId, "en");

        // Create an another bundle with same bundle ID
        expectedException.expect(ServiceException.class);
        createBundleWithLanguages(bundleId, "en");
    }

    @Test
    public void createBundle_EmptySourceLanguage_ShouldFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        createBundleWithLanguages(testBundleId("bundle1"), "");
    }

    @Test
    public void createBundle_BadSourceLanguage_ShouldFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        createBundleWithLanguages(testBundleId("bundle1"), "English");
    }

    @Test
    public void createBundle_BadTargetLanguage_ShouldFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        createBundleWithLanguages(testBundleId("bundle1"), "en", "de", "invalid-language-code");
    }

    @Test
    public void createBundle_TooLongID_ShouldFail() throws ServiceException {
        StringBuilder longId = new StringBuilder(BUNDLE_PREFIX);
        for (int i = longId.length(); i < 256; i++) {
            longId.append('x');
        }
        expectedException.expect(ServiceException.class);
        createBundleWithLanguages(longId.toString(), "en");
    }

    @Test
    public void createBundle_BadID_ShouldFail() throws ServiceException {
        final String badBundleId = testBundleId("My Bundle");
        expectedException.expect(ServiceException.class);
        createBundleWithLanguages(badBundleId, "en");
    }

    //
    // getBundleInfo
    //

    @Test
    public void getBundleInfo_UpdatedBy_ShouldMatchThisUser() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en");
        BundleData bundleData = client.getBundleInfo(bundleId);
        assertEquals("updatedBy field should be the test user",
                getUpdatedByValue(),
                bundleData.getUpdatedBy());
    }

    @Test
    public void getBundleInfo_UpdatedAt_ShouldBeRecent() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en");
        BundleData bundleData = client.getBundleInfo(bundleId);
        Date updatedAt = bundleData.getUpdatedAt();
        assertTrue("updatedAt should be recent", isRecent(updatedAt));
    }

    @Test
    public void getBundleInfo_Default_ShouldNotBeReadOnly() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en");
        BundleData bundleData = client.getBundleInfo(bundleId);
        assertFalse("readOnly should be false", bundleData.isReadOnly());
    }

    @Test
    public void getBundleInfo_NonExisting_ShouldFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        client.getBundleInfo(testBundleId("bundle0"));
    }

    //
    // getBundleMetrics
    //
    @Ignore // TODO: work item 23646
    @Test
    public void getBundleMetrics_NonEmpty_MetricsEntryCountShouldMatch() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String[] targets = {"ja", "ko"};
        createBundleWithLanguages(bundleId, source, targets);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        BundleMetrics bundleMetrics = client.getBundleMetrics(bundleId);
        checkBundleMetrics(bundleMetrics, source, targets, strings.size());
    }

    @Ignore // TODO: work item 23646
    @Test
    public void getBundleMetrics_Empty_MetricsShouldBeEmpty() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String[] targets = {"ja", "ko"};
        createBundleWithLanguages(bundleId, source, targets);

        BundleMetrics bundleMetrics = client.getBundleMetrics(bundleId);
        Map<String, String> strings = client.getResourceStrings(bundleId, source, false);
        checkBundleMetrics(bundleMetrics, source, targets, strings.size());
    }

    @Test
    public void getBundleMetrics_NonExisting_ShouldFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        client.getBundleMetrics(testBundleId("bundle0"));
    }

    //
    // updateBundle
    //

    @Test
    public void updateBundle_SetTargetLangs_ShouldOverwrite() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en", "fr", "zh-Hans");

        Set<String> newTargets = new HashSet<>(Arrays.asList("zh-Hans", "zh-Hant"));
        BundleDataChangeSet changes = new BundleDataChangeSet();
        changes.setTargetLanguages(newTargets);
        client.updateBundle(bundleId, changes);

        BundleData bundleData = client.getBundleInfo(bundleId);
        assertEquals("target languages should be updated",
                newTargets, bundleData.getTargetLanguages());
    }

    @Test
    public void updateBundle_AddBadTargetLang_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String[] origTargets = {"fr", "de"};
        createBundleWithLanguages(bundleId, "en", origTargets);

        Set<String> newTargets = new HashSet<>(Arrays.asList(origTargets));
        newTargets.add("it");
        newTargets.add("Japanese");
        BundleDataChangeSet changes = new BundleDataChangeSet();
        changes.setTargetLanguages(newTargets);

        try {
            client.updateBundle(bundleId, changes);
            fail("bad target language should throw ServiceException");
        } catch (ServiceException e) {
            // ok
        }

        BundleData bundleData = client.getBundleInfo(bundleId);
        assertEquals("target languages should not be updated",
                new HashSet<>(Arrays.asList(origTargets)), bundleData.getTargetLanguages());
    }


    @Test
    public void updateBundle_Metadata_ShouldAddModifyDelete() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key 1", "value 1");

        // Add metadata
        BundleDataChangeSet changes = new BundleDataChangeSet();
        changes.setMetadata(metadata);
        client.updateBundle(bundleId, changes);
        BundleData bundleData = client.getBundleInfo(bundleId);
        assertEquals("metadata should be added", metadata, bundleData.getMetadata());

        // Modify
        metadata.put("key 1", null);        // delete key 1
        metadata.put("key 2", "value 2");   // add key 2
        changes.setMetadata(metadata);

        client.updateBundle(bundleId, changes);
        bundleData = client.getBundleInfo(bundleId);
        assertEquals("metadata should be modified",
                Collections.singletonMap("key 2", "value 2"),
                bundleData.getMetadata());

        // Delete
        changes.setMetadata(Collections.<String, String>emptyMap());
        client.updateBundle(bundleId, changes);

        bundleData = client.getBundleInfo(bundleId);
        assertNull("metadata should be deleted", bundleData.getMetadata());
    }

    //
    // deleteBundle
    //

    @Test
    public void deleteBundle_Existing_ShouldDelete() throws ServiceException {
        Set<String> bundleIds = client.getBundleIds();
        String bundleId = testBundleId("bundle1");
        assertFalse(bundleId + " should not be yet there", bundleIds.contains(bundleId));

        createBundleWithLanguages(bundleId, "en");
        bundleIds = client.getBundleIds();
        assertTrue(bundleId + " should be already created", bundleIds.contains(bundleId));

        // Now delete it
        client.deleteBundle(bundleId);
        bundleIds = client.getBundleIds();
        assertFalse(bundleId + " should be deleted", bundleIds.contains(bundleId));
    }

    @Test
    public void deleteBundle_NonExisting_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle0");
        expectedException.expect(ServiceException.class);
        client.deleteBundle(bundleId);
    }


    private static final String[][] TEST_RES1 = {
        {"menu.help", "Help"},
        {"menu.file", "File"},
        {"menu.edit", "Edit"}
    };

    //
    // getResourceStrings
    //

    @Test
    public void getResourceStrings_SourceLang_ShouldMatch() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Map<String, String> srcStrings1 = client.getResourceStrings(bundleId, source, false);
        assertEquals("gerResourceStrings (fallback:false) for the source should match the original",
                strings, srcStrings1);

        Map<String, String> srcStrings2 = client.getResourceStrings(bundleId, source, true);
        assertEquals("getResourceStrings (fallback:true) for the source should match the original",
                strings, srcStrings2);
    }

    @Test
    public void getResourceStrings_NonExistingLang_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        expectedException.expect(ServiceException.class);
        client.getResourceStrings(bundleId, "xx", true);
    }

    @Test
    public void getResourceStrings_Fallback_ShouldReturnSource() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source, DUMMY_FAIL_LANG);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Map<String, String> targetStrings = client.getResourceStrings(bundleId,
                DUMMY_FAIL_LANG, true);
        assertEquals("gerResourceStrings for " + DUMMY_FAIL_LANG
                + " with fallback:true should match the original",
                strings, targetStrings);
    }

    @Test
    public void getResourceStrings_NoFallback_ShouldReturnEmpty() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source, DUMMY_FAIL_LANG);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Map<String, String> targetStrings = client.getResourceStrings(bundleId,
                DUMMY_FAIL_LANG, false);
        assertTrue("gerResourceStrings for " + DUMMY_FAIL_LANG
                + " with fallback:false should return empty strings",
                targetStrings.isEmpty());
    }

    //
    // getResourceEntries
    //

    @Test
    public void getResourceEntries_SourceLang_ShouldMach() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);

        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);
        client.uploadResourceEntries(bundleId, source, resources);

        Map<String, ResourceEntryData> srcResources = client.getResourceEntries(bundleId, source);
        compareSourceResourceEntries(TEST_RES1, srcResources, true, true);
    }

    @Test
    public void getResourceEntries_NonExistingLang_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        expectedException.expect(ServiceException.class);
        client.getResourceEntries(bundleId, "xx");
    }

    @Test
    public void getResourceEntries_MTFailure_ShouldReturnNonEmpty() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source, DUMMY_FAIL_LANG);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Map<String, ResourceEntryData> targetEntries =
                client.getResourceEntries(bundleId, DUMMY_FAIL_LANG);
        assertEquals("gerResourceEntries for " + DUMMY_FAIL_LANG
                + " should return entries",
                TEST_RES1.length, targetEntries.size());
    }

    @Test
    public void getResourceEntries_French_ShouldBeTranslated()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = "fr";
        createBundleWithLanguages(bundleId, source, target);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        Map<String, ResourceEntryData> targetEntries =
                client.getResourceEntries(bundleId, target);
        checkBeingTranslated(TEST_RES1, targetEntries);
    }

    //
    // getLanguageMetrics
    //

    @Test
    public void getLanguageMetrics_SourceLang_MetricsEntryCountShouldMatch()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        LanguageMetrics metrics = client.getLanguageMetrics(bundleId, source);

        EnumMap<TranslationStatus, Integer> transMetrics = metrics.getTranslationStatusMetrics();
        assertNotNull("translation status metrics for " + source, transMetrics);
        checkTranslationStatusMetrics(metrics.getTranslationStatusMetrics(),
                strings.size(),
                EnumSet.of(TranslationStatus.SOURCE_LANGUAGE));

        ReviewStatusMetrics reviewMetrics = metrics.getReviewStatusMetrics();
        assertNotNull("review status metrics for " + source, reviewMetrics);
        checkReviewStatusMetrics(reviewMetrics, strings.size(), 0);

        Map<String, Integer> partnerMetrics = metrics.getPartnerStatusMetrics();
        assertNotNull("partner status metrics for " + source, partnerMetrics);
        assertEquals("partner status metrics should be empty",
                Collections.<String, Integer>emptyMap(), partnerMetrics);
    }

    @Test
    public void getLanguageMetrics_TargetLang_MetricsEntryCountShouldMatch()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = "zh-Hant";
        createBundleWithLanguages(bundleId, source, target);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        LanguageMetrics metrics = client.getLanguageMetrics(bundleId, target);

        EnumMap<TranslationStatus, Integer> transMetrics = metrics.getTranslationStatusMetrics();
        assertNotNull("translation status metrics for " + target, transMetrics);
        checkTranslationStatusMetrics(metrics.getTranslationStatusMetrics(),
                strings.size(),
                EnumSet.of(TranslationStatus.TRANSLATED, TranslationStatus.IN_PROGRESS));

        ReviewStatusMetrics reviewMetrics = metrics.getReviewStatusMetrics();
        assertNotNull("review status metrics for " + target, reviewMetrics);
        checkReviewStatusMetrics(reviewMetrics, strings.size(), 0);

        Map<String, Integer> partnerMetrics = metrics.getPartnerStatusMetrics();
        assertNotNull("partner status metrics for " + target, partnerMetrics);
        assertEquals("partner status metrics should be empty",
                Collections.<String, Integer>emptyMap(), partnerMetrics);
    }

    @Test
    public void getLanguageMetrics_NonExisting_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source, "fr");

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        expectedException.expect(ServiceException.class);
        client.getLanguageMetrics(bundleId, "de");
    }

    //
    // uploadResourceStrings
    //

    @Test
    public void uploadResourceStrings_NonExistinBundle_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle0");
        Map<String, String> strings = toStringMap(TEST_RES1);

        expectedException.expect(ServiceException.class);
        client.uploadResourceStrings(bundleId, "en", strings);
    }

    @Test
    public void uploadResourceStrings_NonExistingLang_ShouldAddLang() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);

        // Upload source lang
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        // Upload target lang
        String target = "de";
        client.uploadResourceStrings(bundleId, target, strings);

        BundleData bundleData = client.getBundleInfo(bundleId);
        Set<String> targets = bundleData.getTargetLanguages();
        assertNotNull("target languages should not be null", targets);
        assertTrue("target languages should contain " + target, targets.contains(target));
    }

    @Test
    public void uploadResourceStrings_UploadSuperSet_ShouldOnlyAddNewKey()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // Add new key
        String newKey = "NewKey";
        strings.put(newKey, "NewVal");
        int numResources = strings.size();
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // source language
        Map<String, String> resultStrings = client.getResourceStrings(bundleId, source, false);
        assertEquals("number of source resources should be incremented",
                numResources, resultStrings.size());
        assertTrue("the new key should be added to source",
                resultStrings.containsKey(newKey));

        // target language
        resultStrings = client.getResourceStrings(bundleId, target, false);
        assertEquals("number of target resources should be incremented",
                numResources, resultStrings.size());
        assertTrue("the new key should be added to target",
                resultStrings.containsKey(newKey));
    }

    @Test
    public void uploadResourceStrings_UploadSubSet_ShouldDeleteMissingKey()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME_LONG);

        // Remove a key
        String removeKey = TEST_RES1[0][0];
        strings.remove(removeKey);
        int numResources = strings.size();
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // source language
        Map<String, String> resultStrings = client.getResourceStrings(bundleId, source, false);
        assertEquals("number of source resources should be decremented",
                numResources, resultStrings.size());
        assertFalse("the new key should be removed from source",
                resultStrings.containsKey(removeKey));

        // target language
        resultStrings = client.getResourceStrings(bundleId, target, false);
        assertEquals("number of target resources should be decremented",
                numResources, resultStrings.size());
        assertFalse("the new key should be removed to target",
                resultStrings.containsKey(removeKey));
    }

    @Test
    public void uploadResourceStrings_UploadTarget_ShouldIgnoreNonExistingKey()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // overwrite target language, with extra key
        String extraKey = "extraKey";
        strings.put(extraKey, "extraVal");
        client.uploadResourceStrings(bundleId, target, strings);

        Map<String, String> resultStrings = client.getResourceStrings(bundleId, target, false);
        assertFalse("the result should not contain " + extraKey,
                resultStrings.containsKey(extraKey));
    }

    @Test
    public void uploadResourceStrings_UploadTarget_ShouldKeepExistingKeys()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // upload an existing key
        String key = TEST_RES1[0][0];
        String modVal = "XYZ";
        client.uploadResourceStrings(bundleId, target,
                Collections.singletonMap(key, modVal));

        Thread.sleep(WAIT_TIME);

        // make sure the set of keys is not change
        Map<String, String> resultStrings = client.getResourceStrings(bundleId, target, false);
        assertEquals("set of keys should not be changed", strings.keySet(), resultStrings.keySet());
        assertEquals("value of " + key + " should be updated", modVal, resultStrings.get(key));
    }

    //
    // uploadResourceEntries
    //

    @Test
    public void uploadResourceEntries_NonExistinBundle_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle0");
        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);

        expectedException.expect(ServiceException.class);
        client.uploadResourceEntries(bundleId, "en", resources);

        
    }

    @Test
    public void uploadResourceEntries_NonExistingLang_ShouldAddLang() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        createBundleWithLanguages(bundleId, source);

        // Upload source lang
        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);
        client.uploadResourceEntries(bundleId, source, resources);

        // Upload target lang
        String target = "de";
        client.uploadResourceEntries(bundleId, target, resources);

        BundleData bundleData = client.getBundleInfo(bundleId);
        Set<String> targets = bundleData.getTargetLanguages();
        assertNotNull("target languages should not be null", targets);
        assertTrue("target languages should contain " + target, targets.contains(target));
    }

    @Test
    public void uploadResourceEntries_UploadSuperSet_ShouldOnlyAddNewKey() throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);
        client.uploadResourceEntries(bundleId, source, resources);

        Thread.sleep(WAIT_TIME);

        // Add new key
        String newKey = "NewKey";
        resources.put(newKey, new NewResourceEntryData("NewVal"));
        int numResources = resources.size();
        client.uploadResourceEntries(bundleId, source, resources);

        Thread.sleep(WAIT_TIME);

        // source language
        Map<String, ResourceEntryData> resultResources = client.getResourceEntries(bundleId, source);
        assertEquals("number of source resources should be incremented", numResources, resultResources.size());
        assertTrue("the new key should be added to source", resultResources.containsKey(newKey));

        // target language
        resultResources = client.getResourceEntries(bundleId, target);
        assertEquals("number of target resources should be incremented", numResources, resultResources.size());
        assertTrue("the new key should be added to target", resultResources.containsKey(newKey));
    }

    @Test
    public void uploadResourceEntries_UploadSubSet_ShouldDeleteMissingKey() throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);
        client.uploadResourceEntries(bundleId, source, resources);

        Thread.sleep(WAIT_TIME_LONG);

        // Remove a key
        String removeKey = TEST_RES1[0][0];
        resources.remove(removeKey);
        int numResources = resources.size();
        client.uploadResourceEntries(bundleId, source, resources);

        Thread.sleep(WAIT_TIME);

        // source language
        Map<String, ResourceEntryData> resultResources = client.getResourceEntries(bundleId, source);
        assertEquals("number of source resources should be decremented", numResources, resultResources.size());
        assertFalse("the new key should be added to source", resultResources.containsKey(removeKey));

        // target language
        resultResources = client.getResourceEntries(bundleId, target);
        assertEquals("number of target resources should be decremented", numResources, resultResources.size());
        assertFalse("the new key should be added to target", resultResources.containsKey(removeKey));
    }

    @Test
    public void uploadResourceEntries_UploadTarget_ShouldIgnoreNonExistingKey() throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);
        client.uploadResourceEntries(bundleId, source, resources);

        Thread.sleep(WAIT_TIME);

        // overwrite target language, with extra key
        String extraKey = "extraKey";
        resources.put(extraKey, new NewResourceEntryData("extraVal"));
        client.uploadResourceEntries(bundleId, target, resources);

        Map<String, ResourceEntryData> resultResources =
                client.getResourceEntries(bundleId, target);
        assertFalse("the result should not contain " + extraKey,
                resultResources.containsKey(extraKey));
    }

    @Test
    public void uploadResourceEntries_UploadTarget_ShouldKeepExistingKeys()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;
        createBundleWithLanguages(bundleId, source, target);

        Map<String, NewResourceEntryData> resources = toNewResourceEntryMap(TEST_RES1, true);
        client.uploadResourceEntries(bundleId, source, resources);

        Thread.sleep(WAIT_TIME);

        // upload an existing key
        String key = TEST_RES1[0][0];
        String modVal = "XYZ";
        client.uploadResourceEntries(bundleId, target,
                Collections.singletonMap(key, new NewResourceEntryData(modVal)));

        Thread.sleep(WAIT_TIME);

        // make sure the set of keys is not change
        Map<String, ResourceEntryData> resultResources =
                client.getResourceEntries(bundleId, target);
        assertEquals("set of keys should not be changed",
                resources.keySet(), resultResources.keySet());
        assertEquals("value of " + key + " should be updated",
                modVal, resultResources.get(key).getValue());
    }

    //
    // updateResourceStrings
    //

    @Test
    public void updateResourceStrings_NonExistingBundle_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle0");
        Map<String, String> strings = Collections.singletonMap("key1", "val1");

        expectedException.expect(ServiceException.class);
        client.updateResourceStrings(bundleId, "en", strings, false);
    }

    @Test
    public void updateResourceStrings_NoExistingLang_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en");

        Map<String, String> strings = Collections.singletonMap("key1", "val1");

        expectedException.expect(ServiceException.class);
        client.updateResourceStrings(bundleId, "de", strings, false);
    }

    @Test
    public void updateResourceStrings_NewKeyInSource_ShouldAddKey()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;

        // create bundle and upload initial resources
        createBundleWithLanguages(bundleId, source, target);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // new source key
        String newKey = "NewKey";
        String newVal = "NewVal";
        Map<String, String> changeStrings = toStringMap(new String[][] {{newKey, newVal}});
        client.updateResourceStrings(bundleId, source, changeStrings, false);

        Thread.sleep(WAIT_TIME);

        // source strings should contain the new key
        Map<String, String> sourceStrings = client.getResourceStrings(bundleId, source, false);
        String srcNewVal = sourceStrings.get(newKey);
        assertEquals("source lang should contain the one just added", newVal, srcNewVal);
        assertEquals("number of strings in source should be incremented", strings.size() + 1, sourceStrings.size());

        // target strings should also contain the new key
        Map<String, String> targetStrings = client.getResourceStrings(bundleId, target, false);
        assertTrue("target lang should contain the one just added", targetStrings.containsKey(newKey));
        assertEquals("number of strings in source should be incremented", strings.size() + 1, targetStrings.size());
    }

    @Test
    public void updateResourceStrings_NewKeyInTarget_ShouldIgnore()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;

        // create bundle and upload initial resources
        createBundleWithLanguages(bundleId, source, target);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // update target key not available in source language.
        Map<String, String> changeStrings = toStringMap(new String[][] {{"NewKey", "NewVal"}});
        client.updateResourceStrings(bundleId, target, changeStrings, false);

        Thread.sleep(WAIT_TIME);

        // source strings should not be affected
        Map<String, String> sourceStrings = client.getResourceStrings(bundleId, source, false);
        assertEquals("source lang should not be affected", strings, sourceStrings);

        // target strings should not be affected too.
        Map<String, String> targetStrings = client.getResourceStrings(bundleId, target, false);
        assertFalse("target lang should not contain the new key", targetStrings.containsKey("NewKey"));
        assertEquals("number of strings in target should be same", strings.size(), targetStrings.size());
    }

    @Test
    public void updateResourceStrings_DeleteKeyInSource_ShouldDelete()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;

        // create bundle and upload initial resources
        createBundleWithLanguages(bundleId, source, target);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // existing key with null value
        String key = TEST_RES1[0][0];
        Map<String, String> changeStrings = toStringMap(new String[][] {{key, null}});
        client.updateResourceStrings(bundleId, source, changeStrings, false);

        Thread.sleep(WAIT_TIME);

        // key should be deleted in the source language
        Map<String, String> sourceStrings = client.getResourceStrings(bundleId, source, false);
        assertFalse("source strings should not contain " + key, sourceStrings.containsKey(key));

        // it should be also deleted in the target language
        Map<String, String> targetStrings = client.getResourceStrings(bundleId, target, false);
        assertFalse("target strings should not contain " + key, targetStrings.containsKey(key));
    }

    // TODO - Add more test cases

    //
    // updateResourceEntries
    //

    @Test
    public void updateResourceEntries_NonExistingBundle_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle0");
        String[][] strings = {{"key1", "val1"}};
        Map<String, ResourceEntryDataChangeSet> changes = toResourceEntryChangeMap(strings);

        expectedException.expect(ServiceException.class);
        client.updateResourceEntries(bundleId, "en", changes, false);
    }

    @Test
    public void updateResourceEntries_NoExistingLang_ShouldFail() throws ServiceException {
        String bundleId = testBundleId("bundle1");
        createBundleWithLanguages(bundleId, "en");

        String[][] strings = {{"key1", "val1"}};
        Map<String, ResourceEntryDataChangeSet> changes = toResourceEntryChangeMap(strings);

        expectedException.expect(ServiceException.class);
        client.updateResourceEntries(bundleId, "de", changes, false);
    }

    @Test
    public void updateResourceEntries_NewKeyInSource_ShouldAddKey()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;

        // create bundle and upload initial resources
        createBundleWithLanguages(bundleId, source, target);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // add new resource entry
        String newKey = "NewKey";
        String newVal = "NewVal";
        Map<String, ResourceEntryDataChangeSet> changeResources = toResourceEntryChangeMap(new String[][] {{newKey, newVal}});
        client.updateResourceEntries(bundleId, source, changeResources, false);

        Thread.sleep(WAIT_TIME);

        // source strings should contain the new key
        Map<String, String> sourceStrings = client.getResourceStrings(bundleId, source, false);
        String srcNewVal = sourceStrings.get(newKey);
        assertEquals("source lang should contain the one just added", newVal, srcNewVal);
        assertEquals("number of strings in source should be incremented", strings.size() + 1, sourceStrings.size());

        // target strings should also contain the new key
        Map<String, String> targetStrings = client.getResourceStrings(bundleId, target, false);
        assertTrue("target lang should contain the one just added", targetStrings.containsKey(newKey));
        assertEquals("number of strings in source should be incremented", strings.size() + 1, targetStrings.size());
    }

    @Test
    public void updateResourceEntries_NewKeyInTarget_ShouldIgnore()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;

        // create bundle and upload initial resources
        createBundleWithLanguages(bundleId, source, target);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // update target key not available in source language.
        Map<String, ResourceEntryDataChangeSet> changeResources
            = toResourceEntryChangeMap(new String[][] {{"NewKey", "NewVal"}});
        client.updateResourceEntries(bundleId, target, changeResources, false);

        Thread.sleep(WAIT_TIME);

        // source strings should not be affected
        Map<String, String> sourceStrings = client.getResourceStrings(bundleId, source, false);
        assertEquals("source lang should not be affected", strings, sourceStrings);

        // target strings should not be affected too.
        Map<String, String> targetStrings = client.getResourceStrings(bundleId, target, false);
        assertFalse("target lang should not contain the new key", targetStrings.containsKey("NewKey"));
        assertEquals("number of strings in target should be same", strings.size(), targetStrings.size());
    }

    @Test
    public void updateResourceEntries_DeleteKeyInSource_ShouldDelete()
            throws ServiceException, InterruptedException {
        String bundleId = testBundleId("bundle1");
        String source = "en";
        String target = TRANSLIT_LANG;

        // create bundle and upload initial resources
        createBundleWithLanguages(bundleId, source, target);
        Map<String, String> strings = toStringMap(TEST_RES1);
        client.uploadResourceStrings(bundleId, source, strings);

        Thread.sleep(WAIT_TIME);

        // existing key with null value
        String key = TEST_RES1[0][0];
        Map<String, ResourceEntryDataChangeSet> changeResources =
                toResourceEntryChangeMap(new String[][] {{key, null}});
        client.updateResourceEntries(bundleId, source, changeResources, false);

        Thread.sleep(WAIT_TIME);

        // key should be deleted in the source language
        Map<String, String> sourceStrings = client.getResourceStrings(bundleId, source, false);
        assertFalse("source strings should not contain " + key, sourceStrings.containsKey(key));

        // it should be also deleted in the target language
        Map<String, String> targetStrings = client.getResourceStrings(bundleId, target, false);
        assertFalse("target strings should not contain " + key, targetStrings.containsKey(key));
    }

    // TODO - add more test cases

    //
    // getResourceEntry
    //

    // TODO - Add test cases

    
    //
    // updateResourceEntry
    //

    // TODO - Add test cases



    //
    // private utility methods
    //

    private static String testBundleId(String id) {
        return BUNDLE_PREFIX + id;
    }

    private static boolean isTestBundleId(String bundleId) {
        return bundleId.startsWith(BUNDLE_PREFIX);
    }

    private static void createBundleWithLanguages(String bundleId, String source,
            String... targets) throws ServiceException {
        Set<String> targetSet = null;
        if (targets != null) {
            targetSet = new HashSet<String>(Arrays.asList(targets));
        }
        createBundleWithLanguages(bundleId, source, targetSet);
    }

    private static void createBundleWithLanguages(String bundleId, String source,
            Set<String> targets) throws ServiceException {
        NewBundleData newBundleData = new NewBundleData(source);
        if (targets != null) {
            newBundleData.setTargetLanguages(targets);
        }
        client.createBundle(bundleId, newBundleData);
    }

    private static Map<String, String> toStringMap(String[][] resArray) {
        Map<String, String> stringMap = new HashMap<>(resArray.length);
        for (String[] singleResData : resArray) {
            stringMap.put(singleResData[0], singleResData[1]);
        }
        return stringMap;
    }

    private static Map<String, NewResourceEntryData> toNewResourceEntryMap(String[][] resArray,
            boolean withSequence) {
        Map<String, NewResourceEntryData> newResEntryMap = new HashMap<>(resArray.length);
        int seq = 1;
        for (String[] singleResData : resArray) {
            if (singleResData[1] != null) {
                NewResourceEntryData newResEntry = new NewResourceEntryData(singleResData[1]);
                if (withSequence) {
                    newResEntry.setSequenceNumber(Integer.valueOf(seq++));
                }
                newResEntryMap.put(singleResData[0], newResEntry);
            } else {
                newResEntryMap.put(singleResData[0], null);
            }
        }
        return newResEntryMap;
    }

    private static Map<String, ResourceEntryDataChangeSet> toResourceEntryChangeMap(
            String[][] resArray) {
        Map<String, ResourceEntryDataChangeSet> changeResEntryMap = new HashMap<>(resArray.length);
        for (String[] singleResData : resArray) {
            if (singleResData[1] != null) {
                ResourceEntryDataChangeSet changes = new ResourceEntryDataChangeSet();
                changes.setValue(singleResData[1]);
                changeResEntryMap.put(singleResData[0], changes);
            } else {
                changeResEntryMap.put(singleResData[0], null);
            }
        }
        return changeResEntryMap;
    }

    private static void compareSourceResourceEntries(String[][] expectedArray,
            Map<String, ResourceEntryData> actualMap, boolean checkSeq, boolean checkDateRecent) {

        assertEquals("numbers of resources are different",
                expectedArray.length, actualMap.size());

        String expUpdBy = getUpdatedByValue();
        int seq = 0;
        for (String[] expected : expectedArray) {
            String resKey = expected[0];
            String resVal = expected[1];

            ResourceEntryData actualEntry = actualMap.get(resKey);
            assertNotNull("the results should contain " + resKey, actualEntry);
            assertEquals("the result value should match the original",
                    resVal, actualEntry.getValue());

            assertEquals("the updatedBy field should match the user",
                    expUpdBy, actualEntry.getUpdatedBy());
            if (checkDateRecent) {
                assertTrue("the updatedAt field should be recent",
                        isRecent(actualEntry.getUpdatedAt()));
            }

            if (checkSeq) {
                seq++;
                assertNotNull("the sequenceNumber field should not be null",
                        actualEntry.getSequenceNumber());
                assertEquals("the sequenceNumber field should match",
                        seq, actualEntry.getSequenceNumber().intValue());
            }
        }
    }

    private static void checkBeingTranslated(String[][] sourceResArray,
            Map<String, ResourceEntryData> targetEntries) {
        assertEquals("number of resources are different",
                sourceResArray.length, targetEntries.size());
        for (String[] sourceEntry : sourceResArray) {
            String key = sourceEntry[0];
            ResourceEntryData targetEntry = targetEntries.get(key);
            assertNotNull("resource entry for key " + key + " is not found in the target",
                    targetEntry);
            assertEquals("the source value for key " + key
                    + " in the target entry does not match the source entry",
                    sourceEntry[1], targetEntry.getSourceValue());
            TranslationStatus status = targetEntry.getTranslationStatus();
            if (status == TranslationStatus.TRANSLATED) {
                assertNotNull("the translation status for key " + key
                        + " is TRANSLATED, but the value is null",
                        targetEntry.getValue());
            } else if (status == TranslationStatus.IN_PROGRESS) {
                assertNull("the translation status for key " + key
                        + " is IN_PROGRESS, but the value is not null",
                        targetEntry.getValue());
            } else {
                fail("the translation status for key " + key + " is not TRANSLATED or IN_PROGRESS");
            }
        }
    }

    private static void checkBundleMetrics(BundleMetrics metrics,
                                            String source, String[] targets, int numResources) {
        Map<String, EnumMap<TranslationStatus, Integer>> transMetricsByLang =
                metrics.getTranslationStatusMetricsByLanguage();

        Map<String, ReviewStatusMetrics> reviewMetricsByLang =
                metrics.getReviewStatusMetricsByLanguage();
 
        Map<String, Map<String, Integer>> partnerMetricsByLang =
                metrics.getPartnerStatusMetricsByLanguage();

        // source language
        EnumMap<TranslationStatus, Integer> transMetrics = transMetricsByLang.get(source);
        assertNotNull("translation status metrics for " + source, transMetrics);
        checkTranslationStatusMetrics(transMetrics, numResources,
                EnumSet.of(TranslationStatus.SOURCE_LANGUAGE));

        ReviewStatusMetrics reviewMetrics = reviewMetricsByLang.get(source);
        assertNotNull("review status metrics for " + source, reviewMetrics);
        checkReviewStatusMetrics(reviewMetrics, numResources, -1);


        Map<String, Integer> partnerMetrics = partnerMetricsByLang.get(source);
        assertNotNull("partner status metrics for " + source, partnerMetrics);

        // target languages
        if (targets != null) {
            for (String target : targets) {
                transMetrics = transMetricsByLang.get(target);
                assertNotNull("translation status metrics for " + target, transMetrics);
                checkTranslationStatusMetrics(transMetrics, numResources,
                        EnumSet.of(TranslationStatus.TRANSLATED, TranslationStatus.IN_PROGRESS));

                reviewMetrics = reviewMetricsByLang.get(target);
                assertNotNull("review status metrics for " + target, reviewMetrics);
                checkReviewStatusMetrics(reviewMetrics, numResources, -1);

                partnerMetrics = partnerMetricsByLang.get(target);
                assertNotNull("partner status metrics for " + target, partnerMetrics);
            }
        }
    }

    /**
     * Check translation status metrics contents.
     * 
     * @param metrics           The input translation status metrics.
     * @param numResources      The total number of resources expected.
     * @param validStatusSet    If not null, check if all status values are one of
     *                          status included in the set.
     */
    private static void checkTranslationStatusMetrics(EnumMap<TranslationStatus, Integer> metrics,
            int numResources, EnumSet<TranslationStatus> validStatusSet) {
        int actualNumResources = 0;
        for (Entry<TranslationStatus, Integer> entry : metrics.entrySet()) {
            if (validStatusSet != null) {
                assertTrue("translation status should be on of " + validStatusSet,
                        validStatusSet.contains(entry.getKey()));
            }
            actualNumResources += entry.getValue().intValue();
        }
        assertEquals("number of translation status entries", numResources, actualNumResources);
    }

    /**
     * Check review status metrics contents.
     * 
     * @param metrics       The input review status metrics.
     * @param numResources  The total number of resources expected.
     * @param numReviewed   If 0 or positive, check if the number actually
     *                      matches the reviewed count.
     */
    private static void checkReviewStatusMetrics(ReviewStatusMetrics metrics,
            int numResources, int numReviewed) {
        assertEquals("number of review status entries", numResources,
                metrics.getReviewed() + metrics.getNotYetReviewed());
        if (numReviewed >= 0) {
            assertEquals("number of reviewed", numReviewed, metrics.getReviewed());
        }
    }
}
