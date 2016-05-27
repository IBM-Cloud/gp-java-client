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
package com.ibm.g11n.pipeline.client.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.ibm.g11n.pipeline.client.ServiceInfo;
import com.ibm.g11n.pipeline.client.impl.ServiceInfoImpl.ExternalServiceInfoImpl.RestExternalServiceInfo;

/**
 * {@link ServiceInfo} implementation class.
 * 
 * @author Yoshito Umaoka
 */
class ServiceInfoImpl extends ServiceInfo {
    private Collection<ExternalServiceInfo> externalServices;

    ServiceInfoImpl(Map<String, Set<String>> supportedTranslation,
            Collection<RestExternalServiceInfo> restExternalServices) {
        super(supportedTranslation);
        if (restExternalServices != null) {
            externalServices = new ArrayList<>(restExternalServices.size());
            for (RestExternalServiceInfo restExtSvc : restExternalServices) {
                externalServices.add(new ExternalServiceInfoImpl(restExtSvc));
            }
        }
    }

    @Override
    public Collection<ExternalServiceInfo> getExternalServices() {
        if (externalServices == null) {
            return null;
        }
        return Collections.unmodifiableCollection(externalServices);
    }

    static class ExternalServiceInfoImpl extends ServiceInfo.ExternalServiceInfo {
        private RestExternalServiceInfo restExternalService;

        ExternalServiceInfoImpl(RestExternalServiceInfo restExternalService) {
            super(restExternalService.getType(),
                    restExternalService.getId(),
                    restExternalService.getName());
            this.restExternalService = restExternalService;
        }

        @Override
        public Map<String, Set<String>> getSupportedTranslation() {
            Map<String, Set<String>> supportedTranslation =
                    restExternalService.getSupportedTranslation();
            if (supportedTranslation == null) {
                return null;
            }

            return Collections.unmodifiableMap(supportedTranslation);
        }

        /**
         * Data object used for deserializing external service info in JSON.
         * 
         * @author Yoshito Umaoka
         */
        static class RestExternalServiceInfo {
            private String type;
            private String name;
            private String id;
            private Map<String, Set<String>> supportedTranslation;

            /**
             * No-args constructor used by JSON unmarshaller
             */
            private RestExternalServiceInfo() {
            }

            public String getType() {
                return type;
            }

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }

            public Map<String, Set<String>> getSupportedTranslation() {
                return supportedTranslation;
            }
        }
    }
}
