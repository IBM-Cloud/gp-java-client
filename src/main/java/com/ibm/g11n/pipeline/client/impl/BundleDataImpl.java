/*  
 * Copyright IBM Corp. 2015, 2016
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.g11n.pipeline.client.BundleData;

/**
 * BandleData implementation class.
 * 
 * @author Yoshito Umaoka
 */
class BundleDataImpl extends BundleData {

    private final RestBundle bundle;

    BundleDataImpl(RestBundle bundle) {
        super(bundle.getSourceLanguage(), bundle.isReadOnly(),
                bundle.getUpdatedBy(), bundle.getUpdatedAt());
        this.bundle = bundle;
    }

    @Override
    public Set<String> getTargetLanguages() {
        Set<String> targetLanguages = bundle.getTargetLanguages();
        if (targetLanguages == null) {
            return null;
        }
        return Collections.unmodifiableSet(targetLanguages);
    }

    @Override
    public List<String> getNotes() {
        List<String> notes = bundle.getNotes();
        if (notes == null) {
            return null;
        }
        return Collections.unmodifiableList(notes);
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = bundle.getMetadata();
        if (metadata == null) {
            return null;
        }
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String getPartner() {
        return bundle.getPartner();
    }

    @Override
    public String getSegmentSeparatorPattern() {
        return bundle.getSegmentSeparatorPattern();
    }

    @Override
    public String getNoTranslationPattern() {
        return bundle.getNoTranslationPattern();
    }

    /**
     * Data object used for deserializing bundle data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestBundle extends RestObject {
        private String sourceLanguage;
        private Set<String> targetLanguages;
        private boolean readOnly;
        private List<String> notes;
        private Map<String, String> metadata;
        private String partner;
        private String segmentSeparatorPattern;
        private String noTranslationPattern;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestBundle() {
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public Set<String> getTargetLanguages() {
            return targetLanguages;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public List<String> getNotes() {
            return notes;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public String getPartner() {
            return partner;
        }

        public String getSegmentSeparatorPattern() {
            return segmentSeparatorPattern;
        }

        public String getNoTranslationPattern() {
            return noTranslationPattern;
        }
    }
}
