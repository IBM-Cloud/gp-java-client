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
package com.ibm.g11n.pipeline.client.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for URIEncoder
 * 
 * @author Yoshito Umaoka
 */
public class URIEncoderTest {

    @Test
    public void encodePathSegmentTest() {
        final String[][] TESTDATA = {
            {"", ""},
            {"abc", "abc"},
            {"hello!", "hello!"},
            {"yoshito@IBM", "yoshito@IBM"},
            {"a b c", "a%20b%20c"},
            {"abc(xyz)", "abc(xyz)"},
            {"x.y%", "x.y%25"},
            {"x=1;y=2", "x=1;y=2"},
            {"x/y", "x%2Fy"},
            {"a^b", "a%5Eb"},
            {"aßc", "a%C3%9Fc"},
            {"года", "%D0%B3%D0%BE%D0%B4%D0%B0"},
            {"あいう", "%E3%81%82%E3%81%84%E3%81%86"},
            {"快晴", "%E5%BF%AB%E6%99%B4"},
            {"U+10000-􀀀", "U+10000-%F4%80%80%80"},
            {"🍧🍩", "%F0%9F%8D%A7%F0%9F%8D%A9"},
        };

        for (String[] data : TESTDATA) {
            String encoded = URIEncoder.encodePathSegment(data[0]);
            assertEquals(data[0] + " should be encoded to ", data[1], encoded);
        }
    }
}
