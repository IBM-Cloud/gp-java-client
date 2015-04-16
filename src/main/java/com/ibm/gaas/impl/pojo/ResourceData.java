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

import java.util.List;
import java.util.Map;

/**
 * A POJO used for parsing GaaS JSON object resourceData.
 *
 * @author Yoshito Umaoka
 */
public class ResourceData {
    private String language;
    private Map<String, String> data;
    private List<String> inProgress;
    private List<String> failed;

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<String, String> getData() {
        return data;
    }
    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public List<String> getInProgress() {
        return inProgress;
    }
    public void setInProgress(List<String> inProgress) {
        this.inProgress = inProgress;
    }

    public List<String> getFailed() {
        return failed;
    }
    public void setFailed(List<String> failed) {
        this.failed = failed;
    }
}
