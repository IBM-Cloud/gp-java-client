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

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>TranslationRequestData</code> provides read access to the translation request's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public abstract class TranslationRequestData {
    private final String id;
    private final String partner;
    private final Map<String, Set<String>> targetLanguagesByBundle;
    private final TranslationRequestStatus status;
    private final Date createdAt;
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>TranslationRequestData</code>.
     *
     * @param id        The translation request ID
     * @param partner   The assigned professional translation post editing service provider's ID.
     * @param targetLanguagesByBundle   The map containing target languages indexed by bundle IDs.
     * @param status    The current translation request status.
     * @param createdAt The date when the translation request was originally created.
     * @param updatedBy The last user updated this bundle's properties.
     * @param updatedAt The last date when this bundle's properties were updated.
     */
    protected TranslationRequestData(String id, String partner, Map<String, Set<String>> targetLanguagesByBundle,
            TranslationRequestStatus status, Date createdAt, String updatedBy, Date updatedAt) {
        this.id = id;
        this.partner = partner;
        this.targetLanguagesByBundle = targetLanguagesByBundle;
        this.status = status;
        this.createdAt = createdAt;

        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the translation request's ID.
     * 
     * @return The translation request's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the assigned professional translation post editing service provider's ID.
     * This method always returns non-empty string.
     * 
     * @return  The assigned professional translation post editing service provider's ID.
     */
    public String getPartner() {
        return partner;
    }

    /**
     * Returns the map containing target languages indexed by bundle IDs.
     * This method always returns non-null map.
     * 
     * @return  The map containing target languages indexed by bundle IDs.
     */
    public Map<String, Set<String>> getTargetLanguagesByBundle() {
        if (targetLanguagesByBundle == null) {
            assert false;
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(targetLanguagesByBundle);
    }

    /**
     * Returns the current translation request status.
     * This method never return null.
     * 
     * @return The current translation request status.
     */
    public TranslationRequestStatus getStatus() {
        return status;
    }

    /**
     * Returns the date when the translation request was originally created.
     * This method always returns non-null date.
     * 
     * @return  The date when the translation request was originally created.
     */
    public Date getCreatedAt() {
        assert createdAt != null;
        return createdAt;
    }

    /**
     * Returns the last user updated this bundle's properties.
     * This method always returns non-null date.
     * 
     * @return The last user updated this bundle's properties.
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the last date when this bundle's properties were updated.
     * This method always returns non-null date.
     * 
     * @return The last date when this bundle's properties were updated.
     */
    public final Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the name of this translation request.
     * 
     * @return  The name of this translation request.
     */
    public abstract String getName();

    /**
     * Returns the organization name of the requester.
     * 
     * @return  The organization name of the requester.
     */
    public abstract String getOrganization();

    /**
     * Returns a list of the requester's e-mail addresses. When the translation requet's
     * status is changed, an e-mail notification will be sent to the addresses.
     * 
     * @return  A list of the requester's e-mail addresses.
     */
    public abstract List<String> getEmails();

    /**
     * Returns a list of the requester's contact phones.
     * 
     * @return  A list of the requester's contact phones.
     */
    public abstract List<String> getPhones();

    /**
     * Returns a set of the translation domains.
     * 
     * @return  A set of the translation domains.
     */
    public abstract EnumSet<IndustryDomain> getDomains();

    /**
     * Returns the translation notes.
     * 
     * @return  The translation notes.
     */
    public abstract List<String> getNotes();

    /**
     * Returns a map containing the metadata of this translation request represented
     * by key-value pairs.
     * 
     * @return A map containing the metadata of this translation request represented
     * by key-value pairs.
     */
    public abstract Map<String, String> getMetadata();

    /**
     * Returns the word count information of this translation request.
     * 
     * @return  The word count information of this translation request.
     */
    public abstract Map<String, WordCountData> getWordCountData();

    /**
     * Returns the estimated completion date of this translation request.
     * 
     * @return  The estimated completion date of this translation request.
     */
    public abstract Date getEstimatedComletion();

    /**
     * Returns the date when this translation request was submitted, or null
     * if this translation request is not yet submitted.
     * 
     * @return  The date when this translation request was submitted, or null.
     */
    public abstract Date getSubmittedAt();

    /**
     * Returns the date when this translation request was accepted by the professional
     * translation post editing service provider, or null if this translation request
     * is not yet accepted.
     * 
     * @return  The the date when this translation request was accepted by the professional
     * translation post editing service provider, or null
     */
    public abstract Date getStartedAt();

    /**
     * Returns the date when the assigned professional translation post editing service
     * provider finished editing the translation request contents, or null if not yet
     * finished.
     * 
     * @return  The date when the assigned professional translation post editing service
     * provider finished editing the translation request contents, or null.
     */
    public abstract Date getTranslatedAt();

    /**
     * Returns the date when Globalization Pipeline service merged the post editing results
     * back to the original bundles, or null if not yet merged.
     * 
     * @return  The date when Globalization Pipeline service merged the post editing results
     * back to the original bundles, or null.
     */
    public abstract Date getMergedAt();
}
