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
 * <code>NewDocumentTranslationRequestData</code> is used for specifying a new document
 * translation request.
 * 
 * @author jugudanniesundar
 */
public class NewDocumentTranslationRequestData {
    private final Map<String, Map<String, Set<String>>> targetLanguagesMap;

    private String partner = "IBM";
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
     * 
     * @param targetLanguagesMap   A map containing target languages indexed by document
     *                                  type and ids. This constructor adopts the input map without
     *                                  creating a safe copy.
     * @throws NullPointerException When the input <code>targetLanguagesMap</code> is null.
     */
    public NewDocumentTranslationRequestData(Map<String, Map<String, Set<String>>> targetLanguagesMap) {
        // TODO - check empty map?
        if (targetLanguagesMap == null) {
            throw new NullPointerException("The input map is null.");
        }
        this.targetLanguagesMap = targetLanguagesMap;
    }

    /**
     * Returns a map containing target languages indexed by document type and ids. This method returns
     * a map held by this object without creating a safe copy. This method does not return
     * null.
     * 
     * @return  A map containing target languages indexed by document ids.
     */
    public Map<String, Map<String, Set<String>>> getTargetLanguagesMap() {
        return targetLanguagesMap;
    }

    /**
     * Sets the translation post editing service provider's id.
     * If this setter is not used, the default value "IBM" will be used.
     * 
     * @param partner   The translation post editing service provider's id.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setPartner(String partner) {
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
     * Sets the name of this translation request. A non-null name must be set if this object
     * is created for submitting a new request.
     * 
     * @param name  The name of this translation request.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the name of this translation request.
     * 
     * @return  The name of this translation request.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the organization name of the requester. Optional.
     * 
     * @param organization  The organization name of the requester.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    /**
     * Returns the organization name of the requester.
     * 
     * @return  The organization name of the requester.
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Sets a list of the request's e-mail addresses.  A non-empty e-mail addresses
     * must be set if this object is created for submitting a new request.
     * 
     * @param emails    A list of the requester's e-mail addresses.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setEmails(List<String> emails) {
        this.emails = emails;
        return this;
    }

    /**
     * Returns a list of the requester's e-mail addresses. When the translation request's
     * status is changed, an e-mail notification will be sent to the addresses.
     * 
     * @return  A list of the requester's e-mail addresses.
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Sets a list of the requester's contact phones. Optional.
     * 
     * @param phones    A list of the requester's contact phones.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setPhones(List<String> phones) {
        this.phones = phones;
        return this;
    }

    /**
     * Returns a list of the requester's contact phones.
     * 
     * @return  A list of the requester's contact phones.
     */
    public List<String> getPhones() {
        return phones;
    }

    /**
     * Sets a set of the industry domains representing the contents. Optional.
     * @param domains   A set of the industry domains representing the contents.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setDomains(EnumSet<IndustryDomain> domains) {
        this.domains = domains;
        return this;
    }

    /**
     * Returns a set of the industry domains representing the contents.
     * 
     * @return  A set of industry domains representing the contents.
     */
    public EnumSet<IndustryDomain> getDomains() {
        return domains;
    }

    /**
     * Sets the translation notes. Optional.
     * 
     * @param notes The translation notes.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setNotes(List<String> notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Returns the translation notes.
     * 
     * @return  The translation notes.
     */
    public List<String> getNotes() {
        return notes;
    }

    /**
     * Sets a map containing the metadata of this translation request represented
     * by key-value pairs. Optional.
     * 
     * @param metadata  A map containing the metadata of this translation request represented
     * by key-value pairs.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns a map containing the metadata of this translation request represented
     * by key-value pairs.
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
     * @param partnerParameters A map containing key-value pairs specifying configuration parameters.
     * @return This object.
     */
    public NewDocumentTranslationRequestData setPartnerParameters(Map<String, String> partnerParameters) {
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
     * after creation. The default value is false.
     * 
     * @param submit    Sets whether this new translation request will be submitted
     *                  immediately.
     * @return  This object.
     */
    public NewDocumentTranslationRequestData setSubmit(boolean submit) {
        this.submit = submit;
        return this;
    }

    /**
     * Returns whether this translation request will be submitted immediately
     * after creation.
     * 
     * @return  Whether this translation request will be submitted immediately.
     */
    public boolean isSubmit() {
        return submit;
    }
}
