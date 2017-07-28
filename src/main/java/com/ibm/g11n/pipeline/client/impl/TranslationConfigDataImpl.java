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

import java.util.Collections;
import java.util.Map;

import com.ibm.g11n.pipeline.client.TranslationConfigData;

/**
 * TranslationConfigData implementation class.
 * 
 * @author Yoshito Umaoka
 */
class TranslationConfigDataImpl extends TranslationConfigData {

    static class MTServiceDataImpl extends MTServiceData {
        private final Map<String, Object> params;
 
        MTServiceDataImpl(RestMTServiceData restMTService) {
            super(restMTService.getServiceInstanceId(),
                    restMTService.getUpdatedBy(), restMTService.getUpdatedAt());
            this.params = restMTService.getParams();
        }

        @Override
        public Map<String, Object> getParams() {
            if (params == null) {
                return null;
            }
            return Collections.unmodifiableMap(params);
        }
    }

    MTServiceDataImpl mtService;

    TranslationConfigDataImpl(RestTranslationConfigData restTransConfig) {
        super(restTransConfig.getUpdatedBy(), restTransConfig.getUpdatedAt());
        this.mtService = new MTServiceDataImpl(restTransConfig.getMTService());
    }

    @Override
    public MTServiceData getMTServiceData() {
        return mtService;
    }

    /**
     * Data object used for deserializing translation config data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestTranslationConfigData extends RestObject {
        private RestMTServiceData mtService;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestTranslationConfigData() {
        }

        public RestMTServiceData getMTService() {
            return mtService;
        }
    }

    /**
     * Data object used for deserializing MT service data in translation
     * config data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestMTServiceData extends RestObject {
        private String serviceInstanceId;
        private Map<String, Object> params;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestMTServiceData() {
        }

        public String getServiceInstanceId() {
            return serviceInstanceId;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }
}
