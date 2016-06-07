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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>TranslationConfigData</code> represents translation configuration
 * for each translation source/target language pair.
 * 
 * @author Yoshito Umaoka
 */
public class TranslationConfigData {
    private MTServiceData mtService;

    protected TranslationConfigData() {
    }

    public void setMTServiceData(MTServiceData mtService) {
        this.mtService = mtService;
    }

    public MTServiceData getMTServiceData() {
        return mtService;
    }

    public String getUpdatedBy() {
        return null;
    }

    public Date getUpdatedAt() {
        return null;
    }

    public static class MTServiceData {
        private String serviceInstanceId;
        protected Map<String, Object> params;
        protected String updatedBy; // only set by subclass
        protected Date updatedAt;   // only set by subclass

        public MTServiceData(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
        }

        public String getServiceInstanceId() {
            return serviceInstanceId;
        }

        public void setParams(Map<String, Object> params) {
            this.params = new HashMap<>(params);
        }

        public Map<String, Object> getParams() {
            if (params == null) {
                return null;
            }
            return Collections.unmodifiableMap(params);
        }
    }
}
