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
 * The base class of IBM Globalization Pipeline's exception.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * No-arg constructor.
     */
    public ServiceException() {
        super();
    }

    /**
     * Constructor with detailed message.
     * 
     * @param message   The detail message.
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructor with detailed message and cause.
     * 
     * @param message   The detail message.
     * @param cause     The cause.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     * 
     * @param cause     The cause.
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
}
