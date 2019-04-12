/*  
 * Copyright IBM Corp. 2018
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
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jugudanniesundar
 *
 */
public class ServiceClientDocumentTRTest extends AbstractServiceClientDocumentTest  {

    @BeforeClass
    public static void setup() {
        assumeTrue("Doc API testing is enabled","true".equalsIgnoreCase(System.getProperty("DOC_TEST_ENABLED", "false")));
    }
    
    // TODO: Test case for submitting a TR without billing
    
    private final String testFolder = "src/test/resources/com/ibm/g11n/pipeline/client";
    private final File htmlFile = new File(testFolder + "/html_doc1.html");
    private final File mdFile = new File(testFolder + "/md_doc1.md");
    
    // Create / update and delete translation request
    @Test
    public void testTR() throws ServiceException {
        cleanupDocuments();

        // Creates a new test document
        final String trTestHtmlDocumentId = testDocumentId("TR_Test_html");
        final String trTestMdDocumentId = testDocumentId("TR_Test_md");
        final String srcLang = "en";

        createDocumentWithContent(trTestHtmlDocumentId, DocumentType.HTML, srcLang, "fr,de", null, htmlFile);
        createDocumentWithContent(trTestMdDocumentId, DocumentType.MD, srcLang, "fr,de", null, mdFile);

        // Check metrics
        checkDocMetrics(DocumentType.HTML, trTestHtmlDocumentId, srcLang);
        checkDocMetrics(DocumentType.MD, trTestMdDocumentId, srcLang);

        // Creates a new translation request
        final String trTrgLang = "fr";
        Map<String, Map<String, Set<String>>> trgLangsMap = new HashMap<>();
        Map<String, Set<String>> trgLangByHtmlDoc = new HashMap<>();
        trgLangByHtmlDoc.put(trTestHtmlDocumentId, Collections.singleton(trTrgLang));
        trgLangsMap.put(DocumentType.HTML.toString(), trgLangByHtmlDoc);
        Map<String, Set<String>> trgLangByMdDoc = new HashMap<>();
        trgLangByMdDoc.put(trTestMdDocumentId, Collections.singleton(trTrgLang));
        trgLangsMap.put(DocumentType.MD.toString(), trgLangByMdDoc);
        
        NewDocumentTranslationRequestData newTrData = new NewDocumentTranslationRequestData(trgLangsMap);

        final String trName = "Test TR";
        final String trOrg = "Test Org";
        final List<String> trEmails = Collections.singletonList("gp-test@ibm.com");
        final String trPartner = "IBM";
        final int wcByLangForHtml = 47;
        final int wcByLangForMd = 31;
 
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

        DocumentTranslationRequestData trData = client.createDocumentTranslationRequest(newTrData);
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

        Map<String, Map<String, WordCountData>> wcByType = trData.getWordCountData();
        assertNotNull("New TR - words by type", wcByType);
        Map<String, WordCountData> wcByHtmlDocument = wcByType.get(DocumentType.HTML.toString());
        assertNotNull("New TR - words by html document", wcByHtmlDocument);
        WordCountData wcTestHtmlDocument = wcByHtmlDocument.get(trTestHtmlDocumentId);
        assertNotNull("New TR - words in html document: " + trTestHtmlDocumentId, wcTestHtmlDocument);
        assertEquals("New TR - source language (html)", srcLang, wcTestHtmlDocument.getSourceLanguage());
        Map<String, Integer> wordsByLang = wcTestHtmlDocument.getWordsByTargetLanguages();
        assertNotNull("New TR - words by lang in html " + trTestHtmlDocumentId, wordsByLang);
        Integer wordsTrgLang = wordsByLang.get(trTrgLang);
        assertNotNull("New TR - words for " + trTrgLang + " in html " + trTestHtmlDocumentId, wordsTrgLang);
        assertEquals("New TR - number of words " + trTrgLang + " in html " + trTestHtmlDocumentId, wcByLangForHtml, wordsTrgLang.intValue());
        
        
        Map<String, WordCountData> wcByMdDocument = wcByType.get(DocumentType.MD.toString());
        assertNotNull("New TR - words by md document", wcByMdDocument);
        WordCountData wcTestMdDocument = wcByMdDocument.get(trTestMdDocumentId);
        assertNotNull("New TR - words in md document: " + trTestMdDocumentId, wcTestMdDocument);
        assertEquals("New TR - source language (md)", srcLang, wcTestMdDocument.getSourceLanguage());
        wordsByLang = wcTestMdDocument.getWordsByTargetLanguages();
        assertNotNull("New TR - words by lang in md " + trTestMdDocumentId, wordsByLang);
        wordsTrgLang = wordsByLang.get(trTrgLang);
        assertNotNull("New TR - words for " + trTrgLang + " in md " + trTestMdDocumentId, wordsTrgLang);
        assertEquals("New TR - number of words " + trTrgLang + " in md " + trTestMdDocumentId, wcByLangForMd, wordsTrgLang.intValue());

        // Gets the translation request created above
        DocumentTranslationRequestData trDataR = client.getDocumentTranslationRequest(trId);
        assertNotNull("TR:" + trId, trDataR);
        assertEquals("TR:" + trId + " - name", trName, trDataR.getName());

        // Updates the translation request
        DocumentTranslationRequestDataChangeSet trChanges = new DocumentTranslationRequestDataChangeSet();

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
        Map<String, Map<String, Set<String>>> updTrgLangsMap = new HashMap<>();
        Set<String> updTrgLangs = new HashSet<>();
        updTrgLangs.add(trTrgLang); // original language
        updTrgLangs.add(addTrgLang);
        Map<String, Set<String>> updTrgLangsByHtml = new HashMap<>();
        updTrgLangsByHtml.put(trTestHtmlDocumentId, updTrgLangs);
        Map<String, Set<String>> updTrgLangsByMd = new HashMap<>();
        updTrgLangsByMd.put(trTestMdDocumentId, updTrgLangs);
        updTrgLangsMap.put(DocumentType.HTML.toString(), updTrgLangsByHtml);
        updTrgLangsMap.put(DocumentType.MD.toString(), updTrgLangsByMd);

        trChanges
            .setName(updTrName)
            .setNotes(trNotes)
            .setDomains(trDomains)
            .setTargetLanguagesMap(updTrgLangsMap)
            .setMetadata(trMetadataDelta)
            .setPartnerParameters(trPartnerParametersDelta)
            .setSubmit(false);

        DocumentTranslationRequestData trDataU = client.updateDocumentTranslationRequest(trId, trChanges);

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
        Map<String, Map<String, WordCountData>> wcByTypeU = trDataU.getWordCountData();
        assertNotNull("TR(upd):" + trId + " - words by type", wcByTypeU);
        wcByHtmlDocument = wcByTypeU.get(DocumentType.HTML.toString());
        assertNotNull("TR(upd):" + trId + " - words by html", wcByHtmlDocument);
        
        wcByMdDocument = wcByTypeU.get(DocumentType.MD.toString());
        assertNotNull("TR(upd):" + trId + " - words by md", wcByMdDocument);
        
        wcTestHtmlDocument = wcByHtmlDocument.get(trTestHtmlDocumentId);
        assertNotNull("TR(upd):" + trId + " - words in html " + trTestHtmlDocumentId, wcTestHtmlDocument);
        assertEquals("TR(upd):" + trId + " - source language", srcLang, wcTestHtmlDocument.getSourceLanguage());
        Map<String, Integer> wordsByLangU = wcTestHtmlDocument.getWordsByTargetLanguages();
        assertNotNull("TR(upd):" + trId + " - words by lang in html " + trTestHtmlDocumentId, wordsByLangU);
        Integer wordsTrgLangU1 = wordsByLangU.get(trTrgLang);
        assertNotNull("TR(upd):" + trId + " - words for " + trTrgLang + " in html" + trTestHtmlDocumentId, wordsTrgLangU1);
        assertEquals("TR(upd):" + trId + " - number of words " + trTrgLang + " in html" + trTestHtmlDocumentId, wcByLangForHtml, wordsTrgLangU1.intValue());
        Integer wordsTrgLangU2 = wordsByLangU.get(addTrgLang);
        assertNotNull("TR(upd):" + trId + " - words for " + addTrgLang + " in " + trTestHtmlDocumentId, wordsTrgLangU2);
        assertEquals("TR(upd):" + trId + " - number of words " + addTrgLang + " in " + trTestHtmlDocumentId, wcByLangForHtml, wordsTrgLangU2.intValue());
        
        wcTestMdDocument = wcByMdDocument.get(trTestMdDocumentId);
        assertNotNull("TR(upd):" + trId + " - words in html " + trTestMdDocumentId, wcTestHtmlDocument);
        assertEquals("TR(upd):" + trId + " - source language", srcLang, wcTestHtmlDocument.getSourceLanguage());
        wordsByLangU = wcTestMdDocument.getWordsByTargetLanguages();
        assertNotNull("TR(upd):" + trId + " - words by lang in html " + trTestMdDocumentId, wordsByLangU);
        wordsTrgLangU1 = wordsByLangU.get(trTrgLang);
        assertNotNull("TR(upd):" + trId + " - words for " + trTrgLang + " in html" + trTestMdDocumentId, wordsTrgLangU1);
        assertEquals("TR(upd):" + trId + " - number of words " + trTrgLang + " in md" + trTestMdDocumentId, wcByLangForMd, wordsTrgLangU1.intValue());
        wordsTrgLangU2 = wordsByLangU.get(addTrgLang);
        assertNotNull("TR(upd):" + trId + " - words for " + addTrgLang + " in " + trTestMdDocumentId, wordsTrgLangU2);
        assertEquals("TR(upd):" + trId + " - number of words " + addTrgLang + " in " + trTestMdDocumentId, wcByLangForMd, wordsTrgLangU2.intValue());

        // Export XLIFF
        ByteArrayOutputStream outputXliff = new ByteArrayOutputStream();
        try {
            client.getXliffFromDocumentTranslationRequest(trId, srcLang, trTrgLang, outputXliff);

            // Following code does not really check the contents
            byte[] xliffBytes = outputXliff.toByteArray();
            String xliff = new String(xliffBytes, StandardCharsets.UTF_8);
            System.out.println(xliff);
            assertTrue("TR:" + trId + " - XLIFF output", xliff.startsWith("<?xml version=\"1.0\"?>\n<xliff "));
        } catch (IOException e) {
            fail("IOException: " + e.getMessage());
        }

        // Delete document
        client.deleteDocumentTranslationRequest(trId);

        // Make sure the TR was deleted
        Map<String, DocumentTranslationRequestData> trs = client.getDocumentTranslationRequests();
        assertFalse("TR(del):" + trId, trs.containsKey(trId));

        cleanupDocuments();
    }

