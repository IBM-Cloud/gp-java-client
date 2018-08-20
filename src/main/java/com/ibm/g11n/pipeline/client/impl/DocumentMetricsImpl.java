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
package com.ibm.g11n.pipeline.client.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.ibm.g11n.pipeline.client.DocumentMetrics;
import com.ibm.g11n.pipeline.client.ReviewStatusMetrics;
import com.ibm.g11n.pipeline.client.TranslationStatus;

/**
 * DocumentMetrics implementation class.
 * 
 * @author Yoshito Umaoka
 */
public class DocumentMetricsImpl extends DocumentMetrics {
    private final Map<String, EnumMap<TranslationStatus, Integer>> tsByLang;
    private final Map<String, ReviewStatusMetrics> rsByLang;

    DocumentMetricsImpl(Map<String, EnumMap<TranslationStatus, Integer>> tsByLang,
            Map<String, ReviewStatusMetrics> rsByLang) {
        this.tsByLang = tsByLang;
        this.rsByLang = rsByLang;
    }

    @Override
    public Map<String, EnumMap<TranslationStatus, Integer>> getTranslationStatusMetricsByLanguage() {
        if (tsByLang == null) {
            return null;
        }
        return Collections.unmodifiableMap(tsByLang);
    }

    @Override
    public Map<String, ReviewStatusMetrics> getReviewStatusMetricsByLanguage() {
        if (rsByLang == null) {
            return null;
        }
        return Collections.unmodifiableMap(rsByLang);
    }
}
