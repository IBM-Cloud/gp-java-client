/*  
 * Copyright IBM Corp. 2017, 2018
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>DocumentData</code> provides read access to the document's
 * properties.
 * 
 * @author John Emmons
 */
public abstract class DocumentData {
    private final String documentId;
    private final DocumentType type;
    private final String sourceLanguage;
    private final boolean readOnly;
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>DocumentData</code>.
     * 
     * @param documentId        The ID of the document
     * @param type              The type of document (HTML or MD)
     * @param sourceLanguage    The source language of this document, specified by BCP 47
     *                          language tag such as "en" for English.
     * @param readOnly          Specifies whether the document is read-only.
     * @param updatedBy         User name who last updated the document.
     * @param updatedAt         Time of the latest update to the document.
     * 
     */
    public DocumentData(String documentId, DocumentType type, String sourceLanguage, boolean readOnly, String updatedBy, Date updatedAt) {
        this.documentId = documentId;
        this.type = type;
        this.sourceLanguage = sourceLanguage;
        this.readOnly = readOnly;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Returns the source language of this document.
     * 
     * @return The source language of this document.
     */
    public final String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Returns the read only status of this document.
     * 
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns the user name of the latest update to the document.
     * 
     * @return the updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the time of the latest update to the document.
     * 
     * @return the updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }
    /**
     * Returns the set of target languages specified by BCP 47 language tags,
     * such as "de" for German and "zh-Hans" for Simplified Chinese.
     * 
     * @return The set of target languages.
     */
    public abstract Set<String> getTargetLanguages();

    /**
     * Returns the notes for this document.
     * 
     * @return The notes for this document.
     */
    public abstract List<String> getNotes();

    /**
     * Returns the arbitrary metadata represented by string key-value pairs.
     * 
     * @return The arbitrary metadata represented by string key-value pairs.
     */
    public abstract Map<String, String> getMetadata();

    /**
     * Returns the unique ID of the document.
     * 
     * @return the documentId
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Returns the document's type.
     * 
     * @return the type
     */
    public DocumentType getType() {
        return type;
    }

}
