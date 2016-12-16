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
package com.ibm.g11n.pipeline.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <code>ResourceEntryData</code> provides read access to the resource entry's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public abstract class ResourceEntryData {
    private final String value;
    private final String sourceValue;
    private final TranslationStatus translationStatus;
    private final boolean reviewed;
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>ResourceEntryData</code>
     * 
     * @param value             The resource string value.
     * @param sourceValue       The resource string value in the source language.
     * @param translationStatus The translation status.
     * @param reviewed          If this string value marked as reviewed.
     * @param updatedBy         The last user updated this resource entry.
     * @param updatedAt         The last date when this resource entry was updated.
     */
    protected ResourceEntryData(String value, String sourceValue,
            TranslationStatus translationStatus,
            boolean reviewed, String updatedBy, Date updatedAt) {
        this.value = value;
        this.sourceValue = sourceValue;
        this.translationStatus = translationStatus;
        this.reviewed = reviewed;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the resource string value. If this entry is for a translation
     * target language, then this is the translation result.
     * 
     * @return The resource string value.
     */
    public final String getValue() {
        return value;
    }

    /**
     * Returns the resource string value in the source language.
     * 
     * @return The resource string value in the source language.
     */
    public final String getSourceValue() {
        return sourceValue;
    }

    /**
     * Returns the current translation status.
     * 
     * @return The current translation status.
     * @see TranslationStatus
     */
    public final TranslationStatus getTranslationStatus() {
        return translationStatus;
    }

    /**
     * Returns <code>true</code> if this entry is marked as reviewed.
     * 
     * @return <code>true</code> if this entry is marked as reviewed.
     */
    public final boolean isReviewed() {
        return reviewed;
    }

    /**
     * Returns the last user updated this resource entry.
     * 
     * @return The last user updated this resource entry.
     */
    public final String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the last date when this resource entry was updated.
     * 
     * @return The last date when this resource entry was updated.
     */
    public final Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the notes for this resource entry.
     * 
     * @return The notes for this resource entry.
     */
    public abstract List<String> getNotes();

    /**
     * Returns the arbitrary metadata represented by string key-value pairs.
     * 
     * @return The arbitrary metadata represented by string key-value pairs.
     */
    public abstract Map<String, String> getMetadata();

    /**
     * Returns the status string set by a translation partner.
     * This property is reserved for a translation partner's use.
     * 
     * @return The status string set by a translation partner.
     */
    public abstract String getPartnerStatus();

    /**
     * Returns the sequence number of the resource entry.
     * The value of sequence number might be used for sorting resource entries.
     * 
     * @return The sequence number of the resource entry.
     */
    public abstract Integer getSequenceNumber();
}
