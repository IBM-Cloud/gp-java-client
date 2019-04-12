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

import org.junit.Test;

/**
 * @author Siddharth Jain
 *
 */
public class SimpleTokenManagerTest {
    @Test(expected=IllegalArgumentException.class)
    public void testNullToken() {
        new SimpleTokenManager(null);
    }
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyToken() {
        new SimpleTokenManager("");        
    }
    
    @Test
    public void testValidToken() {
        String validToken="abshg2532udmjehjd732...";
        SimpleTokenManager manager=new SimpleTokenManager(validToken);
        assertEquals("Same token should be returned as the instance was initialized with", validToken,manager.getToken());
    }
}
