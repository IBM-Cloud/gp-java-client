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
import java.util.Map;

/**
 * <code>MTServiceBindingData</code> provides read access to machine translation service
 * binding data properties.
 * 
 * @author Yoshito Umaoka
 */
public abstract class MTServiceBindingData {
    private final String serviceName;
    private final String serviceId;
    private final Map<String, Object> serviceCredentials;
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * Protected constructor for a subclass extending <code>MTServiceBindingData</code>.
     *
     * @param serviceName   The MT service name
     * @param serviceId     The MT service ID
     * @param serviceCredentials    The service credentials
     * @param updatedBy     The user last updated
     * @param updatedAt     The date last updated
     */
    protected MTServiceBindingData(String serviceName, String serviceId,
            Map<String, Object> serviceCredentials, String updatedBy, Date updatedAt) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.serviceCredentials = serviceCredentials;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the machine translation service name.
     * 
     * @return The machine translation service name.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Returns the machine translation service ID.
     * 
     * @return The machine translation service ID.
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Returns the credentials used for accessing the machine translation service.
     * 
     * @return The credentials used for accessing the machine translation service.
     */
    public Map<String, Object> getServiceCredentials() {
        if (serviceCredentials == null) {
            return null;
        }
        return Collections.unmodifiableMap(serviceCredentials);
    }

    /**
     * Returns the user saved the machine translation service binding data.
     * 
     * @return The user saved the machine translation service binding data.
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the date when the machine translation service binding data was saved.
     * 
     * @return The date when the machine translation service binding data was saved.
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the machine translation service instance name.
     * 
     * @return The machine translation service instance name.
     */
    public abstract String getServiceInstanceName();

    /**
     * Returns the GUID of service key maintained by Bluemix. This is available
     * only for machine translation services on Bluemix.
     * 
     * @return The GUID of service key.
     */
    public abstract String getServiceKeyGuid();

    /**
     * Returns the refresh token used by Globalization Pipeline to modify
     * machine translation service binding. This is available only for
     * machine translation services on Bluemix.
     * 
     * @return The refresh token.
     */
    public abstract String getRefreshToken();
}

