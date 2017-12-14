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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.g11n.pipeline.client.DocumentData;
import com.ibm.g11n.pipeline.client.DocumentType;

/**
 * DocumentData implementation class.
 * 
 * @author John Emmons
 */
class DocumentDataImpl extends DocumentData {

    private final RestDocument document;

    DocumentDataImpl(RestDocument document) {
        super(document.getDocumentId(), document.getType(), document.getSourceLanguage(), document.isReadOnly(),
                document.getUpdatedBy(), document.getUpdatedAt());
        this.document = document;
    }

    @Override
    public Set<String> getTargetLanguages() {
        Set<String> targetLanguages = document.getTargetLanguages();
        if (targetLanguages == null) {
            return null;
        }
        return Collections.unmodifiableSet(targetLanguages);
    }

    @Override
    public List<String> getNotes() {
        List<String> notes = document.getNotes();
        if (notes == null) {
            return null;
        }
        return Collections.unmodifiableList(notes);
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = document.getMetadata();
        if (metadata == null) {
            return null;
        }
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Data object used for deserializing bundle data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestDocument extends RestObject {
        private String documentId;
        private DocumentType type;
        private String sourceLanguage;
        private Set<String> targetLanguages;
        private boolean readOnly;
        private List<String> notes;
        private Map<String, String> metadata;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestDocument() {
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

        /**
         * @return the documentId
         */
        public String getDocumentId() {
            return documentId;
        }

        /**
         * @param documentId the documentId to set
         */
        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        /**
         * @return the type
         */
        public DocumentType getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(DocumentType type) {
            this.type = type;
        }
    }
}
