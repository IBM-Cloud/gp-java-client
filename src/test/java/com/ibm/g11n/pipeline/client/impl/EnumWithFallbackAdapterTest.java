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
package com.ibm.g11n.pipeline.client.impl;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.junit.Test;

import com.ibm.g11n.pipeline.client.TranslationRequestStatus;
import com.ibm.g11n.pipeline.client.TranslationStatus;
import com.ibm.g11n.pipeline.client.impl.ServiceClientImpl.EnumWithFallbackAdapter;

/**
 * @author yoshito_umaoka
 *
 */
public class EnumWithFallbackAdapterTest {
    @Test
    public void testTranslationStatus() {
        final EnumWithFallbackAdapter<TranslationStatus> adapter =
                new EnumWithFallbackAdapter<>(TranslationStatus.UNKNOWN);

        final LinkedHashMap<String, TranslationStatus> testcaseMap = new LinkedHashMap<>();
        testcaseMap.put("SOURCE_LANGUAGE", TranslationStatus.SOURCE_LANGUAGE);
        testcaseMap.put("IN_PROGRESS", TranslationStatus.IN_PROGRESS);
        testcaseMap.put("TRANSLATED", TranslationStatus.TRANSLATED);
        testcaseMap.put("UNCONFIGURED", TranslationStatus.UNCONFIGURED);
        testcaseMap.put("FAILED", TranslationStatus.FAILED);
        testcaseMap.put("UNKNOWN", TranslationStatus.UNKNOWN);

        testcaseMap.put("In_Progress", TranslationStatus.IN_PROGRESS);
        testcaseMap.put("translated", TranslationStatus.TRANSLATED);

        // fallback
        testcaseMap.put("UNCONFIGURED status", TranslationStatus.UNKNOWN);
        testcaseMap.put("In Progress", TranslationStatus.UNKNOWN);
        testcaseMap.put("UNKNOWN_REALLY", TranslationStatus.UNKNOWN);
        testcaseMap.put("", TranslationStatus.UNKNOWN);

        for (Entry<String, TranslationStatus> testcase : testcaseMap.entrySet()) {
            assertEquals(testcase.getValue(), adapter.toEnumWithFallback(testcase.getKey()));
        }
    }

    @Test
    public void testTranslationRequestStatus() {
        final EnumWithFallbackAdapter<TranslationRequestStatus> adapter =
                new EnumWithFallbackAdapter<>(TranslationRequestStatus.UNKNOWN);

        final LinkedHashMap<String, TranslationRequestStatus> testcaseMap = new LinkedHashMap<>();
        testcaseMap.put("DRAFT", TranslationRequestStatus.DRAFT);
        testcaseMap.put("SUBMITTED", TranslationRequestStatus.SUBMITTED);
        testcaseMap.put("STARTED", TranslationRequestStatus.STARTED);
        testcaseMap.put("TRANSLATED", TranslationRequestStatus.TRANSLATED);
        testcaseMap.put("MERGED", TranslationRequestStatus.MERGED);
        testcaseMap.put("CANCELLED", TranslationRequestStatus.CANCELLED);
        testcaseMap.put("UNKNOWN", TranslationRequestStatus.UNKNOWN);

        testcaseMap.put("Draft", TranslationRequestStatus.DRAFT);
        testcaseMap.put("submitted", TranslationRequestStatus.SUBMITTED);

        // fallback
        testcaseMap.put("DRAFT created", TranslationRequestStatus.UNKNOWN);
        testcaseMap.put("UNKNOWN_REALLY", TranslationRequestStatus.UNKNOWN);
        testcaseMap.put("", TranslationRequestStatus.UNKNOWN);

        for (Entry<String, TranslationRequestStatus> testcase : testcaseMap.entrySet()) {
            assertEquals(testcase.getValue(), adapter.toEnumWithFallback(testcase.getKey()));
        }
    }
}
