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
import java.util.List;
import java.util.Map;

import com.ibm.g11n.pipeline.client.SegmentData;
import com.ibm.g11n.pipeline.client.TranslationStatus;

/**
 * ResoruceEntryData implementation class.
 * 
 * @author jugudanniesundar
 */
class SegmentDataImpl extends SegmentData {

    private final RestSegmentData restSegment;

    SegmentDataImpl(RestSegmentData restSegment) {
        super(restSegment.getValue(), restSegment.getSourceValue(),
                restSegment.getTranslationStatus(), restSegment.isReviewed(),
                restSegment.getUpdatedBy(), restSegment.getUpdatedAt());
        this.restSegment = restSegment;
    }

    @Override
    public List<String> getNotes() {
        List<String> notes = restSegment.getNotes();
        if (notes == null) {
            return null;
        }
        return Collections.unmodifiableList(notes);
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = restSegment.getMetadata();
        if (metadata == null) {
            return null;
        }
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String getPartnerStatus() {
        return restSegment.getPartnerStatus();
    }

    @Override
    public Integer getSequenceNumber() {
        return restSegment.getSequenceNumber();
    }

    /**
     * Data object used for deserializing resource entry data in JSON.
     * 
     * @author jugudanniesundar
     */
    static class RestSegmentData extends RestObject {
        private String segmentKey;
        private String value;
        private String sourceValue;
        private boolean reviewed;
        private TranslationStatus translationStatus;
        private List<String> notes;
        private Map<String, String> metadata;
        private String partnerStatus;
        private Integer sequenceNumber;

        public String getSegmentKey() {
            return segmentKey;
        }
        
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

        public List<String> getNotes() {
            return notes;
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
