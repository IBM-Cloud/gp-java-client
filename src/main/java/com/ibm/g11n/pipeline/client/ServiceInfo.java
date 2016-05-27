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

import java.util.Collection;
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
     * 
     * @param supportedTranslation The supported translation pairs.
     */
    protected ServiceInfo(Map<String, Set<String>> supportedTranslation) {
        this.supportedTranslation = supportedTranslation;
    }

    /**
     * Returns a map containing available translation target languages
     * indexed by supported source language.
     * 
     * @return A map containing available translation target languages
     * indexed by supported source language.
     */
    public Map<String, Set<String>> getSupportedTranslation() {
        return supportedTranslation;
    }

    /**
     * Returns a collection of external services information that can be used by
     * Globalization Pipeline service optionally.
     * 
     * @return A collection of external services information
     */
    public abstract Collection<ExternalServiceInfo> getExternalServices();


    /**
     * <code>ExternalServiceInfo</code> provides read access to an external
     * service's summary information.
     * 
     * @author Yoshito Umaoka
     */
    public static abstract class ExternalServiceInfo {
        private final String type;
        private final String id;
        private final String name;

        /**
         * Protected constructor for a subclass extending <code>ExternalServiceInfo</code>.
         * 
         * @param type  The service type.
         * @param id    The service ID.
         * @param name  The service name.
         */
        protected ExternalServiceInfo(String type, String id, String name) {
            this.type = type;
            this.id = id;
            this.name = name;
        }

        /**
         * Returns the service type.
         * 
         * @return The service type.
         */
        public String getType() {
            return type;
        }

        /**
         * Returns the service ID.
         * 
         * @return The service ID.
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the service name.
         * 
         * @return The service name.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns supported translation languages if the service is a translation
         * service.
         * 
         * @return Supported translation languages, or <code>null</code> when
         * supported languages are unknown or the service is not a translation
         * service.
         */
        public abstract Map<String, Set<String>> getSupportedTranslation();
    }
}
