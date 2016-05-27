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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ibm.g11n.pipeline.client.ServiceInfo.ExternalServiceInfo;

/**
 * Test cases for service APIs in ServiceClient.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceClientServiceTest extends AbstractServiceClientTest {

    //
    // getServiceInfo
    //

    @Test
    public void getServiceInfo_Call_ShouldSuccess() throws ServiceException {
        ServiceInfo serviceInfo = client.getServiceInfo();
        Map<String, Set<String>> supportedTranslation = serviceInfo.getSupportedTranslation();
        assertNotNull("supportedTranslation should not be null", supportedTranslation);

        Set<String> supportedTargets = supportedTranslation.get("en");
        assertNotNull("supportedTranslation should contain en as source", supportedTargets);
        assertTrue("number of supported translation taget should be at least 9",
                supportedTargets.size() >= 9);

        Collection<ExternalServiceInfo> externalServices = serviceInfo.getExternalServices();
        assertNotNull("external services should not be null", externalServices);
        assertTrue("number of external services should be at least 2",
                externalServices.size() >= 2);
    }
}
