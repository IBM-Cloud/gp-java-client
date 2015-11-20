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
package com.ibm.g11n.pipeline.client;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * <code>BundleData</code> provides read access to translation bundle's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public abstract class BundleData {
    private final String sourceLanguage;
    private final boolean readOnly;
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>BundleData</code>.
     * 
     * @param sourceLanguage    The source language of this bundle, specified by BCP 47
     *                          language tag such as "en" for English.
     * @param readOnly          If this bundle is read only.
     * @param updatedBy         The last user updated this bundle's properties.
     * @param updatedAt         The last date when this bundle's properties were updated.
     */
    protected BundleData(String sourceLanguage, boolean readOnly, String updatedBy, Date updatedAt) {
        this.sourceLanguage = sourceLanguage;
        this.readOnly = readOnly;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the source language of this bundle.
     * @return The source language of this bundle.
     */
    public final String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Returns if this bundle is read only.
     * @return <code>true</code> if this bundle is ready only.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns the last user updated this bundle's properties.
     * @return The last user updated this bundle's properties.
     */
    public final String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the last date when this bundle's properties were updated.
     * @return The last date when this bundle's properties were updated.
     */
    public final Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the set of target languages specified by BCP 47 language tags,
     * such as "de" for German and "zh-Hans" for Simplified Chinese.
     * @return The set of target languages.
     */
    public abstract Set<String> getTargetLanguages();

    /**
     * Returns the arbitrary metadata represented by string key-value pairs.
     * @return The arbitrary metadata represented by string key-value pairs.
     */
    public abstract Map<String, String> getMetadata();

    /**
     * Returns the translation partner assigned to this bundle.
     * @return the translation partner assigned to this bundle.
     */
    public abstract String getPartner();
}
