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
package com.ibm.g11n.pipeline.client.rb;

import java.util.ResourceBundle.Control;
import java.util.spi.ResourceBundleControlProvider;

import com.ibm.g11n.pipeline.client.ServiceAccount;

/**
 * <code>CloudResourceBundleControlProvider</code> implements
 * {@link java.util.spi.ResourceBundleControlProvider} introduced in Java 8.
 * IBM Globalization Pipeline client SDK jar file contains the Java extension
 * configuration file (<code>META-INF/services/java.util.spi.ResoruceBundleControlProvider</code>),
 * so this implementation is automatically enabled by putting the jar file
 * in the JRE's extension directory.
 * <p>
 * When using the {@link CloudResourceBundle} though the Java extension mechanism, the service
 * account information must be available in either VCAP_SERVICES (on Bluemix) or environment
 * variables. Please refer {@link ServiceAccount} API documentation for the details.
 * <p>
 * If the service account information is insufficient, this service provider implementation will
 * return null and {@link CloudResourceBundle} won't be used.
 * 
 * @author Yoshito Umaoka
 */
public final class CloudResourceBundleControlProvider implements
        ResourceBundleControlProvider {

    @Override
    public Control getControl(String baseName) {
        return CloudResourceBundleControl.getInstance();
   }
}
