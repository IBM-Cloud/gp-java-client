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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>NewTranslationConfigData</code> is used for specifying translation
 * configuration for each source/target language pair.
 * 
 * @author Yoshito Umaoka
 */
public class NewTranslationConfigData {
    private NewMTServiceData mtService;

    /**
     * Constructor.
     */
    public NewTranslationConfigData() {
    }

    /**
     * Sets the machine translation service configuration.
     * 
     * @param mtService The machine translation service configuration.
     */
    public void setMTServiceData(NewMTServiceData mtService) {
        this.mtService = mtService;
    }

    /**
     * Returns the machine translation service configuration.
     * 
     * @return The machine translation service configuration.
     */
    public NewMTServiceData getMTServiceData() {
        return mtService;
    }

    /**
     * <code>NewMTServiceData</code> is used for specifying machine translation
     * service configuration.
     * 
     * @author Yoshito Umaoka
     */
    public static class NewMTServiceData {
        private String serviceInstanceId;
        private Map<String, Object> params;

        /**
         * Constructor.
         * 
         * @param serviceInstanceId The ID of machine translation service instance.
         */
        public NewMTServiceData(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
        }

        /**
         * Returns the ID of machine translation service instance.
         * 
         * @return The ID of machine translation service instance.
         */
        public String getServiceInstanceId() {
            return serviceInstanceId;
        }

        /**
         * Sets optional parameters (key/value pairs) that will be passed to
         * the machine translation service instance.
         * 
         * @param params    The optional parameters (key/value pairs).
         */
        public void setParams(Map<String, Object> params) {
            this.params = new HashMap<>(params);
        }

        /**
         * Returns a map containing optional parameters (key/value pairs) that will
         * be passed to the translation service instance.
         * 
         * @return A map containing optional parameters (key/value pairs) that will
         * be passed to the translation service instance.
         */
        public Map<String, Object> getParams() {
            if (params == null) {
                return null;
            }
            return Collections.unmodifiableMap(params);
        }
    }
}
