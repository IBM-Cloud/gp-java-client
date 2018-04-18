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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <code>NewDocumentData</code> is used for specifying new document's
 * properties.
 * 
 * @author John Emmons
 */
public class NewDocumentData {
    private final String sourceLanguage;
    private Set<String> targetLanguages;
    private List<String> notes;
    private Map<String, String> metadata;

    /**
     * Constructor.
     * 
     * @param sourceLanguage    The source language of the new bundle, specified
     *                          by BCP 47 language tag. Must not be null.
     */
    public NewDocumentData(String sourceLanguage) {
        this.sourceLanguage = Objects.requireNonNull(sourceLanguage,
                "sourceLanguage must not be null");
    }

    /**
     * Returns the source language.
     * 
     * @return The source language.
     */
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Returns the set of target languages.
     * 
     * @return The set of target languages.
     */
    public Set<String> getTargetLanguages() {
        return targetLanguages;
    }

    /**
     * Sets the set of target languages specified by BCP 47 language tags.
     * 
     * @param targetLanguages   The set of target languages specified by BCP 47
     *                          language tags.
     * @return This object.
     */
    public NewDocumentData setTargetLanguages(Set<String> targetLanguages) {
        this.targetLanguages = targetLanguages;
        return this;
    }

    /**
     * Returns the notes for the new document.
     * 
     * @return The notes for the new document.
     */
    public List<String> getNotes() {
        return notes;
    }

    /**
     * Sets the notes for the new document.
     * 
     * @param notes The notes for the new document.
     * @return This object.
     */
    public NewDocumentData setNotes(List<String> notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Returns a map containing the key-value pairs.
     * 
     * @return A map containing the key-value pairs.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets arbitrary metadata associated with the new document specified
     * by a map containing string key-value pairs.
     * 
     * @param metadata  A map containing string key-value pairs.
     * @return This object.
     */
    public NewDocumentData setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}