    private void checkDocMetrics(DocumentType type, String docId, String srcLang) throws ServiceException {
        DocumentMetrics metrics = client.getDocumentMetrics(type, docId);

        Map<String, EnumMap<TranslationStatus, Integer>> trstsMap = metrics.getTranslationStatusMetricsByLanguage();
        EnumMap<TranslationStatus, Integer> srcTrsts = trstsMap.get(srcLang);
        assertNotNull("Source translation status map for " + docId, srcTrsts);
        int srcSegCount = 0;
        for (Integer count : srcTrsts.values()) {
            srcSegCount += count.intValue();
        }

        // Make sure total segment counts are same in target languages
        for (Entry<String, EnumMap<TranslationStatus, Integer>> trstsEntry : trstsMap.entrySet()) {
            if (trstsEntry.getKey().equals(srcLang)) {
                continue;
            }
            int tgtSegCount = 0;
            for (Integer count : trstsEntry.getValue().values()) {
                tgtSegCount += count.intValue();
            }
            assertEquals("Translation status segment count for " + docId + " in language " + trstsEntry.getKey(), srcSegCount, tgtSegCount);
        }

        Map<String, ReviewStatusMetrics> revstsMap = metrics.getReviewStatusMetricsByLanguage();
        for (Entry<String, ReviewStatusMetrics> revstsEntry : revstsMap.entrySet()) {
            ReviewStatusMetrics revMetrics = revstsEntry.getValue();
            assertEquals("Review status segment count for " + docId + " in language " + revstsEntry.getKey(),
                    srcSegCount, revMetrics.getReviewed() + revMetrics.getNotYetReviewed());
        }
    }
}
