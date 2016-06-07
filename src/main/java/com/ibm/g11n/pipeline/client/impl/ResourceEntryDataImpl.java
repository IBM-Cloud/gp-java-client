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
import java.util.Map;

import com.ibm.g11n.pipeline.client.ResourceEntryData;
import com.ibm.g11n.pipeline.client.TranslationStatus;

/**
 * ResoruceEntryData implementation class.
 * 
 * @author Yoshito Umaoka
 */
public class ResourceEntryDataImpl extends ResourceEntryData {

    private final RestResourceEntry resourceEntry;

    ResourceEntryDataImpl(RestResourceEntry resourceEntry) {
        super(resourceEntry.getValue(), resourceEntry.getSourceValue(),
                resourceEntry.getTranslationStatus(), resourceEntry.isReviewed(),
                resourceEntry.getUpdatedBy(), resourceEntry.getUpdatedAt());
        this.resourceEntry = resourceEntry;
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = resourceEntry.getMetadata();
        if (metadata == null) {
            return null;
        }
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String getPartnerStatus() {
        return resourceEntry.getPartnerStatus();
    }

    @Override
    public Integer getSequenceNumber() {
        return resourceEntry.getSequenceNumber();
    }

    /**
     * Data object used for deserializing resource entry data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestResourceEntry extends RestObject {
        private String value;
        private String sourceValue;
        private boolean reviewed;
        private TranslationStatus translationStatus;
        private Map<String, String> metadata;
        private String partnerStatus;
        private Integer sequenceNumber;

        public String getValue() {
            return value;
        }

        public String getSourceValue() {
            return sourceValue;
        }

        public boolean isReviewed() {
            return reviewed;
        }

        public TranslationStatus getTranslationStatus() {
            return translationStatus;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public String getPartnerStatus() {
            return partnerStatus;
        }

        public Integer getSequenceNumber() {
            return sequenceNumber;
        }
    }
}
