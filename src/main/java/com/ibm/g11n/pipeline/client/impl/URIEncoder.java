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

/**
 * URI encode utility
 * 
 * @author Yoshito Umaoka
 */
public class URIEncoder {

    private static byte[] URI_CHAR7_TABLE = {
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,1,0,0,1,0,1,1,1,1,1,1,1,1,1,0,
        1,1,1,1,1,1,1,1,1,1,1,1,0,1,0,0,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,
        0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,0,
    };

    /**
     * Encode a single URI path segment
     * 
     * @param s A single path segment
     * @return An encoded path segment
     */
    public static String encodePathSegment(String s) {
        if (s == null) {
            return null;
        }

        int idx = 0;
        while (idx < s.length()) {
            char ch = s.charAt(idx);
            if (ch >= 0x80 || URI_CHAR7_TABLE[ch] == 0) {
                // URI path segment unsafe
                break;
            }
            idx++;
        }
        if (idx == s.length()) {
            // Nothing to encode
            return s;
        }

        StringBuilder encoded = new StringBuilder();
        encoded.append(s, 0, idx);

        // Use percent encoding for path unsafe characters
        while (idx < s.length()) {
            // Use code point, instead of char here
            int cp = s.codePointAt(idx);
            if (cp < 0x80) {
                if (URI_CHAR7_TABLE[cp] == 0) {
                    appendPercentHex(encoded, (byte)cp);
                } else {
                    encoded.append((char)cp);
                }
            } else if (cp < 0x0800) {
                appendPercentHex(encoded, (byte)(0xC0 | (cp >>> 6)));
                appendPercentHex(encoded, (byte)(0x80 | (cp & 0x3F)));
            } else if (cp < 0x10000) {
                if (cp >= 0xD800 && cp <= 0xDFFF) {
                    // Standalone surrogate??
                    throw new IllegalArgumentException("Standalone surrogate");
                }
                appendPercentHex(encoded, (byte)(0xE0 | (cp >>> 12)));
                appendPercentHex(encoded, (byte)(0x80 | ((cp >>> 6) & 0x3F)));
                appendPercentHex(encoded, (byte)(0x80 | (cp & 0x3F)));
            } else if (cp < 0x110000) {
                appendPercentHex(encoded, (byte)(0xF0 | (cp >>> 18)));
                appendPercentHex(encoded, (byte)(0x80 | ((cp >>> 12) & 0x3F)));
                appendPercentHex(encoded, (byte)(0x80 | ((cp >>> 6) & 0x3F)));
                appendPercentHex(encoded, (byte)(0x80 | (cp & 0x3F)));
            } else {
                // Out of Unicode definition
                throw new IllegalArgumentException("Code point out of Unicode definition");
            }
            idx += Character.charCount(cp);
        }

        return encoded.toString();
    }

    private static char[] HEX_DIGITS = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    private static void appendPercentHex(StringBuilder appendTo, byte b) {
        appendTo
            .append('%')
            .append(HEX_DIGITS[(b & 0xFF) >>> 4])
            .append(HEX_DIGITS[b & 0x0F]);
    }
}
