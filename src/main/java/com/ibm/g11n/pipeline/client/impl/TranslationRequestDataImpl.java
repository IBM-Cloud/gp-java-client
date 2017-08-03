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
package com.ibm.g11n.pipeline.client.impl;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.g11n.pipeline.client.IndustryDomain;
import com.ibm.g11n.pipeline.client.NewTranslationRequestData;
import com.ibm.g11n.pipeline.client.TranslationRequestData;
import com.ibm.g11n.pipeline.client.TranslationRequestDataChangeSet;
import com.ibm.g11n.pipeline.client.TranslationRequestStatus;
import com.ibm.g11n.pipeline.client.WordCountData;

/**
 * TranslationRequestData implementation class
 * 
 * @author yoshito_umaoka
 */
public class TranslationRequestDataImpl extends TranslationRequestData {
    
    private final RestTranslationRequest translationRequest;

    protected TranslationRequestDataImpl(String id, RestTranslationRequest translationRequest) {
        super(id, translationRequest.getPartner(), translationRequest.getTargetLanguagesByBundle(),
                TranslationRequestStatus.valueOf(translationRequest.getStatus()),
                translationRequest.getCreatedAt(), translationRequest.getUpdatedBy(),
                translationRequest.getUpdatedAt());
        this.translationRequest = translationRequest;
    }

    @Override
    public String getName() {
        return translationRequest.getName();
    }

    @Override
    public String getOrganization() {
        return translationRequest.getOrganization();
    }

    @Override
    public List<String> getEmails() {
        List<String> emails = translationRequest.getEmails();
        if (emails == null) {
            return emails;
        }
        return Collections.unmodifiableList(emails);
    }

    @Override
    public List<String> getPhones() {
        List<String> phones = translationRequest.getPhones();
        if (phones == null) {
            return null;
        }
        return Collections.unmodifiableList(phones);
    }

    @Override
    public EnumSet<IndustryDomain> getDomains() {
        Set<IndustryDomain> domains = new HashSet<>();
        Set<String> domainStrs = translationRequest.getDomains();
        if (domainStrs != null) {
            for (String domainStr : domainStrs) {
                try {
                    IndustryDomain domain = IndustryDomain.valueOf(domainStr);
                    domains.add(domain);
                } catch (IllegalArgumentException e) {
                    // Ignore unknown domain keyword
                }
            }
        }
        return EnumSet.copyOf(domains);
    }

    @Override
    public List<String> getNotes() {
        List<String> notes = translationRequest.getNotes();
        if (notes == null) {
            return null;
        }
        return Collections.unmodifiableList(notes);
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = translationRequest.getMetadata();
        if (metadata == null) {
            return null;
        }
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public Map<String, WordCountData> getWordCountData() {
        Map<String, WordCountData> wcDataByBundle = new TreeMap<>();
        Map<String, RestWordCountData> restWcDataByBundle = translationRequest.getWordCountsByBundle();
        if (restWcDataByBundle != null) {
            for (Entry<String, RestWordCountData> entry : restWcDataByBundle.entrySet()) {
                String bundleId = entry.getKey();
                RestWordCountData restWcData = entry.getValue();

                String srcLang = restWcData.getSourceLanguage();
                Map<String, Integer> wordsByTargetLanguages = new TreeMap<>(restWcData.getCounts());
                WordCountData wcData = new WordCountData(srcLang, wordsByTargetLanguages);
                wcDataByBundle.put(bundleId, wcData);
            }
        }
        return wcDataByBundle;
    }

    @Override
    public Date getEstimatedComletion() {
        Date estimatedCompletion = translationRequest.getEstimatedCompletion();
        if (estimatedCompletion == null) {
            return null;
        }
        return (Date)estimatedCompletion.clone();
    }

    @Override
    public Date getSubmittedAt() {
        Date submittedAt = translationRequest.getSubmittedAt();
        if (submittedAt == null) {
            return null;
        }
        return (Date)submittedAt.clone();
    }

    @Override
    public Date getStartedAt() {
        Date startedAt = translationRequest.getStartedAt();
        if (startedAt == null) {
            return null;
        }
        return (Date)startedAt.clone();
    }

    @Override
    public Date getTranslatedAt() {
        Date translatedAt = translationRequest.getTranslatedAt();
        if (translatedAt == null) {
            return null;
        }
        return (Date)translatedAt.clone();
    }

    @Override
    public Date getMergedAt() {
        Date mergedAt = translationRequest.getMergedAt();
        if (mergedAt == null) {
            return null;
        }
        return (Date)mergedAt.clone();
    }


    /**
     * Data object used for deserializing translation request data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestTranslationRequest extends RestObject {
        private String partner;
        private String name;
        private Map<String, Set<String>> targetLanguagesByBundle;
        private String organization;
        private List<String> emails;
        private List<String> phones;
        private Set<String> domains;
        private List<String> notes;
        private Map<String, String> metadata;
        private String status;
        private Map<String, RestWordCountData> wordCountsByBundle;
        private Date estimatedCompletion;
        private Date createdAt;
        private Date startedAt;
        private Date submittedAt;
        private Date translatedAt;
        private Date mergedAt;

        public String getPartner() {
            return partner;
        }

        public String getName() {
            return name;
        }

        public Map<String, Set<String>> getTargetLanguagesByBundle() {
            return targetLanguagesByBundle;
        }

        public String getOrganization() {
            return organization;
        }

        public List<String> getEmails() {
            return emails;
        }

        public List<String> getPhones() {
            return phones;
        }

        public Set<String> getDomains() {
            return domains;
        }

        public List<String> getNotes() {
            return notes;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public String getStatus() {
            return status;
        }

        public Map<String, RestWordCountData> getWordCountsByBundle() {
            return wordCountsByBundle;
        }

        public Date getEstimatedCompletion() {
            return estimatedCompletion;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public Date getStartedAt() {
            return startedAt;
        }

        public Date getSubmittedAt() {
            return submittedAt;
        }

        public Date getTranslatedAt() {
            return translatedAt;
        }

        public Date getMergedAt() {
            return mergedAt;
        }
    }

    /**
     * Data object used for deserializing word count data in JSON.
     * 
     * @author yoshito_umaoka
     */
    static class RestWordCountData {
        private String sourceLanguage;
        private Map<String, Integer> counts;

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }
    }

