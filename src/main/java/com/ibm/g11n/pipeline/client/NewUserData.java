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
 * <code>NewUserData</code> is used for specifying new user's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public class NewUserData {
    private final UserType type;
    private String displayName;
    private String comment;
    private Set<String> bundles;
    private Map<String, String> metadata;
    private String externalId;

    /**
     * Constructor.
     * @param type  The type of the new user.
     */
    public NewUserData(UserType type) {
        this.type = type;
    }

    /**
     * Returns the type of the new user.
     * @return The type of the new user.
     * @see UserType
     */
    public UserType getType() {
        return type;
    }

    /**
     * Returns the display name of the new user.
     * @return The display name of the new user.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the new user. When display name
     * is not empty, Globalization Pipeline service append it
     * to user ID in user name fields, such as updated-by.
     * @param displayName   The displayName of the new user.
     * @return This object.
     */
    public NewUserData setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Returns the comment for the new user.
     * @return The comment for the new user.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment for the new user.
     * @param comment The comment for the new user.
     * @return This object.
     */
    public NewUserData setComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Returns the set of bundles that the new user can access.
     * @return The set of bundles that the new user can access.
     * @see UserData#ALL_BUNDLES
     */
    public Set<String> getBundles() {
        return bundles;
    }

    /**
     * Sets the set of bundle IDs that the new user can access.
     * This setting is only applicable when user type is
     * {@link UserType#TRANSLATOR TRANSLATOR} or {@link UserType#READER READER}.
     * If user type is {@link UserType#TRANSLATOR TRANSLATOR} or
     * {@link UserType#READER READER} and no accessible bundles are set,
     * the new user cannot access any bundles. If you want to allow
     * the new user to access all bundles, use {@link UserData#ALL_BUNDLES ALL_BUNDLES}
     * in this method.
     * @param bundles   The set of bundle IDs that the new user can access.
     * @return This object.
     */
    public NewUserData setBundles(Set<String> bundles) {
        this.bundles = bundles;
        return this;
    }

    /**
     * Returns a map containing the key-value pairs.
     * @return A map containing the key-value pairs.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets arbitrary metadata associated with the new user specified
     * by a map containing string key-value pairs.
     * @param metadata  A map containing string key-value pairs.
     * @return This object.
     */
    public NewUserData setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the external ID of the new user.
     * @return The external ID of the new user.
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets the external ID of the new user. An external ID is not
     * used by Globalization Pipeline service for displaying or
     * identifying a user. This property is reserved for custom applications
     * accessing Globalization Pipeline service programmatically.
     * @param externalId The external ID of the new user.
     * @return This object.
     */
    public NewUserData setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }
}
