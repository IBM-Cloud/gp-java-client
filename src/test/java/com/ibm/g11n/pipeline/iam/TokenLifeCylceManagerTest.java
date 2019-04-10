/*  
 * Copyright IBM Corp. 2019
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
package com.ibm.g11n.pipeline.iam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Siddharth Jain
 *
 */
public class TokenLifeCylceManagerTest {
    static String iamApiEndpoint;
    static String apiKey;
    static String apiKey1;
    static String iamToken;
    private static final String dummyIamApiEndpoint="https://iam.cloud.ibm.com";
    private static final String dummyApiKey = "DUMMY_KEY";

    
    @BeforeClass
    public static void setup() {
         iamApiEndpoint=System.getProperty("iamApi");
         apiKey=System.getProperty("apiKey");
         apiKey1=System.getProperty("apiKey1");

         iamToken=System.getProperty("iamToken","dummy-token");
    }
    
    @Test
    public void testSingleton() {
        assertTrue(
                "Single instance of TokenLifeCylceManager should be maintained for a unique pair of IAM API endpoint and IAM API key.",
                TokenLifeCylceManager.getInstance(dummyIamApiEndpoint,
                        dummyApiKey) == TokenLifeCylceManager
                                .getInstance(dummyIamApiEndpoint, dummyApiKey));
    }
    
    @Test
    public void testTokenExpiryThresholdValues() {
        assumeTrue("IAM Endpoint and API Key are available",
                iamApiEndpoint != null && apiKey != null && apiKey1 != null);
        try {
            System.setProperty(
                    TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY,
                    "0");
            TokenLifeCylceManager.getInstance(iamApiEndpoint, apiKey1);
            fail("0% is an invalid value for IAM_TOKEN_EXPIRY_THRESHOLD");
        } catch (IllegalArgumentException ex) {
            System.clearProperty(
                    TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY);
        }
        try {
            System.setProperty(
                    TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY,
                    "1");
            TokenLifeCylceManager.getInstance(iamApiEndpoint, apiKey1);
            fail("100% is an invalid value for IAM_TOKEN_EXPIRY_THRESHOLD");
        } catch (IllegalArgumentException ex) {
            System.clearProperty(
                    TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY);
        }
        try {
            System.setProperty(
                    TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY,
                    "0.1");
            TokenLifeCylceManager.getInstance(iamApiEndpoint, apiKey1);
            fail("10% is an invalid value for IAM_TOKEN_EXPIRY_THRESHOLD");
        } catch (IllegalArgumentException ex) {
            System.clearProperty(
                    TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY);
        }
        System.setProperty(
                TokenLifeCylceManager.IAM_TOKEN_EXPIRY_THRESHOLD_PROP_KEY,
                "0.5");
        TokenLifeCylceManager.getInstance(iamApiEndpoint, apiKey1);

    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyIamApiEndpointJson() {
        TokenLifeCylceManager.getInstance(
                "{'apikey':'"+dummyApiKey+"','iam_endpoint':''}");
    }
    @Test(expected=IllegalArgumentException.class)
    public void testNullIamApiEndpointJson() {
        TokenLifeCylceManager.getInstance(
                "{'apikey':'"+dummyApiKey+"','iam_endpoint': null}");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyIamApiKeyJson() {
        TokenLifeCylceManager.getInstance(
                "{'apikey':'','iam_endpoint':'"+dummyIamApiEndpoint+"'}");
    }
    @Test(expected=IllegalArgumentException.class)
    public void testNullIamApiKeyJson() {
        TokenLifeCylceManager.getInstance(
                "{'apikey':null,'iam_endpoint':'"+dummyIamApiEndpoint+"'}");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullIamApiEndpoint() {
        TokenLifeCylceManager.getInstance(null,dummyApiKey);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyIamApiEndpoint() {
        TokenLifeCylceManager.getInstance("",dummyApiKey);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullApiKey() {
        TokenLifeCylceManager.getInstance(dummyIamApiEndpoint,null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyApiKey() {
        TokenLifeCylceManager.getInstance(dummyIamApiEndpoint,"");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullParams() {
        TokenLifeCylceManager.getInstance(null,null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyParams() {
        TokenLifeCylceManager.getInstance("","");
    }
    
    @Test
    public void testApiKeySingleCall() throws TokenManagerException {
        assumeTrue("IAM Endpoint and API Key are available",iamApiEndpoint!=null && apiKey!=null);
        TokenLifeCylceManager manager=TokenLifeCylceManager.getInstance(iamApiEndpoint,
                apiKey) ;
        String token=manager.getToken();
        assertNotNull("IAM token should not be empty.", token);
    }
    
    @Test
    public void testTokenCaching() throws TokenManagerException {
        assumeTrue("IAM Endpoint and API Key are available",iamApiEndpoint!=null && apiKey!=null);
        String token=null;
        TokenLifeCylceManager manager=TokenLifeCylceManager.getInstance(iamApiEndpoint,
                apiKey) ;
        for(int i=0;i<4;i++) {
            if(token==null) {
                token=manager.getToken();
            }
            else {
                assertEquals("Cached token should be returned on multiple calls", token,manager.getToken());
            }
        }
    }
    
    @Test
    public void testTokenCachingMultiThreadCalls() throws TokenManagerException, InterruptedException {
        assumeTrue("IAM Endpoint and API Key are available",iamApiEndpoint!=null && apiKey!=null);
        final int threads=30;
        final CyclicBarrier barrier=new CyclicBarrier(threads);
        final Set<String> tokens=Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
        List<Thread> threadStore=new ArrayList<>();
        for(int i=0;i<threads;i++) {
            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TokenLifeCylceManager manager=TokenLifeCylceManager.getInstance(iamApiEndpoint,
                                apiKey) ;
                        barrier.await();
                        for(int i=0;i<10;i++) {
                            tokens.add(manager.getToken());  
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    } catch (TokenManagerException e) {
                        e.printStackTrace();
                    }
                    
                }
            });
            threadStore.add(t);
            t.start();
        }
        for(Thread t:threadStore) {
            t.join();
        }
        assertTrue("There should only be one token fetch operation from IAM irrespective of the number of calls from n threads for the same pair of iam api key and endpoint",tokens.size()==1);
    }
    
}
