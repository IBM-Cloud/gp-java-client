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

import com.ibm.g11n.pipeline.client.ServiceInstanceInfo;

/**
 * {@link ServiceInstanceInfo} implementation class.
 * 
 * @author Yoshito Umaoka
 */
class ServiceInstanceInfoImpl extends ServiceInstanceInfo {
    static class UsageDataImpl extends UsageData {
        protected UsageDataImpl(RestUsageData restUsageData) {
            super(restUsageData.getSize());
        }
    }

    ServiceInstanceInfoImpl(RestServiceInstanceInfo restServiceInstanceInfo) {
        super(
            restServiceInstanceInfo.getRegion(),
            restServiceInstanceInfo.getOrgId(),
            restServiceInstanceInfo.getSpaceId(),
            restServiceInstanceInfo.getServiceId(),
            restServiceInstanceInfo.getPlanId(),
            restServiceInstanceInfo.getCfServiceInstanceId(),
            restServiceInstanceInfo.isDisabled(),
            (restServiceInstanceInfo.getUsageData() != null ?
                    new UsageDataImpl(restServiceInstanceInfo.getUsageData()) : null),
            restServiceInstanceInfo.getUpdatedBy(),
            restServiceInstanceInfo.getUpdatedAt());
    }

    /**
     * Data object used for deserializing external service instance info in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestServiceInstanceInfo {
        private String region;
        private String cfServiceInstanceId;
        private String serviceId;
        private String orgId;
        private String spaceId;
        private String planId;
        private Boolean disabled;
        private RestUsageData usage;
        private String updatedBy;
        private Date updatedAt;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestServiceInstanceInfo() {
        }

        public String getRegion() {
            return region;
        }

        public String getCfServiceInstanceId() {
            return cfServiceInstanceId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String getOrgId() {
            return orgId;
        }

        public String getSpaceId() {
            return spaceId;
        }

        public String getPlanId() {
            return planId;
        }

        public boolean isDisabled() {
            if (disabled != null) {
                return disabled.booleanValue();
            }
            return false;
        }

        public RestUsageData getUsageData() {
            return usage;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }
    }

    /**
     * Data object used for deserializing external service instance's usage
     * data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestUsageData {
        private long size = -1;

        /**
         * No-args constructor used by JSON unmarshaller
         */
        RestUsageData() {
        }

        long getSize() {
            return size;
        }
    }
}
