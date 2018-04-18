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
import java.util.Set;

/**
 * <code>DocumentDataChangeSet</code> is used for specifying changes
 * of a translatable document's properties.
 * 
 * @author John Emmons
 */
public class DocumentDataChangeSet {
    private Set<String> targetLanguages;
    private Boolean readOnly;
    private List<String> notes;
    private Map<String, String> metadata;

    /**
     * Constructor, creating an empty change set.
     */
    public DocumentDataChangeSet() {
    }

    /**
     * Returns the new set of target languages.
     * 
     * @return The new set of target languages.
     */
    public Set<String> getTargetLanguages() {
        return targetLanguages;
    }

    /**
     * Sets the new set of target languages specified by BCP 47
     * language tags.
     * 
     * @param targetLanguages The set of target languages.
     * @return This object.
     */
    public DocumentDataChangeSet setTargetLanguages(Set<String> targetLanguages) {
        this.targetLanguages = targetLanguages;
        return this;
    }

    /**
     * Returns the new read-only setting.
     * 
     * @return the new read-only setting.
     */
    public Boolean getReadOnly() {
        return readOnly;
    }

    /**
     * Sets <code>Boolean.TRUE</code> to make this document read only.
     * 
     * @param readOnly <code>Boolean.TRUE</code> to make this document read only.
     * @return This object.
     */
    public DocumentDataChangeSet setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * Returns the notes for this document.
     * 
     * @return The notes for this document.
     */
    public List<String> getNotes() {
        return notes;
    }

    /**
     * Sets the notes for this document.
     * 
     * @param notes The notes for this document.
     * @return This object.
     */
    public DocumentDataChangeSet setNotes(List<String> notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Returns a map containing the new or updated key-value pairs.
     * 
     * @return A map containing the new or updated key-value pairs.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets a map containing the new or updated key-value pairs.
     * <p>
     * When a key currently exists in document's metadata, the value
     * of the key will be replaced with the new value. When a key
     * currently exists and the new value is empty, the key-value
     * pair will be removed. When a key does not exist, the key-value
     * pair will be added.
     * 
     * @param metadata A map containing new or updated key-value pairs.
     * @return This object.
     */
    public DocumentDataChangeSet setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

}
