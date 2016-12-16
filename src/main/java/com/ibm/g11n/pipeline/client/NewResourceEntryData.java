/*  
 * Copyright IBM Corp. 2016
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

/**
 * <code>NewResourceEntryData</code> is used for specifying a new
 * resource entry.
 * 
 * @author Yoshito Umaoka
 */
public class NewResourceEntryData {
    private final String value;
    private Boolean reviewed;
    private List<String> notes;
    private Map<String, String> metadata;
    private String partnerStatus;
    private Integer sequenceNumber;

    /**
     * Constructor.
     * 
     * @param value The resource string value. Must not be null.
     */
    public NewResourceEntryData(String value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Returns the new string resource value.
     * 
     * @return The new string resource value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the new Boolean review status.
     * 
     * @return The Boolean review status.
     */
    public Boolean getReviewed() {
        return reviewed;
    }

    /**
     * Sets the new Boolean review status.
     * 
     * @param reviewed  The Boolean review status.
     * @return This object.
     */
    public NewResourceEntryData setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
        return this;
    }

    /**
     * Returns the notes for the new resource entry.
     * 
     * @return The notes for the new resource entry.
     */
    public List<String> getNotes() {
        return notes;
    }

    /**
     * Sets the notes for the new resource entry.
     * 
     * @param notes The notes for the new resource entry.
     * @return This object.
     */
    public NewResourceEntryData setNote(List<String> notes) {
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
     * When a key currently exists in resource entry's metadata, the value
     * of the key will be replaced with the new value. When a key
     * currently exists and the new value is empty, the key-value
     * pair will be removed. When a key does not exists, the key-value
     * pair will be added.
     * 
     * @param metadata A map containing new or updated key-value pairs.
     * @return This object.
     */
    public NewResourceEntryData setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the new partner status string.
     * 
     * @return The new partner status string.
     */
    public String getPartnerStatus() {
        return partnerStatus;
    }

    /**
     * Sets the new partner status string.
     * <p>
     * This property is reserved for a translation partner's use.
     * If you don't use any translation partners, you can maintain
     * custom translation status string with this property.
     * 
     * @param partnerStatus The new partner status string.
     * @return This object.
     */
    public NewResourceEntryData setPartnerStatus(String partnerStatus) {
        this.partnerStatus = partnerStatus;
        return this;
    }

    /**
     * Returns the sequence number.
     * 
     * @return The sequence number.
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number.
     * <p>
     * The sequence number is an optional property used for specifying
     * an sorting order of this resource entry.
     * 
     * @param sequenceNumber    The sequence number.
     * @return This object.
     */
    public NewResourceEntryData setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }
}
