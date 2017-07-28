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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ServiceClient bundle test base class.
 * 
 * @author yoshito_umaoka
 */
public class AbstractServiceClientBundleTest extends AbstractServiceClientTest {
    protected static final String BUNDLE_PREFIX = "junit-bundle-";

    public void cleanupBundles() throws ServiceException {
        if (client != null) {
            Set<String> bundleIds = client.getBundleIds();
            for (String bundleId : bundleIds) {
                if (isTestBundleId(bundleId)) {
                    client.deleteBundle(bundleId);
                }
            }
        }
    }

    //
    // static test utility methods
    //

    protected static String testBundleId(String id) {
        return BUNDLE_PREFIX + id;
    }

    protected static boolean isTestBundleId(String bundleId) {
        return bundleId.startsWith(BUNDLE_PREFIX);
    }

    protected static void createBundleWithLanguages(String bundleId,
            String source, String... targets) throws ServiceException {
        Set<String> targetSet = null;
        if (targets != null) {
            targetSet = new HashSet<String>(Arrays.asList(targets));
        }
        createBundle(bundleId, source, targetSet, null);
    }

    protected static void createBundleWithLanguages(String bundleId,
            String source, Set<String> targets) throws ServiceException {
        createBundle(bundleId, source, targets, null);
    }

    protected static void createBundle(String bundleId, String source,
            Set<String> targets, List<String> notes) throws ServiceException {
        NewBundleData newBundleData = new NewBundleData(source);
        if (targets != null) {
            newBundleData.setTargetLanguages(targets);
        }
        if (notes != null) {
            newBundleData.setNotes(notes);
        }
        client.createBundle(bundleId, newBundleData);
    }

    /**
     * Creates a new bundle with initial resource string data
     * 
     * @param bundleId  The bundle ID.
     * @param srcLang   The source language of the bundle.
     * @param trgLangs  The target languages separated by comman e.g. "fr,de", or null.
     * @param notes     The list of bundle comments, or null.
     * @param resStrings    The resource key/value pairs in the source language, or null.
     * @throws ServiceException
     */
    protected static void createBundleWithStrings(String bundleId, String srcLang, String trgLangs,
            List<String> notes, String[][] resStrings) throws ServiceException {
        Set<String> trgLangSet = null;
        if (trgLangs != null) {
            String[] langs = trgLangs.split(",");
            for (String lang : langs) {
                lang = lang.trim();
                if (!lang.isEmpty()) {
                    if (trgLangSet == null) {
                        trgLangSet = new HashSet<>();
                    }
                    trgLangSet.add(lang);
                }
            }
        }
        createBundle(bundleId, srcLang, trgLangSet, notes);
        if (resStrings != null) {
            Map<String, String> resMap = new HashMap<>();
            for (String[] kv : resStrings) {
                if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
                    resMap.put(kv[0], kv[1]);
                }
            }
            if (!resMap.isEmpty()) {
                client.uploadResourceStrings(bundleId, srcLang, resMap);
            }
        }
    }
}
