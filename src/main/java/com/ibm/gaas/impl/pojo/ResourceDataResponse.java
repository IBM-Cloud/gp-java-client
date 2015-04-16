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
package com.ibm.gaas.impl.pojo;

/**
 * A POJO used for parsing GaaS GET /v1/projects/{projectID}/{languageID}
 * response.
 * 
 * @author Yoshito Umaoka
 */
public class ResourceDataResponse {
    private String status;
    private String message;
    private ResourceData resourceData;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public ResourceData getResourceData() {
        return resourceData;
    }
    public void setResourceData(ResourceData resourceData) {
        this.resourceData = resourceData;
    }
}
