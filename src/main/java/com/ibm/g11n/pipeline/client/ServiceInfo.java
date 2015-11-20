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
 * <code>ServiceInfo</code> provides read access to IBM Globalization
 * Pipeline service's summary information.
 * 
 * @author Yoshito Umaoka
 */
public abstract class ServiceInfo {
    private Map<String, Set<String>> supportedTranslation;

    /**
     * Protected constructor for a subclass extending <code>ServiceInfo</code>.
     * @param supportedTranslation The supported translation pairs.
     */
    protected ServiceInfo(Map<String, Set<String>> supportedTranslation) {
        this.supportedTranslation = supportedTranslation;
    }

    /**
     * Returns a map containing available translation target languages
     * indexed by supported source language.
     * @return A map containing available translation target languages
     * indexed by supported source language.
     */
    public Map<String, Set<String>> getSupportedTranslation() {
        return supportedTranslation;
    }
}
