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
package com.ibm.g11n.pipeline.client;

import static org.junit.Assume.assumeTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.junit.Before;

/**
 * ServiceClient test base class.
 * 
 * @author Yoshito Umaoka
 */
public abstract class AbstractServiceClientTest {
    protected static ServiceClient client = null;
    protected static ServiceAccount account = null;

    // average elapsed time for getBundleIds() - default 2 sec
    protected static long unitTime = 2000;


    static {
        Properties testProps = new Properties();
        String testPropPath = System.getProperty("TEST_PROPERTIES", "test.properties");
        if (testPropPath != null) {
            try (FileInputStream fis = new FileInputStream(testPropPath)) {
                testProps.load(fis);
                System.out.println("Loading test properties from " + testPropPath);
            } catch (IOException e) {
                // Ignore
            }
        }
        // Overwrite loaded properties (if any) with env vars.
        // i.e. an env var wins over a value in the properties file.
        testProps.putAll(System.getenv());

        String url = testProps.getProperty("GP_URL");
        String instanceId = testProps.getProperty("GP_INSTANCE_ID");
        String userId = testProps.getProperty("GP_USER_ID");
        String password = testProps.getProperty("GP_PASSWORD");

        if (url != null && instanceId != null && userId != null && password != null) {
            ServiceAccount tmpAccount = ServiceAccount.getInstance(url, instanceId, userId, password);
            ServiceClient tmpClient = ServiceClient.getInstance(tmpAccount);

            // Make sure it is able to access the service, and also measure response time
            try {
                // warm up
                for (int i = 0; i < 2; i++) {
                    tmpClient.getBundleIds();
                }

                // measure response time of getBundleIds()
                long time = System.currentTimeMillis();
                int numIteration = 5;
                for (int i = 0; i < numIteration; i++) {
                    tmpClient.getBundleIds();
                }
                unitTime = (System.currentTimeMillis() - time) / numIteration;
                client = tmpClient;
                account = tmpAccount;
            } catch (ServiceException e) {
                // Failed to call the REST service - disable client
                client = null;
                account = null;
            }
        }
    }

    @Before
    public void checkClient() {
        assumeTrue(client != null);
    }

    public static String getUpdatedByValue() {
        if (account == null) {
            return null;
        }
        return "(" + account.getInstanceId() + ")" + account.getUserId();
    }

    /*
     * Checks if the given date is past and within unitTime*units.
     * unitTime is average elapsed time of getBundleIds() call. 
     */
    public static boolean isRecent(Date d, int units) {
        long delta = System.currentTimeMillis() - d.getTime();
        return delta > 0 && delta < unitTime * units;
    }

    public static boolean isRecent(Date d) {
        return isRecent(d, 20);
    }
}
