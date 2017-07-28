/*  
 * Copyright IBM Corp. 2017
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

/**
 * Industry domain constants used by translation request data.
 * 
 * @author yoshito_umaoka
 */
public enum IndustryDomain {
    /**
     * Aerospace and the military-industrial complex
     */
    AEROMIL("Aerospace and the military-industrial complex"),
    /**
     * Construction
     */
    CNSTRCT("Construction"),
    /**
     * Goods and service
     */
    GDSSVCS("Goods and service"),
    /**
     * Education
     */
    EDUCATN("Education"),
    /**
     * Financial Services
     */
    FINSVCS("Financial Services"),
    /**
     * Government and public sector
     */
    GOVPUBL("Government and public sector"),
    /**
     * Healthcare and social services
     */
    HEALTHC("Healthcare and social services"),
    /**
     * Industrial manufacturing
     */
    INDSTMF("Industrial manufacturing"),
    /**
     * Telecommunication
     */
    TELECOM("Telecommunication"),
    /**
     * Digital media and entertainment
     */
    DMEDENT("Digital media and entertainment"),
    /**
     * Information technology
     */
    INFTECH("Information technology"),
    /**
     * Travel and transportation
     */
    TRVLTRS("Travel and transportation"),
    /**
     * Insurance
     */
    INSURNC("Insurance"),
    /**
     * Energy and utilities
     */
    ENGYUTL("Energy and utilities"),
    /**
     * Agriculture
     */
    AGRICLT("Agriculture");

    private String description;

    private IndustryDomain(String description) {
        this.description = description;
    }

    /**
     * Returns the description in English.
     * @return  The description in English.
     */
    public String getDescription() {
        return description;
    }
}
