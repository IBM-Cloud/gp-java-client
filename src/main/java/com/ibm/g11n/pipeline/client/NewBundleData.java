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

import java.util.Map;
import java.util.Set;

/**
 * <code>NewBundleData</code> is used for specifying new bundle's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public class NewBundleData {
    private final String sourceLanguage;
    private Set<String> targetLanguages;
    private Map<String, String> metadata;
    private String partner;

    /**
     * Constructor.
     * @param sourceLanguage    The source language of the new bundle, specified
     *                          by BCP 47 language tag.
     */
    public NewBundleData(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    /**
     * Returns the source language.
     * @return The source language.
     */
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Returns the set of target languages.
     * @return The set of target languages.
     */
    public Set<String> getTargetLanguages() {
        return targetLanguages;
    }

    /**
     * Sets the set of target languages specified by BCP 47 language tags.
     * @param targetLanguages   The set of target languages specified by BCP 47
     *                          language tags.
     * @return This object.
     */
    public NewBundleData setTargetLanguages(Set<String> targetLanguages) {
        this.targetLanguages = targetLanguages;
        return this;
    }

    /**
     * Returns a map containing the key-value pairs.
     * @return A map containing the key-value pairs.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets arbitrary metadata associated with the new bundle specified
     * by a map containing string key-value pairs.
     * @param metadata  A map containing string key-value pairs.
     * @return This object.
     */
    public NewBundleData setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the translation partner assigned to the new bundle.
     * @return The translation partner assigned to the new bundle.
     */
    public String getPartner() {
        return partner;
    }

    /**
     * Sets the translation partner assigned to the new bundle.
     * @param partner The translation partner assigned to the new bundle.
     * @return This object.
     */
    public NewBundleData setPartner(String partner) {
        this.partner = partner;
        return this;
    }
}