    /**
     * Data object used for serializing NewTranslationRequestData and
     * TranslationRequestDataChangeSet to JSON
     * 
     * @author yoshito_umaoka
     */
    static class RestInputTranslationRequestData {
        private Map<String, Set<String>> targetLanguagesByBundle;
        private String partner;
        private String name;
        private String organization;
        private List<String> emails;
        private List<String> phones;
        private Set<String> domains;
        private List<String> notes;
        private String status;
//        private Map<String, String> metadata;

        public RestInputTranslationRequestData(NewTranslationRequestData newTrData) {
            this.targetLanguagesByBundle = newTrData.getTargetLanguagesByBundle();
            this.partner = newTrData.getPartner();
            this.name = newTrData.getName();
            this.organization = newTrData.getOrganization();
            this.emails = newTrData.getEmails();
            this.phones = newTrData.getPhones();
            this.notes = newTrData.getNotes();

            this.status = newTrData.isSubmit() ? "SUBMITTED" : "DRAFT";

            Set<IndustryDomain> domainEnums = newTrData.getDomains();
            if (domainEnums != null && !domainEnums.isEmpty()) {
                Set<String> tmpDomains = new HashSet<>();
                for (IndustryDomain d : domainEnums) {
                    tmpDomains.add(d.toString());
                }
                this.domains = tmpDomains;
            }
        }

        public RestInputTranslationRequestData(TranslationRequestDataChangeSet trChangeSet) {
            this.targetLanguagesByBundle = trChangeSet.getTargetLanguagesByBundle();
            this.partner = trChangeSet.getPartner();
            this.name = trChangeSet.getName();
            this.organization = trChangeSet.getOrganization();
            this.emails = trChangeSet.getEmails();
            this.phones = trChangeSet.getPhones();
            this.notes = trChangeSet.getNotes();

            this.status = trChangeSet.isSubmit() ? "SUBMITTED" : "DRAFT";

            Set<IndustryDomain> domainEnums = trChangeSet.getDomains();
            if (domainEnums != null && !domainEnums.isEmpty()) {
                Set<String> tmpDomains = new HashSet<>();
                for (IndustryDomain d : domainEnums) {
                    tmpDomains.add(d.toString());
                }
                this.domains = tmpDomains;
            }
        }

        public String getPartner() {
            return partner;
        }

        public String getName() {
            return name;
        }

        public Map<String, Set<String>> getTargetLanguagesByBundle() {
            return targetLanguagesByBundle;
        }

        public String getOrganization() {
            return organization;
        }

        public List<String> getEmails() {
            return emails;
        }

        public List<String> getPhones() {
            return phones;
        }

        public Set<String> getDomains() {
            return domains;
        }

        public List<String> getNotes() {
            return notes;
        }

//        public Map<String, String> getMetadata() {
//            return metadata;
//        }

        public String getStatus() {
            return status;
        }
    }

}
