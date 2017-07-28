/*  
 * Copyright IBM Corp. 2016
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
package com.ibm.g11n.pipeline.client.rb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle.Control;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ibm.g11n.pipeline.client.AbstractServiceClientTest;
import com.ibm.g11n.pipeline.client.NewBundleData;
import com.ibm.g11n.pipeline.client.ServiceClient;
import com.ibm.g11n.pipeline.client.ServiceException;
import com.ibm.g11n.pipeline.client.rb.CloudResourceBundleControl.LookupMode;

/**
 * Test cases for CloudResourceBundleControl.
 * 
 * @author Yoshito Umaoka
 */
public class CloudResourceBundleControlTest extends AbstractServiceClientTest {

    static class TestBundle {
        String id;
        String source;
        Set<String> targets;
        Map<String, String> strings;

        TestBundle(String id, String source, String[] targetArray, String[][] stringsArray) {
            this.id = id;
            this.source = source;
            if (targetArray != null) {
                targets = new HashSet<String>(Arrays.asList(targetArray));
            }
            if (stringsArray != null) {
                strings = new HashMap<>(stringsArray.length);
                for (String[] singleResData : stringsArray) {
                    strings.put(singleResData[0], singleResData[1]);
                }
            }
        }

        void create(ServiceClient client) throws ServiceException {
            NewBundleData data = new NewBundleData(source);
            data.setTargetLanguages(targets);
            client.createBundle(id, data);
            if (strings != null) {
                client.uploadResourceStrings(id, source, strings);
            }
        }

        void delete(ServiceClient client) throws ServiceException {
            client.deleteBundle(id);
        }
    }

    static final String TEST_ID1 = "com.ibm.g11n.pipeline.client.rb.Test1";
    static final String TEST_SOURCE1 = "en";

    static final String[][] TEST_STRS1 = {
        {"tools_import", "Import"},
        {"tools_export", "Export"},
        {"tools_convert", "Convert"},
        {"remote_only", "Remote Only"}
    };
    static final String[] TEST_LANGS1 = {
        "fr",   // remote only, "es" is local only
        "ja",   // both remote and local
        "zxx",  // for testing missing translations
    };


    static final TestBundle[] TEST_BUNDLES = {
        new TestBundle(TEST_ID1, TEST_SOURCE1, TEST_LANGS1, TEST_STRS1),
        new TestBundle("my.Test1", "en", TEST_LANGS1, TEST_STRS1),
    };

    private static boolean initialized = false;

    @BeforeClass
    public static void createTestBundles() throws ServiceException {
        if (client == null) {
            return;
        }

        cleanupTestBundles();

        for (TestBundle bundle : TEST_BUNDLES) {
            bundle.create(client);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println("Interrupted - createTestBundles()");
        }
        initialized = true;
    }

    @AfterClass
    public static void cleanupTestBundles() throws ServiceException {
        if (client == null) {
            return;
        }

        Set<String> bundleIds = client.getBundleIds();
        for (TestBundle bundle : TEST_BUNDLES) {
            if (bundleIds.contains(bundle.id)) {
                bundle.delete(client);
            }
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void checkInitialized() {
        assumeTrue(initialized);
    }

    @Test
    public void test_Remote_English() {
        TestBundle testBundle = TEST_BUNDLES[0];
        String bundleId = testBundle.id;
        CloudResourceBundleControl ctrl = CloudResourceBundleControl.getInstance(
                account, Control.TTL_DONT_CACHE, null, null, null, null);

        ResourceBundle bundle = ResourceBundle.getBundle(bundleId, new Locale("en"), ctrl);

        for (Entry<String, String> entry : testBundle.strings.entrySet()) {
            String key = entry.getKey();
            String resValue = bundle.getString(key);
            assertEquals("resource value for " + key, entry.getValue(), resValue);
        }
    }

    @Test
    public void test_Remote_French() {
        TestBundle testBundle = TEST_BUNDLES[0];
        String bundleId = testBundle.id;
        CloudResourceBundleControl ctrl = CloudResourceBundleControl.getInstance(
                account, Control.TTL_DONT_CACHE, null, null, null, null);

        ResourceBundle bundle = ResourceBundle.getBundle(bundleId, new Locale("fr"), ctrl);

        for (Entry<String, String> entry : testBundle.strings.entrySet()) {
            String key = entry.getKey();
            try {
                String frVal = bundle.getString(key);
                assertNotNull("Test1 fr " + key + " value", frVal);
            } catch (MissingResourceException e) {
                fail("Test1 fr should contain " + key);
            }
        }
    }

    @Test
    public void test_LocalOnly_KeyOnlyInLocalEN() {
        TestBundle testBundle = TEST_BUNDLES[0];
        String bundleId = testBundle.id;
        CloudResourceBundleControl ctrl = CloudResourceBundleControl.getInstance(
                account, Control.TTL_DONT_CACHE, null, null, null, LookupMode.LOCAL_ONLY);

        ResourceBundle bundle = ResourceBundle.getBundle(bundleId, new Locale("en"), ctrl);
        try {
            String localOnly = bundle.getString("local_only");
            assertNotNull("Local Test1 en key local_only value", localOnly);
        } catch (MissingResourceException e) {
            fail("Local Test1 en should contain key local_only");
        }
    }

    @Test
    public void test_RemoteOnly_KeyOnlyInLocalEN() {
        TestBundle testBundle = TEST_BUNDLES[0];
        String bundleId = testBundle.id;
        CloudResourceBundleControl ctrl = CloudResourceBundleControl.getInstance(
                account, Control.TTL_DONT_CACHE, null, null, null, LookupMode.REMOTE_ONLY);

        ResourceBundle bundle = ResourceBundle.getBundle(bundleId, new Locale("en"), ctrl);

        expectedException.expect(MissingResourceException.class);
        String s = bundle.getString("local_only");
        System.out.println(s);
    }

    @Test
    public void test_LocalThenRemote_KeyOnlyInLocalJA() {
        TestBundle testBundle = TEST_BUNDLES[0];
        String bundleId = testBundle.id;
        CloudResourceBundleControl ctrl = CloudResourceBundleControl.getInstance(
                account, Control.TTL_DONT_CACHE, null, null, null, LookupMode.LOCAL_THEN_REMOTE);

        ResourceBundle bundle = ResourceBundle.getBundle(bundleId, new Locale("ja"), ctrl);
        try {
            String localOnly = bundle.getString("local_only");
            assertNotNull("Local Test1 ja key local_only value", localOnly);
        } catch (MissingResourceException e) {
            fail("Local Test1 ja should contain key local_only");
        }
    }

    @Test
    public void test_LocalThenRemote_KeyOnlyInRemoteJA() {
        TestBundle testBundle = TEST_BUNDLES[0];
        String bundleId = testBundle.id;
        CloudResourceBundleControl ctrl = CloudResourceBundleControl.getInstance(
                account, Control.TTL_DONT_CACHE, null, null, null, LookupMode.LOCAL_THEN_REMOTE);

        ResourceBundle bundle = ResourceBundle.getBundle(bundleId, new Locale("ja"), ctrl);

        // "remote_only" is only in remote bundle. LOCAL_THEN_REMOTE should resolve
        // the local ja bundle, therefore, resource "remote_only" does not exist.
        expectedException.expect(MissingResourceException.class);
        bundle.getString("remote_only");
    }
}
