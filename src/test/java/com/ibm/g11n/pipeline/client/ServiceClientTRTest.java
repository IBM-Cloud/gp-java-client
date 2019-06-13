/*  
 * Copyright IBM Corp. 2017
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Test cases for translation request APIs in ServiceClient.
 * 
 * @author yoshito_umaoka
 */
public class ServiceClientTRTest extends AbstractServiceClientBundleTest {

    private static final boolean TEST_TR_SUBMIT;
    static {
        String testSubmit = System.getProperty("TEST_TR_SUBMIT", "false");
        TEST_TR_SUBMIT = testSubmit.equalsIgnoreCase("true");
    }

    // Create / update and delete translation request
    @Test
    public void testTR() throws ServiceException {
        cleanupBundles();

        // Creates a new test bundle
        final String trTestBundleId = testBundleId("TR_Test");
        final String srcLang = "en";

        createBundleWithStrings(trTestBundleId, srcLang, "fr,de", null,
                new String[][] {{"key1", "New Bundle"}, {"key2", "Delete"}});

        // Creates a new translation request
        final String trTrgLang = "fr";
        Map<String, Set<String>> trgLangsByBundle = new HashMap<>();
        trgLangsByBundle.put(trTestBundleId, Collections.singleton(trTrgLang));
        NewTranslationRequestData newTrData = new NewTranslationRequestData(trgLangsByBundle);

        final String trName = "Test TR";
        final String trOrg = "Test Org";
        final List<String> trEmails = Collections.singletonList("gp-test@ibm.com");
        final String trPartner = "IBM";
        final int wcByLang = 3;
 
        final String trMetaKey1 = "mdk1";
        final String trMetaVal1 = "mdv1";
        final String trMetaKey2 = "mdk2";
        final String trMetaVal2 = "mdv2";
        final Map<String, String> trMetadata = new HashMap<>();
        trMetadata.put(trMetaKey1, trMetaVal1);
        trMetadata.put(trMetaKey2, trMetaVal2);

        final String trPartnerParamKey1 = "ppk1";
        final String trPartnerParamVal1 = "ppv1";
        final Map<String, String> trPartnerParameters = new HashMap<>();
        trPartnerParameters.put(trPartnerParamKey1, trPartnerParamVal1);

        newTrData
            .setName(trName)
            .setOrganization(trOrg)
            .setEmails(trEmails)
            .setMetadata(trMetadata)
            .setPartnerParameters(trPartnerParameters);

        TranslationRequestData trData = client.createTranslationRequest(newTrData);
        assertNotNull("New TR", trData);

        String trId = trData.getId();
        assertNotNull("New TR ID", trId);
        assertEquals("New TR - name", trName, trData.getName());
        assertEquals("New TR - organization", trOrg, trData.getOrganization());
        assertEquals("New TR - emails", trEmails, trData.getEmails());
        assertEquals("New TR - partner", trPartner, trData.getPartner());   // implicitly set
        assertEquals("New TR - status", TranslationRequestStatus.DRAFT, trData.getStatus());
        assertEquals("New TR - metadata", trMetadata, trData.getMetadata());
        assertEquals("New TR - partnerParameters", trPartnerParameters, trData.getPartnerParameters());

        Map<String, WordCountData> wcByBundle = trData.getWordCountData();
        assertNotNull("New TR - words by bundle", wcByBundle);
        WordCountData wcTestBundle = wcByBundle.get(trTestBundleId);
        assertNotNull("New TR - words in " + trTestBundleId, wcTestBundle);
        assertEquals("New TR - source language", srcLang, wcTestBundle.getSourceLanguage());
        Map<String, Integer> wordsByLang = wcTestBundle.getWordsByTargetLanguages();
        assertNotNull("New TR - words by lang in " + trTestBundleId, wcByBundle);
        Integer wordsTrgLang = wordsByLang.get(trTrgLang);
        assertNotNull("New TR - words for " + trTrgLang + " in " + trTestBundleId, wordsTrgLang);
        assertEquals("New TR - number of words " + trTrgLang + " in " + trTestBundleId, wcByLang, wordsTrgLang.intValue());


        // Gets the translation request created above
        TranslationRequestData trDataR = client.getTranslationRequest(trId);
        assertNotNull("TR:" + trId, trDataR);
        assertEquals("TR:" + trId + " - name", trName, trDataR.getName());

        // Updates the translation request
        TranslationRequestDataChangeSet trChanges = new TranslationRequestDataChangeSet();

        final String updTrName = "TR_Test_updated";
        final List<String> trNotes = Collections.singletonList("This is a test bundle.");
        final EnumSet<IndustryDomain> trDomains = EnumSet.of(IndustryDomain.INFTECH, IndustryDomain.EDUCATN);

        final String trMetaVal1Mod = "mdv1-mod";
        final String trMetaKey3 = "mdk3";
        final String trMetaVal3 = "mdv3";
        final Map<String, String> trMetadataDelta = new HashMap<>();
        trMetadataDelta.put(trMetaKey1, trMetaVal1Mod);
        trMetadataDelta.put(trMetaKey3, trMetaVal3);

        final Map<String, String> trMetadataExpected = new HashMap<>();
        trMetadataExpected.put(trMetaKey1, trMetaVal1Mod);
        trMetadataExpected.put(trMetaKey2, trMetaVal2);
        trMetadataExpected.put(trMetaKey3, trMetaVal3);


        final String trPartnerParamKey2 = "ppk2";
        final String trPartnerParamVal2 = "ppv2";
        final Map<String, String> trPartnerParametersDelta = new HashMap<>();
        trPartnerParametersDelta.put(trPartnerParamKey1, "");    // delete "ppk1"
        trPartnerParametersDelta.put(trPartnerParamKey2, trPartnerParamVal2);

        final Map<String, String> trPartnerParametersExpected = new HashMap<>();
        trPartnerParametersExpected.put(trPartnerParamKey2, trPartnerParamVal2);

        final String addTrgLang = "de";
        Map<String, Set<String>> updTrgLangsByBundle = new HashMap<>();
        Set<String> updTrgLangs = new HashSet<>();
        updTrgLangs.add(trTrgLang); // original language
        updTrgLangs.add(addTrgLang);
        updTrgLangsByBundle.put(trTestBundleId, updTrgLangs);

        trChanges
            .setName(updTrName)
            .setNotes(trNotes)
            .setDomains(trDomains)
            .setTargetLanguagesByBundle(updTrgLangsByBundle)
            .setMetadata(trMetadataDelta)
            .setPartnerParameters(trPartnerParametersDelta)
            .setSubmit(false);

        TranslationRequestData trDataU = client.updateTranslationRequest(trId, trChanges);

        assertEquals("TR(upd):" + trId + " - name", updTrName, trDataU.getName());
        assertEquals("TR(upd):" + trId + " - notes", trNotes, trDataU.getNotes());
        assertEquals("TR(upd):" + trId + " - domains", trDomains, trDataU.getDomains());
        assertEquals("TR(upd):" + trId + " - metadata", trMetadataExpected, trDataU.getMetadata());
        assertEquals("TR(upd):" + trId + " - partnerParameters", trPartnerParametersExpected, trDataU.getPartnerParameters());

        // Unchanged
        assertEquals("TR(upd):" + trId + " - organization", trOrg, trDataU.getOrganization());
        assertEquals("TR(upd):" + trId + " - emails", trEmails, trDataU.getEmails());
        assertEquals("TR(upd):" + trId + " - partner", trPartner, trDataU.getPartner());
        assertEquals("TR(upd):" + trId + " - status", TranslationRequestStatus.DRAFT, trDataU.getStatus());

        // Word count should reflect the updated target languages
        Map<String, WordCountData> wcByBundleU = trDataU.getWordCountData();
        assertNotNull("TR(upd):" + trId + " - words by bundle", wcByBundle);
        WordCountData wcTestBundleU = wcByBundleU.get(trTestBundleId);
        assertNotNull("TR(upd):" + trId + " - words in " + trTestBundleId, wcTestBundleU);
        assertEquals("TR(upd):" + trId + " - source language", srcLang, wcTestBundleU.getSourceLanguage());
        Map<String, Integer> wordsByLangU = wcTestBundleU.getWordsByTargetLanguages();
        assertNotNull("TR(upd):" + trId + " - words by lang in " + trTestBundleId, wcByBundleU);

        Integer wordsTrgLangU1 = wordsByLangU.get(trTrgLang);
        assertNotNull("TR(upd):" + trId + " - words for " + trTrgLang + " in " + trTestBundleId, wordsTrgLangU1);
        assertEquals("TR(upd):" + trId + " - number of words " + trTrgLang + " in " + trTestBundleId, wcByLang, wordsTrgLangU1.intValue());
        Integer wordsTrgLangU2 = wordsByLangU.get(addTrgLang);
        assertNotNull("TR(upd):" + trId + " - words for " + addTrgLang + " in " + trTestBundleId, wordsTrgLangU2);
        assertEquals("TR(upd):" + trId + " - number of words " + addTrgLang + " in " + trTestBundleId, wcByLang, wordsTrgLangU2.intValue());

        // Export XLIFF
        ByteArrayOutputStream outputXliff = new ByteArrayOutputStream();
        try {
            client.getXliffFromTranslationRequest(trId, srcLang, trTrgLang, outputXliff);

            // Following code does not really check the contents
            byte[] xliffBytes = outputXliff.toByteArray();
            String xliff = new String(xliffBytes, StandardCharsets.UTF_8);
            System.out.println(xliff);
            assertTrue("TR:" + trId + " - XLIFF output", xliff.startsWith("<?xml version=\"1.0\"?>\n<xliff "));
        } catch (IOException e) {
            fail("IOException: " + e.getMessage());
        }

        // Delete TR
        client.deleteTranslationRequest(trId);

        // Make sure the TR was deleted
        Map<String, TranslationRequestData> trs = client.getTranslationRequests();
        assertFalse("TR(del):" + trId, trs.containsKey(trId));

        cleanupBundles();
    }

