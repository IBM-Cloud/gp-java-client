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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import org.junit.Before;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.g11n.pipeline.iam.TokenManagerException;

/**
 * ServiceClient test base class.
 * 
 * @author Yoshito Umaoka
 */
public abstract class AbstractServiceClientTest {
    protected static ServiceClient client = null;
    protected static ServiceAccount account = null;

    // average elapsed time for getBundleIds() - default 1 sec
    protected static long unitTime = 1000;


    static {
        Properties testProps = new Properties();
        String testPropPath = System.getProperty("TEST_PROPERTIES", "src/test/resources/com/ibm/g11n/pipeline/client/test.properties");
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

        String url = testProps.getProperty(ServiceAccount.GP_URL);
        String instanceId = testProps.getProperty(ServiceAccount.GP_INSTANCE_ID);
        String userId = testProps.getProperty(ServiceAccount.GP_USER_ID);
        String password = testProps.getProperty(ServiceAccount.GP_PASSWORD);
        String iamApiKey=testProps.getProperty(ServiceAccount.GP_IAM_API_KEY);
        String iamBearerToken=testProps.getProperty(ServiceAccount.GP_IAM_BEARER_TOKEN);
        String iamEndpoint=testProps.getProperty(ServiceAccount.GP_IAM_ENDPOINT);

        ServiceAccount tmpAccount = ServiceAccount.getInstance(url, instanceId, userId, password,iamApiKey,iamBearerToken,iamEndpoint);
        if (tmpAccount!=null) {
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

    public static String getUpdatedByValue() throws TokenManagerException {
        if (account == null) {
            return null;
        }
        return "(" + account.getInstanceId() + ")" + (account.isIamEnabled()?getIamUser(account.getIamToken()):account.getUserId());
    }
    
    private static String getIamUser(String iamToken) {
        String[] parts = iamToken.split("\\.");
        String encoded = parts[1];
        String jsonString = new String(BaseEncoding.base64().decode(encoded), StandardCharsets.UTF_8);
        JsonObject token = new JsonParser().parse(jsonString)
                .getAsJsonObject();        
        return token.get("iam_id").getAsString();
    }

    public static long getUnitTime() {
        return unitTime;
    }

    /*
     * Checks if the given date is within the plus/minus specified seconds.
     */
    public static boolean isRecent(Date d, int seconds) {
        long delta = System.currentTimeMillis() - d.getTime();
        return Math.abs(delta) < 1000 * seconds;
    }

    public static boolean isRecent(Date d) {
        // within 30 seconds
        return isRecent(d, 30);
    }
}
