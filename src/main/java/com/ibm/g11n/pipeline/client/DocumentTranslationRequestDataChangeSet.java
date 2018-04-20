/*  
 * Copyright IBM Corp. 2018
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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>DocumentTranslationRequestDataChangeSet</code> is used for specifying changes
 * of a document translation request.
 * 
 * @author jugudanniesundar
 */
public class DocumentTranslationRequestDataChangeSet {
    private Map<String, Map<String, Set<String>>> targetLanguagesMap;
    private String partner;
    private String name;
    private String organization;
    private List<String> emails;
    private List<String> phones;
    private EnumSet<IndustryDomain> domains;
    private List<String> notes;
    private Map<String, String> metadata;
    private Map<String, String> partnerParameters;
    private boolean submit;

    /**
     * Constructor.
     */
    public DocumentTranslationRequestDataChangeSet() {
    }

    /**
     * Sets a map containing target languages indexed by document ids. This method adopts
     * the input map without creating a safe copy.
     * 
     * @param targetLanguagesMap   A map containing target languages indexed by
     *                                  document ids.
     * @return  This object.
     * @throws NullPointerException When the input <code>targetLanguagesMap</code> is null.
     */
    public DocumentTranslationRequestDataChangeSet setTargetLanguagesMap(
            Map<String, Map<String, Set<String>>> targetLanguagesMap) {
        // TODO - check empty map?
        if (targetLanguagesMap == null) {
            throw new NullPointerException("The input map is null.");
        }
        this.targetLanguagesMap = targetLanguagesMap;
        return this;
    }

    /**
     * Returns a map containing target languages indexed by document type and ids in this change set.
     * This method returns a map held by this object without creating a safe copy.
     * 
     * @return  A map containing target languages indexed by document type and ids.
     */
    public Map<String, Map<String, Set<String>>> getTargetLanguagesMap() {
        return targetLanguagesMap;
    }

    /**
     * Sets the translation post editing service provider's id.
     * 
     * @param partner   The translation post editing service provider's id.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setPartner(String partner) {
        this.partner = partner;
        return this;
    }

    /**
     * Returns the translation post editing service provider's id.
     * 
     * @return  The translation post editing service provider's id.
     */
    public String getPartner() {
        return partner;
    }

    /**
     * Sets the name of this translation request.
     * 
     * @param name  The name of this translation request.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the name of this translation request in this change set.
     * 
     * @return  The name of this translation request.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the organization name of the requester.
     * 
     * @param organization  The organization name of the requester.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    /**
     * Returns the organization name of the requester in this change set.
     * 
     * @return  The organization name of the requester.
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Sets a list of the request's e-mail addresses. A non-empty e-mail addresses
     * must be set if this object is created for submitting a new request.
     * 
     * @param emails    A list of the requester's e-mail addresses.
     * @return  This object.
     */

    public DocumentTranslationRequestDataChangeSet setEmails(List<String> emails) {
        this.emails = emails;
        return this;
    }

    /**
     * Returns a list of the requester's e-mail addresses in this change set.
     * 
     * @return  A list of the requester's e-mail addresses.
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Sets a list of the requester's contact phones.
     * 
     * @param phones    A list of the requester's contact phones.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setPhones(List<String> phones) {
        this.phones = phones;
        return this;
    }

    /**
     * Returns a list of the requester's contact phones in this change set.
     * 
     * @return  A list of the requester's contact phones.
     */
    public List<String> getPhones() {
        return phones;
    }

    /**
     * Sets a set of the industry domains representing the contents.
     * @param domains   A set of the industry domains representing the contents.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setDomains(EnumSet<IndustryDomain> domains) {
        this.domains = domains;
        return this;
    }

    /**
     * Returns a set of the industry domains representing the contents in this change set.
     * 
     * @return  A set of industry domains representing the contents.
     */
    public EnumSet<IndustryDomain> getDomains() {
        return domains;
    }

    /**
     * Sets the translation notes.
     * 
     * @param notes The translation notes.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setNotes(List<String> notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Returns the translation notes in this change set.
     * 
     * @return  The translation notes.
     */
    public List<String> getNotes() {
        return notes;
    }

    /**
     * Sets a map containing the metadata of this translation request represented
     * by key-value pairs.
     * 
     * <p>This map should specify only delta from the current map. To remove a specific
     * key from existing metadta filed, specify the key to be removed with an empty value.
     * 
     * @param metadata  A map containing the metadata of this translation request represented
     * by key-value pairs.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns a map containing the metadata of this translation request represented
     * by key-value pairs in this change set.
     * 
     * @return A map containing the metadata of this translation request represented
     * by key-value pairs.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets a map containing key-value pairs specifying configuration parameters
     * passed to professional human post editing service provider.
     * 
     * <p>This map should specify only delta from the current map. To remove a specific
     * key from existing partnerParameters filed, specify the key to be removed with an
     * empty value.
     * 
     * @param partnerParameters A map containing key-value pairs specifying configuration parameters.
     * @return This object.
     */
    public DocumentTranslationRequestDataChangeSet setPartnerParameters(Map<String, String> partnerParameters) {
        this.partnerParameters = partnerParameters;
        return this;
    }

    /**
     * Returns a map containing key-value pairs specifying configuration parameters
     * passed to professional human post editing service provider.
     * 
     * @return A map containing key-value pairs specifying configuration parameters
     * passed to professional human post editing service provider.
     */
    public Map<String, String> getPartnerParameters() {
        return partnerParameters;
    }

    /**
     * Sets whether this new translation request will be submitted immediately
     * after this change set is applied. The default value is false.
     * 
     * @param submit    Sets whether this new translation request will be submitted
     *                  immediately.
     * @return  This object.
     */
    public DocumentTranslationRequestDataChangeSet setSubmit(boolean submit) {
        this.submit = submit;
        return this;
    }

    /**
     * Returns whether this translation request will be submitted immediately
     * after this change set is applied.
     * 
     * @return  Whether this translation request will be submitted immediately.
     */
    public boolean isSubmit() {
        return submit;
    }
}
