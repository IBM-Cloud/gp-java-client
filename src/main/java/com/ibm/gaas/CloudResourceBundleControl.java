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
package com.ibm.gaas;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.gaas.impl.GaasRestServiceClient;

/**
 * <code>CloudResourceBundleControl</code> is a concrete subclass of {@link Control}.
 * The implementation looks for a project with the specified bundle base name (i.e. baseName argument used in
 * {@link ResourceBundle#getBundle(String, Locale, Control)}) in the IBM Globalization service.
 * If there is no matching project, or there is a matching project but no matching locale is found in the
 * IBM Globlization project, then the implementation uses the standard Java implementation. This implementation
 * allows some bundles such as root bundle stored locally, while other bundles stored in the IBM Globalization
 * service.
 * 
 * @author Yoshito Umaoka
 */
public final class CloudResourceBundleControl extends Control {

    /**
     * Default resource bundle cache expiration time (60000 = 10 minutes)
     */
    public static final long DEFAULT_CACHE_EXPIRATION = 60000L;

    private static final String FORMAT_GAAS_TRANSLATE_CLOUD = "gaas.translate.cloud";
    private static final List<String> FORMAT_LIST =
        Collections.unmodifiableList(Arrays.asList(FORMAT_GAAS_TRANSLATE_CLOUD, "java.class", "java.properties"));

    private static final ConcurrentHashMap<URL, Set<Locale>> SUPPORTED_LOCALES = new ConcurrentHashMap<URL, Set<Locale>>();

    private final ServiceAccount serviceAccount;
    private long ttl;
    private NameMapper nameMapper;

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account and the default cache expiration ({@link #DEFAULT_CACHE_EXPIRATION}).
     * <p>
     * This method is equivalent to <code>getInstance(serviceAccount, DEFAULT_CACHE_EXPIRATION, null)</code>.
     * 
     * @param serviceAccount    The service account.
     * @return  An instance of CloundResourceBundleControl.
     * @throws IllegalArgumentException when serviceAccount is null.
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount) {
        return getInstance(serviceAccount, DEFAULT_CACHE_EXPIRATION, null);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account and the cache expiration. The cache expiration time is in milliseconds
     * and must be positive except for two special values.
     * <p>
     * This method is equivalent to <code>getInstance(serviceAccount, cacheExpiration, null)</code>.
     *
     * @param serviceAccount    The service account.
     * @param cacheExpiration   The cache expiration, see the method description for details.
     * @return  An instance of CloudResourceBundleControl.
     * @throws IllegalArgumentException when <code>serviceAccount</code> is null or <code>cacheExpiration</code>
     * value is illegal.
     * @see CloudResourceBundleControl#getInstance(ServiceAccount, long, NameMapper)
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount, long cacheExpiration) {
        return getInstance(serviceAccount, cacheExpiration, null);
    }

    /**
     * The callback interface used for mapping a resource bundle base name
     * to a project ID in the IBM Globlization service instance.
     */
    public interface NameMapper {
        /**
         * Returns the project ID used in the IBM Globalization service instance for
         * the specified resource bundle base name.
         * 
         * @param baseName The base name used in Java 
         * @return  The project ID used in the IBM Globalization service instance, or <code>null</code>
         *          if there is no corresponding project.
         */
        String getProjectID(String baseName);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account and the cache expiration. The cache expiration time is in milliseconds
     * and must be positive except for two special values.
     *  <ul>
     *      <li>{@link Control#TTL_DONT_CACHE} to disable resource bundle cache</li>
     *      <li>{@link Control#TTL_NO_EXPIRATION_CONTROL} to disable resource bundle cache expiration</li>
     *  </ul>
     *
     * @param serviceAccount    The service account.
     * @param cacheExpiration   The cache expiration, see the method description for details.
     * @param nameMapper        The custom base name to project ID mapper, or null if no mapping is necessary.
     * @return  An instance of CloudResourceBundleControl.
     * @throws IllegalArgumentException when <code>serviceAccount</code> is null or <code>cacheExpiration</code>
     * value is illegal.
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount,
            long cacheExpiration, NameMapper nameMapper) {
        if (serviceAccount == null) {
            throw new IllegalArgumentException("serviceAccount is null");
        }
        if (cacheExpiration < 0
                && cacheExpiration != Control.TTL_DONT_CACHE
                && cacheExpiration != Control.TTL_NO_EXPIRATION_CONTROL) {
            throw new IllegalArgumentException("Illegal cacheExpiration: " + cacheExpiration);
        }
        return new CloudResourceBundleControl(serviceAccount, cacheExpiration, nameMapper);
        
    }

    /**
     * Package local constructor.
     * 
     * @param serviceAccount    The service account
     * @param ttl               The cache expiration time in milliseconds.
     *                          -1 (Control.TTL_DONT_CACHE) to disable cache,
     *                          -2 (Control.TTL_NO_EXPIRATION_CONTROL) to disable cache expiration.
     * @param nameMapper        The Java base name to IBM Globalization service project ID
     *                          mapper, or null if same names are used.
     */
    CloudResourceBundleControl(ServiceAccount serviceAccount, long ttl, NameMapper nameMapper) {
        this.serviceAccount = serviceAccount;
        this.ttl = ttl;
        this.nameMapper = nameMapper;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return FORMAT_LIST;
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return ttl;
    }

    @Override
    public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader,
        ResourceBundle bundle, long loadTime) {
        // We may implement light weight 'last update' check.
        // For now, always returns true, that means, whenever cached
        // bundle is expired, reload the contents from the GaaS
        // translation service server.
        return true;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
        throws IllegalAccessException, InstantiationException, IOException {
        if (!format.equals(FORMAT_GAAS_TRANSLATE_CLOUD)) {
            return super.newBundle(baseName, locale, format, loader, reload);
        }

        if (!isLocaleSupportedByService(locale)) {
            return null;
        }

        String projectName = nameMapper != null ? nameMapper.getProjectID(baseName) : baseName;
        if (projectName == null) {
            return null;
        }
        // loadBundle returns null if locale is not available
        return CloudResourceBundle.loadBundle(serviceAccount, projectName, locale);
    }

    /**
     * Returns if the specified locale is supported by the service.
     * This implementation calls GaaS's /service endpoint and cache the
     * results. This method returns true if the specified locale is included
     * in the supported language list (both source/target). It does not
     * mean the specified language is available in a specific project.
     * 
     * @param locale The locale.
     * @return true if the specified locale is supported by the service.
     */
    private boolean isLocaleSupportedByService(Locale locale) {
        URL url = serviceAccount.getServiceUrl();
        Set<Locale> locales = SUPPORTED_LOCALES.get(url);
        if (locales == null) {
            ServiceAccount accountWithNoKey = new ServiceAccount(url, null);
            GaasRestServiceClient client = new GaasRestServiceClient(accountWithNoKey);
            Map<String, Set<String>> supportedTranslation = client.getSupportedTranslation();
            if (supportedTranslation == null) {
                locales = Collections.emptySet();
                // don't cache - the service might not be responding temporarily
            } else {
                locales = new HashSet<Locale>();
                for (Entry<String, Set<String>> entry : supportedTranslation.entrySet()) {
                    String fromLang = entry.getKey();
                    locales.add(Locale.forLanguageTag(fromLang));
                    for (String toLang : entry.getValue()) {
                        locales.add(Locale.forLanguageTag(toLang));
                    }
                }
                SUPPORTED_LOCALES.putIfAbsent(url, locales);
            }
        }
        return locales.contains(locale);
    }
}
