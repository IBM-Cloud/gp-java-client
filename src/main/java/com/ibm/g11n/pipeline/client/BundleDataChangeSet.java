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
 * <code>BundleDataChangeSet</code> is used for specifying changes
 * of a translation bundle's properties.
 * 
 * @author Yoshito Umaoka
 */
public class BundleDataChangeSet {
    private Set<String> targetLanguages;
    private Boolean readOnly;
    private Map<String, String> metadata;
    private String partner;
    private String segmentSeparatorPattern;
    private String noTranslationPattern;

    /**
     * Constructor, creating an empty change set.
     */
    public BundleDataChangeSet() {
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
    public BundleDataChangeSet setTargetLanguages(Set<String> targetLanguages) {
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
     * Sets <code>Boolean.TRUE</code> to make this bundle read only.
     * 
     * @param readOnly <code>Boolean.TRUE</code> to make this bundle read only.
     * @return This object.
     */
    public BundleDataChangeSet setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
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
     * When a key currently exists in bundle's metadata, the value
     * of the key will be replaced with the new value. When a key
     * currently exists and the new value is empty, the key-value
     * pair will be removed. When a key does not exists, the key-value
     * pair will be added.
     * 
     * @param metadata A map containing new or updated key-value pairs.
     * @return This object.
     */
    public BundleDataChangeSet setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the translation partner assigned to this bundle.
     * 
     * @return The translation partner assigned to this bundle.
     */
    public String getPartner() {
        return partner;
    }

    /**
     * Sets the translation partner assigned to this bundle.
     * 
     * @param partner The translation partner assigned to this bundle.
     * @return This object.
     */
    public BundleDataChangeSet setPartner(String partner) {
        this.partner = partner;
        return this;
    }

    /**
     * Returns the user defined segmentation separator pattern string in Java
     * regular expression syntax.
     * 
     * @return The user defined segmentation separator pattern.
     */
    public String getSegmentSeparatorPattern() {
        return segmentSeparatorPattern;
    }

    /**
     * Sets the user defined segmentation separator pattern string in Java
     * regular expression syntax.
     * <p>
     * The pattern is used for dividing an input resource string value into
     * multiple segments before machine translation. The matching substrings
     * will be preserved after machine translation.
     * 
     * @param segmentSeparatorPattern   The user defined segmentation separator pattern.
     * @return This object.
     */
    public BundleDataChangeSet setSegmentSeparatorPattern(String segmentSeparatorPattern) {
        this.segmentSeparatorPattern = segmentSeparatorPattern;
        return this;
    }

    /**
     * Returns the user defined no-translation pattern string in Java regular
     * expression syntax.
     * 
     * @return The user defined no-translation pattern string.
     */
    public String getNoTranslationPattern() {
        return noTranslationPattern;
    }

    /**
     * Sets the user defined no-translation pattern string in Java regular
     * expression syntax.
     * <p>
     * The pattern is used for preserving matching substrings during machine
     * translation. For example, pattern "IBM|Bluemix" will skip translating
     * the words "IBM" and "Bluemix".
     * 
     * @param noTranslationPattern  The user defined no-translation pattern string.
     * @return This object.
     */
    public BundleDataChangeSet setNoTranslationPattern(String noTranslationPattern) {
        this.noTranslationPattern = noTranslationPattern;
        return this;
    }
}
