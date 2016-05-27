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

import java.util.Date;
import java.util.Map;

import com.ibm.g11n.pipeline.client.TranslationConfigData;

/**
 * TranslationConfigData implementation class
 * 
 * @author Yoshito Umaoka
 */
public class TranslationConfigDataImpl extends TranslationConfigData {
    RestTranslationConfigData restTransConfig;

    TranslationConfigDataImpl(RestTranslationConfigData restTransConfig) {
        this.restTransConfig = restTransConfig;
        if (restTransConfig.mtService != null) {
            setMTServiceData(new MTServiceDataImpl(restTransConfig.mtService));
        }
    }

    public static class MTServiceDataImpl extends MTServiceData {
        MTServiceDataImpl(RestMTServiceData restMTService) {
            super(restMTService.getServiceInstanceId());
            this.params = restMTService.getParams();
            this.updatedBy = restMTService.getUpdatedBy();
            this.updatedAt = restMTService.getUpdatedAt();
        }
    }

    public String getUpdatedBy() {
        return restTransConfig.getUpdatedBy();
    }

    public Date getUpdatedAt() {
        return restTransConfig.getUpdatedAt();
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
        private RestTranslationConfigData() {
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
        private RestMTServiceData() {
        }

        public String getServiceInstanceId() {
            return serviceInstanceId;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }
}
