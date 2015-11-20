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
package com.ibm.g11n.pipeline.client.impl;

import java.util.Map;
import java.util.Set;

/**
 * Data object used for deserializing bundle data in JSON.
 * 
 * @author Yoshito Umaoka
 */
class RestBundle extends RestObject {
    private String sourceLanguage;
    private Set<String> targetLanguages;
    private boolean readOnly;
    private Map<String, String> metadata;
    private String partner;

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public Set<String> getTargetLanguages() {
        return targetLanguages;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getPartner() {
        return partner;
    }
}
