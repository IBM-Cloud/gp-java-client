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

/**
 * <code>ServiceInstanceInfo</code> provides read access to the service instance's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public abstract class ServiceInstanceInfo {
    private final String region;
    private final String orgId;
    private final String spaceId;
    private final String serviceId;
    private final String planId;
    private final String cfServiceInstanceId;
    private final boolean disabled;

    /**
     * <code>UsageData</code> provides read access to the service instance's
     * usage information.
     * 
     * @author Yoshito Umaoka
     */
    public static class UsageData {
        private final long size;

        /**
         * Protected constructor for a subclass extending <code>UsageData</code>.
         * 
         * @param size The resource data size.
         */
        protected UsageData(long size) {
            this.size = size;
        }

        /**
         * Returns the size of resource data used by the Globalization
         * Pipeline instance in bytes.
         * 
         * @return The size of resource data used by the Globalization
         * Pipeline instance in bytes.
         */
        public long getSize() {
            return size;
        }
    }
    private final UsageData usageData;

    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>ServiceInstanceData</code>.
     * 
     * @param region    The service instance owner's region.
     * @param orgId     The service instance owner's organization ID.
     * @param spaceId   The ID of the space where the service instance was created.
     * @param serviceId The service ID of Globalization Pipeline
     * @param planId    The Globalization Pipeline's plan ID used by this service instance.
     * @param cfServiceInstanceId   The service instance ID assigned by Bluemix Cloud Foundry.
     * @param disabled  Whether if this service instance is disabled.
     * @param usageData The usage data.
     * @param updatedBy The last user updated the service instance data.
     * @param updatedAt The last date when the service instance data was updated (except usage data).
     */
    protected ServiceInstanceInfo(String region, String orgId, String spaceId,
            String serviceId, String planId, String cfServiceInstanceId, boolean disabled,
            UsageData usageData, String updatedBy, Date updatedAt) {
        this.region = region;
        this.orgId = orgId;
        this.spaceId = spaceId;
        this.serviceId = serviceId;
        this.planId = planId;
        this.cfServiceInstanceId = cfServiceInstanceId;
        this.disabled = disabled;

        this.usageData = usageData;

        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the Bluemix region where the owner of this Globalization
     * Pipeline service is in.
     * 
     * @return The Bluemix  region where the owner of this Globalization
     * Pipeline service is in.
     */
    public final String getRegion() {
        return region;
    }

    /**
     * Returns the ID of Blumix organization owning the Globalization Pipeline
     * service instance.
     * 
     * @return The ID of Blumix organization owning the Globalization Pipeline
     * service instance.
     */
    public final String getOrgId() {
        return orgId;
    }

    /**
     * Returns the ID of user's space where the Globalization Pipeline service
     * instance was created.
     * 
     * @return The ID of user's space where the Globalization Pipeline service
     * instance was created.
     */
    public final String getSpaceId() {
        return spaceId;
    }

    /**
     * Returns the service ID.
     * <p>Note: The service ID is assigned for Globalization Pipeline service.
     * So this API returns the same ID always.
     * 
     * @return The service ID.
     */
    public final String getServiceId() {
        return serviceId;
    }

    /**
     * Returns the plan ID used by the service instance.
     * 
     * @return The plan ID used by the service instance.
     */
    public final String getPlanId() {
        return planId;
    }

    /**
     * Returns the service instance ID assigned by Bluemix CF.
     * <p>Note: This ID is different from service instance ID
     * assigned by the Globalization Pipeline service.
     * 
     * @return The service instance ID assigned by Bluemix CF.
     */
    public final String getCfServiceIntanceId() {
        return cfServiceInstanceId;
    }


    /**
     * Returns if this service instance is disabled.
     * 
     * @return <code>true</code> if this service instance is disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Returns the current usage data of this service instance.
     * 
     * @return The current usage data of this service instance.
     */
    public final UsageData getUsageData() {
        return usageData;
    }

    /**
     * Returns the last user updated this service instance data.
     * 
     * @return The last user updated this service instance data.
     */
    public final String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the last date when this service instance data was updated.
     * <p>Note: The usage data is not counted as last update.
     * 
     * @return The last date when this service instance data was updated.
     */
    public final Date getUpdatedAt() {
        return updatedAt;
    }

}
