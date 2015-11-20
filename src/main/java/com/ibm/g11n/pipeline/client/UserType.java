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

/**
 * Service instance user types.
 * 
 * @author Yoshito Umaoka
 */
public enum UserType {
    /**
     * Administrator of a service instance.
     * An administrator can create, read, update and delete
     * bundles and users in the service instance.
     */
    ADMINISTRATOR,
    /**
     * Translator. A translator can read bundles and update
     * translations. This user type is designed for allowing
     * external translators to edit translation contents.
     */
    TRANSLATOR,
    /**
     * Reader. A reader can read resource strings and access
     * a list of available languages in a bundle. This user
     * type is designed for embedding service account information
     * in distributed or web client based applications.
     */
    READER
}
