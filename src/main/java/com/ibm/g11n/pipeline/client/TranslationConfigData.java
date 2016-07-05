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

import java.util.Date;
import java.util.Map;

/**
 * <code>TranslationConfigData</code> provides a read only access to translation
 * configuration for each source/target language pair.
 * 
 * @author Yoshito Umaoka
 */
public abstract class TranslationConfigData {
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>TranslationConfigData</code>.
     * 
     * @param updatedBy The last user updated this translation configuration.
     * @param updatedAt The last date when this translation configuration was updated.
     */
    protected TranslationConfigData(String updatedBy, Date updatedAt) {
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public abstract MTServiceData getMTServiceData();

    /**
     * Returns the last user updated this translation configuration.
     * 
     * @return The last user updated this translation configuration.
     */
    public final String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the last date when this translation configuration was updated.
     * 
     * @return The last date when this translation configuration was updated.
     */
    public final Date getUpdatedAt() {
        return updatedAt;
    }

    public static abstract class MTServiceData {
        private final String serviceInstanceId;
        protected final String updatedBy;
        protected final Date updatedAt;

        /**
         * Protected constructor for a subclass extending <code>MTServiceData</code>.
         * 
         * @param serviceInstanceId The ID of machine translation service instance.
         * @param updatedBy The last user updated this machine translation service
         *                  configuration.
         * @param updatedAt The last date when this machine translation service
         *                  configuration was updated.
         */
        public MTServiceData(String serviceInstanceId, String updatedBy, Date updatedAt) {
            this.serviceInstanceId = serviceInstanceId;
            this.updatedBy = updatedBy;
            this.updatedAt = updatedAt;
        }

        /**
         * Returns the ID of machine translation service instance.
         * 
         * @return The ID of machine translation service instance.
         */
        public final String getServiceInstanceId() {
            return serviceInstanceId;
        }

        /**
         * Returns a map containing optional parameters (key/value pairs) that will
         * be passed to the translation service instance.
         * 
         * @return A map containing optional parameters (key/value pairs) that will
         * be passed to the translation service instance.
         */
        public abstract Map<String, Object> getParams();

        /**
         * Returns the last user updated this machine translation service configuration.
         * 
         * @return The last user updated this machine translation service configuration.
         */
        public final String getUpdatedBy() {
            return updatedBy;
        }

        /**
         * Returns the last date when this machine translation service configuration
         * was updated.
         * 
         * @return The last date when this machine translation service configuration was
         * updated.
         */
        public final Date getUpdatedAt() {
            return updatedAt;
        }
    }
}
