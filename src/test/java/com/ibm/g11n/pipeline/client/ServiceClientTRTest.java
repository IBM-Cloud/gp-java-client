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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
    // TODO: Test case for submitting a TR without billing

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
 
        newTrData
            .setName(trName)
            .setOrganization(trOrg)
            .setEmails(trEmails);

        TranslationRequestData trData = client.createTranslationRequest(newTrData);
        assertNotNull("New TR", trData);

        String trId = trData.getId();
        assertNotNull("New TR ID", trId);
        assertEquals("New TR - name", trName, trData.getName());
        assertEquals("New TR - organization", trOrg, trData.getOrganization());
        assertEquals("New TR - emails", trEmails, trData.getEmails());
        assertEquals("New TR - partner", trPartner, trData.getPartner());   // implicitly set
        assertEquals("New TR - status", TranslationRequestStatus.DRAFT, trData.getStatus());

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
            .setSubmit(false);

        TranslationRequestData trDataU = client.updateTranslationRequest(trId, trChanges);

        assertEquals("TR(upd):" + trId + " - name", updTrName, trDataU.getName());
        assertEquals("TR(upd):" + trId + " - notes", trNotes, trDataU.getNotes());
        assertEquals("TR(upd):" + trId + " - domains", trDomains, trDataU.getDomains());

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
            assertTrue("TR:" + trId + " - XLIFF output", xliff.startsWith("<?xml version=\"1.0\"?>\n<xliff "));
        } catch (IOException e) {
            fail("IOException: " + e.getMessage());
        }

        // Delete bundle
        client.deleteTranslationRequest(trId);

        // Make sure the TR was deleted
        Map<String, TranslationRequestData> trs = client.getTranslationRequests();
        assertFalse("TR(del):" + trId, trs.containsKey(trId));

        cleanupBundles();
    }
}
