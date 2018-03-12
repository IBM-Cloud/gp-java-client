/*  
 * Copyright IBM Corp. 2018
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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jugudanniesundar
 *
 */
public class AbstractServiceClientDocumentTest extends AbstractServiceClientTest {
    protected static final String DOCUMENT_PREFIX = "junit-document-";

    public void cleanupDocuments() throws ServiceException {
        if (client != null) {
            Set<String> documentIds = client.getDocumentIds(DocumentType.HTML);
            for (String documentId : documentIds) {
                if (isTestDocumentId(documentId)) {
                    client.deleteDocument(documentId, DocumentType.HTML);
                }
            }
            documentIds = client.getDocumentIds(DocumentType.MD);
            for (String documentId : documentIds) {
                if (isTestDocumentId(documentId)) {
                    client.deleteDocument(documentId, DocumentType.MD);
                }
            }
        }
    }

    //
    // static test utility methods
    //

    protected static String testDocumentId(String id) {
        return DOCUMENT_PREFIX + id;
    }

    protected static boolean isTestDocumentId(String documentId) {
        return documentId.startsWith(DOCUMENT_PREFIX);
    }

    protected static void createDocumentWithLanguages(String documentId,
            DocumentType type, String source, String... targets) throws ServiceException {
        Set<String> targetSet = null;
        if (targets != null) {
            targetSet = new HashSet<String>(Arrays.asList(targets));
        }
        createDocument(documentId, type, source, targetSet, null);
    }

    protected static void createDocument(String documentId, DocumentType type, String source,
            Set<String> targets, List<String> notes) throws ServiceException {
        NewDocumentData newDocumentData = new NewDocumentData(source);
        if (targets != null) {
            newDocumentData.setTargetLanguages(targets);
        }
        if (notes != null) {
            newDocumentData.setNotes(notes);
        }
        client.createDocument(documentId, type, newDocumentData);
    }

    /**
     * Creates a new bundle with initial resource string data
     * 
     * @param documentId  The document id.
     * @param type      The document type
     * @param srcLang   The source language of the bundle.
     * @param trgLangs  The target languages separated by comman e.g. "fr,de", or null.
     * @param notes     The list of bundle comments, or null.
     * @param file      The file containing document content in source language
     * @throws ServiceException
     */
    protected static void createDocumentWithContent(String documentId, DocumentType type, String srcLang, String trgLangs,
            List<String> notes, File file) throws ServiceException {
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
        createDocument(documentId, type, srcLang, trgLangSet, notes);
        if (file != null) {
            client.updateDocumentContent(documentId, type, srcLang, file);
        }
    }


}