    @Test
    public void testTRSubmit() throws ServiceException {
        if (!TEST_TR_SUBMIT) {
            // Skip this if env var TEST_SUBMIT is not true
            return;
        }

        // Creates a new test bundle
        final String trTestBundleId = testBundleId("TR_TestSubmit");
        final String srcLang = "en";

        createBundleWithStrings(trTestBundleId, srcLang, "fr,de", null,
                new String[][] {{"key1", "Hello"}, {"key2", "Good bye"}});

        // Creates a new translation request
        final String trTrgLang = "de";
        Map<String, Set<String>> trgLangsByBundle = new HashMap<>();
        trgLangsByBundle.put(trTestBundleId, Collections.singleton(trTrgLang));
        NewTranslationRequestData newTrData = new NewTranslationRequestData(trgLangsByBundle);

        final String trName = "Test TR Submit";
        final String trOrg = "Test Org";
        final List<String> trEmails = Collections.singletonList("gp-test@ibm.com");
        final String trPartner = "IBM";
 
        final String metaKeyPhase = "phase";
        final String metaVal1 = "1";
        final Map<String, String> trMetadata = new HashMap<>();
        trMetadata.put(metaKeyPhase, metaVal1);

        newTrData
            .setPartner(trPartner)
            .setName(trName)
            .setOrganization(trOrg)
            .setEmails(trEmails)
            .setMetadata(trMetadata)
            .setSubmit(true);

        TranslationRequestData trData = client.createTranslationRequest(newTrData);
        String trId = trData.getId();

        assertNotEquals("Status of TR should not be DRAFT", TranslationRequestStatus.DRAFT, trData.getStatus());

        // Update metadata
        TranslationRequestDataChangeSet trChanges = new TranslationRequestDataChangeSet();

        final String metaVal2 = "2";
        final Map<String, String> updMetadata = new HashMap<>();
        updMetadata.put(metaKeyPhase, metaVal2);

        trChanges.setMetadata(updMetadata);

        TranslationRequestData updData = client.updateTranslationRequest(trId, trChanges);

        Map<String, String> modMetadata = updData.getMetadata();
        assertNotNull("TR metadata", modMetadata);
        String metaVal = modMetadata.get(metaKeyPhase);
        assertEquals("Updated metadata 'phase'", metaVal2, metaVal);

        // TODO: Cleanup - cannot delete a TR immediately
        // client.deleteTranslationRequest(trId);

        cleanupBundles();
    }
    
