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
     * user can modify the contents of the translation request.
     */
    DRAFT,
    /**
     * Submitted status. The translation request is submitted by user
     * for professional translation post editing service. At this point,
     * Globalization Pipeline user cannot modify the contents of the translation
     * request.
     */
    SUBMITTED,
    /**
     * Started status. The professional translation post editing service
     * provider acknowledged the translation request and started working on
     * the request.
     */
    STARTED,
    /**
     * Translated status. The professional translation post editing service
     * provider finished editing the translation.
     */
    TRANSLATED,
    /**
     * Merged status. The final translation results from the professional translation
     * post editing provider were merged to the original Globalization Pipeline
     * bundle.
     */
    MERGED;
}
