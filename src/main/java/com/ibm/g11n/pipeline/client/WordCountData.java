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
import java.util.Map;
import java.util.TreeMap;

/**
 * WordCountData represents a source language and word count by
 * each target language.
 * 
 * @author yoshito_umaoka
 */
public class WordCountData {
    private String sourceLanguage;
    private Map<String, Integer> wordsByTargetLanguages;

    /**
     * Constructor.
     * 
     * @param sourceLanguage            The source language.
     * @param wordsByTargetLanguages    The word counts by target languages.
     * @throws NullPointerException When <code>sourceLanguage</code> or <code>wordsByTargetLanguages</code>
     * is null.
     */
    public WordCountData(String sourceLanguage, Map<String, Integer> wordsByTargetLanguages) {
        if (sourceLanguage == null || wordsByTargetLanguages == null) {
            throw new NullPointerException("sourceLanguage or wordsByTargetLanguages is null");
        }
        this.sourceLanguage = sourceLanguage;
        this.wordsByTargetLanguages = Collections.unmodifiableMap(new TreeMap<>(wordsByTargetLanguages));
    }

    /**
     * Returns the source language.
     * 
     * @return  The source language.
     */
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Returns an unmodifiable map containing word counts by target languages.
     * 
     * @return  An unmodifiable map containing word counts by target languages.
     */
    public Map<String, Integer> getWordsByTargetLanguages() {
        return wordsByTargetLanguages;
    }
}