    @Test
    public void testTRSubmitAsync() throws ServiceException {
        if (!TEST_TR_SUBMIT) {
            // Skip this if env var TEST_SUBMIT is not true
            return;
        }

        // Creates a new test bundle
        final String trTestBundleId = testBundleId("TR_TestSubmitAsync1");
        final String srcLang = "en";

        createBundleWithStrings(trTestBundleId, srcLang, "fr,de", null,
                new String[][] {{"key1", "Hello"}, {"key2", "Good bye"}});

        // Creates a new translation request
        final String trTrgLang = "de";
        Map<String, Set<String>> trgLangsByBundle = new HashMap<>();
        trgLangsByBundle.put(trTestBundleId, Collections.singleton(trTrgLang));
        NewTranslationRequestData newTrData = new NewTranslationRequestData(trgLangsByBundle);

        final String trName = "Test TR Submit Async";
        final String trOrg = "Test Org";
        final List<String> trEmails = Collections.singletonList("gp-test@ibm.com");
        final String trPartner = "IBM";
 
        final String metaKeyPhase = "phase";
        final String metaVal1 = "1";
        final Map<String, String> trMetadata = new HashMap<>();
        trMetadata.put(metaKeyPhase, metaVal1);

        newTrData
            .setPartner(trPartner)
            .setName(trName)
            .setOrganization(trOrg)
            .setEmails(trEmails)
            .setMetadata(trMetadata)
            .setSubmit(true);

        // create translation request asynchronously
        TranslationRequestData trData = client.createTranslationRequest(newTrData, true);
        String trId = trData.getId();
        assertNotEquals("Status of TR should not be DRAFT", TranslationRequestStatus.DRAFT, trData.getStatus());
        // assert that word count data should be empty when TR is submitted asynchronously
        assertTrue("Word count map should null/empty because TR is submitted asyncrhonously", trData.getWordCountData() == null || trData.getWordCountData().isEmpty());

        
        // Creates second test bundle
        final String trTestBundleId2 = testBundleId("TR_TestSubmitAsync2");
        createBundleWithStrings(trTestBundleId2, srcLang, "fr,de", null,
                new String[][] {{"key1", "Hello"}, {"key2", "Good bye"}});

        // Creates a new translation request (DRAFT then SUBMITTED)
        trgLangsByBundle = new HashMap<>();
        trgLangsByBundle.put(trTestBundleId2, Collections.singleton(trTrgLang));
        NewTranslationRequestData newTrData2 = new NewTranslationRequestData(trgLangsByBundle);

        final String trName2 = "Test TR Submit Async2";

        newTrData2
            .setPartner(trPartner)
            .setName(trName2)
            .setOrganization(trOrg)
            .setEmails(trEmails)
            .setSubmit(false);
        
        // create DRAFT translation request
        TranslationRequestData trData2 = client.createTranslationRequest(newTrData, true);
        assertTrue("Status should be equal to DRAFT", TranslationRequestStatus.DRAFT == trData2.getStatus());
        String trId2 = trData2.getId();
        
        // Update status to SUBMITTED asynchronously
        TranslationRequestDataChangeSet trChanges = new TranslationRequestDataChangeSet();
        trChanges.setSubmit(true);
        TranslationRequestData updData = client.updateTranslationRequest(trId2, trChanges, true);
        assertTrue("Status should be equal to SUBMITTED", TranslationRequestStatus.SUBMITTED == updData.getStatus());

        cleanupBundles();
    }
    
