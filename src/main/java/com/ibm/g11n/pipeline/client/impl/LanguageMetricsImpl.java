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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.ibm.g11n.pipeline.client.LanguageMetrics;
import com.ibm.g11n.pipeline.client.ReviewStatusMetrics;
import com.ibm.g11n.pipeline.client.TranslationStatus;

/**
 * LanguageMetrics implementation class.
 * 
 * @author Yoshito Umaoka
 */
class LanguageMetricsImpl extends LanguageMetrics {
    private final EnumMap<TranslationStatus, Integer> ts;
    private final ReviewStatusMetrics rs;
    private final Map<String, Integer> ps;

    LanguageMetricsImpl(EnumMap<TranslationStatus, Integer> ts,
            ReviewStatusMetrics rs, Map<String, Integer> ps) {
        this.ts = ts;
        this.rs = rs;
        this.ps = ps;
    }

    @Override
    public EnumMap<TranslationStatus, Integer> getTranslationStatusMetrics() {
        // no unmodifiable wrapper...
        return ts;
    }

    @Override
    public ReviewStatusMetrics getReviewStatusMetrics() {
        return rs;
    }

    @Override
    public Map<String, Integer> getPartnerStatusMetrics() {
        if (ps == null) {
            return null;
        }
        return Collections.unmodifiableMap(ps);
    }
}
