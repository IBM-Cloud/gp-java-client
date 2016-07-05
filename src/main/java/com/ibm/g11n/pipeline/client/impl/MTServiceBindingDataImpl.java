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
package com.ibm.g11n.pipeline.client.impl;

import java.util.Map;

import com.ibm.g11n.pipeline.client.MTServiceBindingData;

/**
 * MTServiceBindingData implementation class.
 * 
 * @author Yoshito Umaoka
 */
class MTServiceBindingDataImpl extends MTServiceBindingData {

    private final RestMTServiceBinding mtServiceBinding;

    MTServiceBindingDataImpl(RestMTServiceBinding mtServiceBinding) {
        super(mtServiceBinding.getServiceName(),
                mtServiceBinding.getServiceId(),
                mtServiceBinding.getServiceCredentials(),
                mtServiceBinding.getUpdatedBy(),
                mtServiceBinding.getUpdatedAt());
        this.mtServiceBinding = mtServiceBinding;
    }

    @Override
    public String getServiceInstanceName() {
        return mtServiceBinding.getServiceInstanceName();
    }

    @Override
    public String getServiceKeyGuid() {
        return mtServiceBinding.getServiceKeyGuid();
    }

    @Override
    public String getRefreshToken() {
        return mtServiceBinding.getRefreshToken();
    }

    /**
     * Data object used for deserializing MT binding data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestMTServiceBinding extends RestObject {
        private String serviceName;
        private String serviceId;
        private Map<String, Object> serviceCredentials;

        private String serviceInstanceName;
        private String serviceKeyGuid;
        private String refreshToken;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestMTServiceBinding() {
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getServiceId() {
            return serviceId;
        }

        public Map<String, Object> getServiceCredentials() {
            return serviceCredentials;
        }

        public String getServiceInstanceName() {
            return serviceInstanceName;
        }

        public String getServiceKeyGuid() {
            return serviceKeyGuid;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
