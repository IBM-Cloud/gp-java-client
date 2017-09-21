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
 * Translation request status enum.
 * 
 * @author yoshito_umaoka
 */
public enum TranslationRequestStatus {
    /**
     * Draft status. The translation request is not yet submitted for
     * professional translation post editing service. Globalization Pipeline
     * user can edit the contents of the translation request or delete the
     * request in this state.
     */
    DRAFT,
    /**
     * Submitted status. The translation request is submitted by user
     * for professional translation post editing service. Globalization Pipeline
     * user cannot modify the contents of the translation request in this state.
     */
    SUBMITTED,
    /**
     * Started status. The professional translation post editing service
     * provider acknowledged the translation request and started working on
     * the request. Globalization Pipeline user cannot modify the contents
     * of the translation request in this state.
     */
    STARTED,
    /**
     * Translated status. The professional translation post editing service
     * provider finished editing the translation. Globalization Pipeline user
     * cannot modify the contents of the translation request in this state.
     */
    TRANSLATED,
    /**
     * Merged status. The final translation results from the professional translation
     * post editing provider were merged to the original Globalization Pipeline
     * bundle. Globalization Pipeline user can modify only specific field(s) such
     * as metadata field in this state.
     */
    MERGED,
    /**
     * Cancelled status. The translation request is cancelled. Globalization Pipeline
     * service or assigned professional translation post editing service provider may
     * cancel a translation request when the service or the provider cannot handle
     * the request for some reasons. Globalization Pipeline user cannot modify
     * the contents of the translation request in this state, but can delete it.
     */
    CANCELLED,
    /**
     * Unknown status. This is a special status only used when Globalization Pipeline
     * service returns a status not supported by this SDK. This status should not be
     * used for updating an existing translation request.
     */
    UNKNOWN;
}
