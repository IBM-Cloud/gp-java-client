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
 * <code>ReviewStatusMetrics</code> represents the review status
 * of resource entries in a language.
 * 
 * @author Yoshito Umaoka
 */
public class ReviewStatusMetrics {
    private final int reviewed;
    private final int notYetReviewed;

    /**
     * Constructs a <code>ReviewStatusMetrics</code>.
     * @param reviewed          The number of entries marked as reviewed.
     * @param notYetReviewed    The number of entries not marked as reviewed.
     */
    public ReviewStatusMetrics(int reviewed, int notYetReviewed) {
        this.reviewed = reviewed;
        this.notYetReviewed = notYetReviewed;
    }

    /**
     * Returns the number of entries marked as reviewed.
     * @return The number of entries marked as reviewed.
     */
    public int getReviewed() {
        return reviewed;
    }

    /**
     * Returns the number of entries not marked as reviewed.
     * @return The number of entries not marked as reviewed.
     */
    public int getNotYetReviewed() {
        return notYetReviewed;
    }
}
