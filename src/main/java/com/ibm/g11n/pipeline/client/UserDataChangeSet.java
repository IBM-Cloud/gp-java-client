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
package com.ibm.g11n.pipeline.client;

import java.util.Map;
import java.util.Set;

/**
 * <code>UserDataChangeSet</code> is used for specifying changes
 * of a user's properties.
 * 
 * @author Yoshito Umaoka
 */
public class UserDataChangeSet {
    private String displayName;
    private String comment;
    private Set<String> bundles;
    private Map<String, String> metadata;
    private String externalId;

    /**
     * Constructor, creating an empty change set.
     */
    public UserDataChangeSet() {
    }

    /**
     * Return the new display name.
     * @return The new display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the new display name.
     * @param displayName The new display name.
     * @return This object.
     */
    public UserDataChangeSet setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Returns the new comment.
     * @return The new comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the new comment.
     * @param comment The new comment.
     * @return This object.
     */
    public UserDataChangeSet setComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Returns the updated accessible bundle IDs.
     * @return The updated accessible bundle IDs.
     */
    public Set<String> getBundles() {
        return bundles;
    }

    /**
     * Sets the updated accessible bundle IDs.
     * @param bundles The updated accessible bundle IDs.
     * @return This object.
     */
    public UserDataChangeSet setBundles(Set<String> bundles) {
        this.bundles = bundles;
        return this;
    }

    /**
     * Returns a map containing the new or updated key-value pairs.
     * @return A map containing the new or updated key-value pairs.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets a map containing the new or updated key-value pairs.
     * When a key currently exists in bundle's metadata, the value
     * of the key will be replaced with the new value. When a key
     * currently exists and the new value is empty, the key-value
     * pair will be removed. When a key does not exists, the key-value
     * pair will be added.
     * @param metadata A map containing new or updated key-value pairs.
     * @return This object.
     */
    public UserDataChangeSet setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the new external ID.
     * @return The new external ID.
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets the new external ID.
     * @param externalId The new external ID.
     * @return This object.
     */
    public UserDataChangeSet setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }
}
