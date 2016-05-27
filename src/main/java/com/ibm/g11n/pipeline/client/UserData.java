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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * <code>UserData</code> provides read access to service user's
 * properties.
 * 
 * @author Yoshito Umaoka
 */
public abstract class UserData {

    private final UserType type;
    private final String id;
    private final boolean serviceManaged;
    private final String updatedBy;
    private final Date updatedAt;

    /**
     * The constant used for all bundles used in per bundle access
     * control.
     * 
     * @see #getBundles()
     */
    public static final Set<String> ALL_BUNDLES = Collections.singleton("*");

    /**
     * Protected constructor for a subclass extending <code>UserData</code>.
     * 
     * @param type              The user type.
     * @param id                The user ID.
     * @param serviceManaged    If this user is managed by Cloud Foundry.
     * @param updatedBy         The last user updated this user's properties.
     * @param updatedAt         The last date when this user's properties were updated.
     */
    protected UserData(UserType type, String id, boolean serviceManaged,
            String updatedBy, Date updatedAt) {
        this.type = type;
        this.id = id;
        this.serviceManaged = serviceManaged;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the user type.
     * 
     * @return The user type.
     */
    public UserType getType() {
        return type;
    }

    /**
     * Returns the user ID.
     * 
     * @return The user ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns if this user is managed by Cloud Foundry.
     * <p>
     * Note: A user created by Cloud Foundry cannot be deleted by a service user
     * using the client SDK.
     * 
     * @return <code>true</code> if this user is managed by Cloud Foundry.
     */
    public boolean isServiceManaged() {
        return serviceManaged;
    }

    /**
     * Returns the last user updated this user's properties.
     * 
     * @return The last user updated this user's properties.
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Returns the last date when this user's properties were updated.
     * 
     * @return The last date when this user's properties were updated.
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the user's password.
     * <p>
     * Note: This method returns non-null value only when this object
     * is returned by operations 1) creating a new user 2) reset password.
     * 
     * @return The user's password.
     * @see ServiceClient#createUser(NewUserData)
     * @see ServiceClient#updateUser(String, UserDataChangeSet, boolean)
     */
    public abstract String getPassword();

    /**
     * Returns the user's display name.
     * 
     * @return The user's display name.
     */
    public abstract String getDisplayName();

    /**
     * Returns the comment.
     * 
     * @return The comment.
     */
    public abstract String getComment();

    /**
     * Returns a set of bundle IDs accessible by the user.
     * <p>
     * This method returns null when user type is
     * {@link UserType#ADMINISTRATOR ADMINISTRATOR}.
     * If user type is {@link UserType#TRANSLATOR TRANSLATOR}
     * or {@link UserType#READER READER}, the value null
     * indicates the user has no access to all bundles.
     * 
     * @return A set of bundle IDs accessible by the user.
     * @see #ALL_BUNDLES
     */
    public abstract Set<String> getBundles();

    /**
     * Returns the arbitrary metadata represented by string key-value pairs.
     * 
     * @return The arbitrary metadata represented by string key-value pairs.
     */
    public abstract Map<String, String> getMetadata();

    /**
     * Returns the user's external ID.
     * 
     * @return The user's external ID.
     */
    public abstract String getExternalId();
}
