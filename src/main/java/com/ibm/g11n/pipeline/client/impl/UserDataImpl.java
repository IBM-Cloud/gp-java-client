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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.ibm.g11n.pipeline.client.UserData;
import com.ibm.g11n.pipeline.client.UserType;

/**
 * UserData implementation class.
 * 
 * @author Yoshito Umaoka
 */
class UserDataImpl extends UserData {

    private final RestUser user;

    public UserDataImpl(RestUser user) {
        super(user.getType(), user.getId(), user.isServiceManaged(),
                user.getUpdatedBy(), user.getUpdatedAt());
        this.user = user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getDisplayName() {
        return user.getDisplayName();
    }

    @Override
    public String getComment() {
        return user.getComment();
    }

    @Override
    public Set<String> getBundles() {
        Set<String> bundles = user.getBundles();
        if (bundles == null) {
            return null;
        }
        return Collections.unmodifiableSet(bundles);
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = user.getMetadata();
        if (metadata == null) {
            return null;
        }
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String getExternalId() {
       return user.getExternalId();
    }

    /**
     * Data object used for deserializing user data in JSON.
     * 
     * @author Yoshito Umaoka
     */
    static class RestUser extends RestObject {
        private UserType type;
        private String id;
        private String password;
        private String displayName;
        private String comment;
        private Set<String> bundles;
        private Map<String, String> metadata;
        private boolean serviceManaged;
        private String externalId;

        

        public UserType getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public String getPassword() {
            return password;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getComment() {
            return comment;
        }

        public Set<String> getBundles() {
            return bundles;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public boolean isServiceManaged() {
            return serviceManaged;
        }

        public String getExternalId() {
            return externalId;
        }
    }
}