    @Test
    public void testListDraftTRs() throws ServiceException {
        // Test list performance of Draft TRs with huge word count using summary field
        
        // Creates a new test bundle
        final String trTestBundleId = testBundleId("TR_TestDraft");
        final String srcLang = "en";

        createBundleWithStrings(trTestBundleId, srcLang, "fr,de", null,
                new String[][] {
                {"key1", "This is a test for listing draft TRs."}, 
                {"key2", "This test checks whether the list of TRs is populated quickly."},
                {"key2", "When a large number of draft TRs are created, then we do not process wordcount if summary field is true."}
                });

        // Creates a new translation request
        final String trTrgLang = "de";
        Map<String, Set<String>> trgLangsByBundle = new HashMap<>();
        trgLangsByBundle.put(trTestBundleId, Collections.singleton(trTrgLang));
        NewTranslationRequestData newTrData = new NewTranslationRequestData(trgLangsByBundle);

        final String trName = "Test TR Submit Async";
        final String trOrg = "Test Org";
        final List<String> trEmails = Collections.singletonList("gp-test@ibm.com");
        final String trPartner = "IBM";
 
        final String metaKeyPhase = "phase";
        final String metaVal1 = "1";
        final Map<String, String> trMetadata = new HashMap<>();
        trMetadata.put(metaKeyPhase, metaVal1);

        newTrData
            .setPartner(trPartner)
            .setName(trName)
            .setOrganization(trOrg)
            .setEmails(trEmails)
            .setMetadata(trMetadata)
            .setSubmit(false);
        
        Set<String> draftTRIdSet = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            // create draft translation request asynchronously
            TranslationRequestData trData = client.createTranslationRequest(newTrData, true);
            draftTRIdSet.add(trData.getId());
        }
        
        // check performance of summary call
        Date timeStart = new Date();
        Map<String, TranslationRequestData> summaryDraftTRMap = client.getTranslationRequests(true);
        long durationWithSummary = new Date().getTime() - timeStart.getTime();
        Set<String> summaryDraftTRIdSet = summaryDraftTRMap.keySet();
        assertTrue("All the draft TRs (searched with summary view) should be listed", summaryDraftTRIdSet.containsAll(draftTRIdSet));
        
        
        // check performance of default call without summary field being set to true
        timeStart = new Date();
        summaryDraftTRMap = client.getTranslationRequests(false);
        long durationWithoutSummary = new Date().getTime() - timeStart.getTime();
        Set<String> withoutSummaryDraftTRIdSet = summaryDraftTRMap.keySet();
        assertTrue("All the draft TRs should be listed", 
                withoutSummaryDraftTRIdSet.containsAll(draftTRIdSet));
        assertTrue("The listing of draft TRs with summary took:" + durationWithSummary +
                " milliseconds, while list performance of draft TRs without summary took: " + durationWithoutSummary + " milliseconds",
                durationWithoutSummary >= durationWithSummary);
        
        cleanupBundles();
    }
}
