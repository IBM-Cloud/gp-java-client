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
package com.ibm.g11n.pipeline.client.rb;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ibm.g11n.pipeline.client.ServiceAccount;

/**
 * <code>CloudResourceBundleControl</code> is a concrete subclass of {@link Control}.
 * The implementation looks for a bundle with the specified bundle base name (i.e. baseName argument used in
 * {@link ResourceBundle#getBundle(String, Locale, Control)}) in the IBM Globalization Pipeline service.
 * If there is no matching translation bundle, or there is a matching translation bundle but no matching locale
 * is found in the IBM Globlization Pipeline service instance, then the implementation uses the standard Java
 * implementation. This implementation allows some language bundles such as root stored locally, while other
 * language bundles stored in the IBM Globalization Pipeline service instance.
 * 
 * @author Yoshito Umaoka
 */
public final class CloudResourceBundleControl extends Control {

    private static final String FORMAT_GP_CLOUD_BUNDLE = "gp.cloud.bundle";

    /**
     * Enum for resource bundle lookup modes.
     * 
     * @author Yoshito Umaoka
     */
    public enum LookupMode {
        /**
         * Lookup a bundle in a Globalization Pipeline service instance first.
         * If absent, lookup a standard Java resource bundle in classpath.
         */
        REMOTE_THEN_LOCAL(
                Arrays.asList(FORMAT_GP_CLOUD_BUNDLE, "java.class", "java.properties")),

        /**
         * Lookup a standard Java resource bundle in classpath first.
         * If absent, lookup a bundle in a Globalization Pipeline service instance.
         */
        LOCAL_THEN_REMOTE(
                Arrays.asList("java.class", "java.properties", FORMAT_GP_CLOUD_BUNDLE)),

        /**
         * Lookup a bundle in a Globalization Pipeline service only.
         */
        REMOTE_ONLY(
                Collections.singletonList(FORMAT_GP_CLOUD_BUNDLE)),

        /**
         * Look up a standard Java resource bundle in classpath.
         */
        LOCAL_ONLY(
                Arrays.asList("java.class", "java.properties"));

        private final List<String> formatList;

        LookupMode(List<String> formatList) {
            this.formatList = Collections.unmodifiableList(formatList);
        }

        List<String> getFormatList() {
            return formatList;
        }
    };

    /**
     * Default resource bundle cache expiration time (60000 = 10 minutes)
     */
    public static final long DEFAULT_CACHE_EXPIRATION = 60000L;

    private final ServiceAccount serviceAccount;
    private long ttl;
    private Pattern inclusionPattern;
    private Pattern exclusionPattern;
    private NameMapper nameMapper;
    private LookupMode mode;

    /**
     * The environment variable name for specifying resource bundle lookup mode.
     * The valid values are defined in {@link LookupMode}. By default,
     * {@link LookupMode#REMOTE_THEN_LOCAL REMOTE_THEN_LOCAL} is used. 
     */
    public static final String GP_LOOKUP_MODE = "GP_LOOKUP_MODE";

    private static final LookupMode DEFAULT_LOOKUP_MODE = LookupMode.REMOTE_THEN_LOCAL;

    /**
     * The environment variable name for specifying resource bundle cache expiration time.
     * The unit is millisecond. When a constructor without cache expiration is used and
     * this environment environment variable is not set, the default cache expiration time
     * (60000 - 10 minutes) will be used.
     * <p>
     * Following negative values are reserved for special purpose:
     * <ul>
     *  <li>-1: The cloud resource bundles are never cached.</li>
     *  <li>-2: The cache for cloud resource bundles will never be expired.</li>
     * </ul>
     * Any other negative values are invalid and ignored.
     */
    public static final String GP_CACHE_EXPIRATION = "GP_CACHE_EXPIRATION";

    /**
     * The environment variable name for specifying bundle names included by this
     * cloud bundle implementation. The value is specified by a regular expression pattern.
     * For example, com\.acme\.myapp\..* to include all bundles in com.acme.myapp
     * package.
     * <p>
     * Note: When this environment variable is set, any resources not matching the
     * pattern won't be processed as a Globalization Pipeline cloud bundle. Also,
     * the exclusion pattern takes higher priority than the inclusion pattern.
     * @see #GP_RB_EXCLUSION_PATTERN
     */
    public static final String GP_RB_INCLUSION_PATTERN = "GP_RB_INCLUSION_PATTERN";

    /**
     * The environment variable name for specifying bundle names excluded by this
     * cloud bundle implementation, in addition to {@link #GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING}.
     * The value is specified by a regular expression pattern.
     * For example, com\.acme\.lib\..* to exclude all bundles in com.acme.lib package.
     * @see #GP_RB_INCLUSION_PATTERN
     * @see #GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING
     */
    public static final String GP_RB_EXCLUSION_PATTERN = "GP_RB_EXCLUSION_PATTERN";

    /**
     * The default regular expression pattern string specifying bundle names to be
     * excluded.
     * <pre>
     * (java|javax|org\.ietf|org\.jcp|org\.omg|org\.w3c|org\.xml|com\.sun|sun|jdk)\..*
     * </pre>
     * A resource bundle name matching this pattern won't be processed as
     * cloud bundle.
     * @see #GP_RB_EXCLUSION_PATTERN
     */
    public static final String GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING = 
            "(java|javax|org\\.ietf|org\\.jcp|org\\.omg|org\\.w3c|org\\.xml|com\\.sun|sun|jdk)\\..*";

    private static final Pattern DEFAULT_EXCLUSION =
            Pattern.compile(GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING);


    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with a service account
     * initialized with environment variables.
     * <p>
     * This method is equivalent to <code>getInstance(ServiceAccount.getIntance())</code>.
     * 
     * @return  An instance of CloundResourceBundleControl or null if there are not sufficient
     * configuration variables available in the environment.
     * @see ServiceAccount#getInstance()
     */
    public static CloudResourceBundleControl getInstance() {
        ServiceAccount serviceAccount = ServiceAccount.getInstance();
        if (serviceAccount == null) {
            return null;
        }
        return getInstance(serviceAccount);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account.
     * 
     * @param serviceAccount    The service account.
     * @return  An instance of CloundResourceBundleControl.
     * @throws IllegalArgumentException when serviceAccount is null.
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount) {
        return getInstance(serviceAccount, initCacheExpiration());
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account.
     * 
     * @param serviceAccount    The service account.
     * @param mode              The resource bundle resolution mode, or null for default mode
     *                          ({@link LookupMode#REMOTE_THEN_LOCAL}).
     * @return  An instance of CloundResourceBundleControl.
     * @throws IllegalArgumentException when serviceAccount is null.
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount, LookupMode mode) {
        return getInstance(serviceAccount, initCacheExpiration(), null, null, null, mode);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account and the cache expiration.
     * <p>
     * This method is equivalent to
     * <code>getInstance(serviceAccount, cacheExpiration, null, null, null)</code>.
     *
     * @param serviceAccount    The service account.
     * @param cacheExpiration   The cache expiration, see the method description for details.
     * @return  An instance of CloudResourceBundleControl.
     * @throws IllegalArgumentException when <code>serviceAccount</code> is null or <code>cacheExpiration</code>
     * value is illegal.
     * @see #getInstance(ServiceAccount, long, String, String, NameMapper)
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount,
            long cacheExpiration) {
        return getInstance(serviceAccount, cacheExpiration, null, null);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account, cache expiration and bundle inclusion/exclusion name patterns.
     * <p>
     * This method is equivalent to
     * <code>getInstance(serviceAccount, cacheExpiration, inclusionPattern, exclusionPattern, null)</code>.
     * @param serviceAccount    The service account.
     * @param cacheExpiration   The cache expiration.
     * @param inclusionPattern  The regular expression pattern string for specifying resource bundle
     *                          names to be included, or null.
     * @param exclusionPattern  The regular expression pattern string for specifying resource bundle
     *                          names to be excluded in addition to
     *                          {@link #GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING}
     * @return  An instance of CloudResourceBundleControl.
     * @throws IllegalArgumentException when <code>serviceAccount</code> is null,
     * or <code>cacheExpiration</code> value is illegal,
     * or <code>inclusionPattern</code>/<code>exclusionPattern</code> syntax is invalid.
     * @see #getInstance(ServiceAccount, long, String, String, NameMapper)
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount,
            long cacheExpiration, String inclusionPattern, String exclusionPattern) {
        return getInstance(serviceAccount, cacheExpiration, null, null, null);
    }

    /**
     * The callback interface used for mapping a resource bundle base name
     * to a bundle ID in the IBM Globlization Pipeline service instance.
     */
    public interface NameMapper {
        /**
         * Returns the bundle ID used in the IBM Globalization Pipeline service instance for
         * the specified resource bundle base name.
         * 
         * @param baseName The base name used in Java
         * @return  The bundle ID used in the IBM Globalization Pipeline service instance, or
         *          <code>null</code> if there is no corresponding bundle.
         */
        String getBundleID(String baseName);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account, cache expiration, bundle inclusion/exclusion name pattern and the custom
     * bundle name mapper.
     * <p>
     * The cache expiration time is in milliseconds
     * and must be positive except for two special values.
     *  <ul>
     *      <li>{@link Control#TTL_DONT_CACHE} to disable resource bundle cache</li>
     *      <li>{@link Control#TTL_NO_EXPIRATION_CONTROL} to disable resource bundle cache expiration</li>
     *  </ul>
     * <p>
     *
     * @param serviceAccount    The service account.
     * @param cacheExpiration   The cache expiration, see the method description for details.
     * @param inclusionPattern  The regular expression pattern string for specifying resource bundle
     *                          names to be included, or null.
     * @param exclusionPattern  The regular expression pattern string for specifying resource bundle
     *                          package names to be excluded in addition to
     *                          {@link #GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING}, or null.
     * @param nameMapper        The custom base name to bundle ID mapper, or null if no mapping is necessary.
     * @return  An instance of CloudResourceBundleControl.
     * @throws IllegalArgumentException when <code>serviceAccount</code> is null,
     * or <code>cacheExpiration</code> value is illegal,
     * or <code>inclusionPattern</code>/<code>exclusionPattern</code> syntax is invalid.
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount,
            long cacheExpiration, String inclusionPattern, String exclusionPattern, NameMapper nameMapper) {
        return getInstance(serviceAccount, cacheExpiration,
                inclusionPattern, exclusionPattern, nameMapper, null);
    }

    /**
     * Create an instance of <code>CloudResourceBundleControl</code> with the specified
     * service account, cache expiration, bundle inclusion/exclusion name pattern, the custom
     * bundle name mapper and bundle lookup mode.
     * <p>
     * The cache expiration time is in milliseconds
     * and must be positive except for two special values.
     *  <ul>
     *      <li>{@link Control#TTL_DONT_CACHE} to disable resource bundle cache</li>
     *      <li>{@link Control#TTL_NO_EXPIRATION_CONTROL} to disable resource bundle cache expiration</li>
     *  </ul>
     * <p>
     *
     * @param serviceAccount    The service account.
     * @param cacheExpiration   The cache expiration, see the method description for details.
     * @param inclusionPattern  The regular expression pattern string for specifying resource bundle
     *                          names to be included, or null.
     * @param exclusionPattern  The regular expression pattern string for specifying resource bundle
     *                          package names to be excluded in addition to
     *                          {@link #GP_RB_DEFAULT_EXCLUSION_PATTERN_STRING}, or null.
     * @param nameMapper        The custom base name to bundle ID mapper, or null if no mapping is necessary.
     * @param mode              The resource bundle lookup mode. If null, and the environment variable
     *                          {@link #GP_LOOKUP_MODE} is not set, {@link LookupMode#REMOTE_THEN_LOCAL
     *                          REMOTE_THEN_LOCAL} is used.
     * @return  An instance of CloudResourceBundleControl.
     * @throws IllegalArgumentException when <code>serviceAccount</code> is null,
     * or <code>cacheExpiration</code> value is illegal,
     * or <code>inclusionPattern</code>/<code>exclusionPattern</code> syntax is invalid.
     */
    public static CloudResourceBundleControl getInstance(ServiceAccount serviceAccount,
            long cacheExpiration, String inclusionPattern, String exclusionPattern,
            NameMapper nameMapper, LookupMode mode) {
        if (serviceAccount == null) {
            throw new IllegalArgumentException("serviceAccount is null");
        }

        if (cacheExpiration < 0
                && cacheExpiration != Control.TTL_DONT_CACHE
                && cacheExpiration != Control.TTL_NO_EXPIRATION_CONTROL) {
            throw new IllegalArgumentException("Illegal cacheExpiration: " + cacheExpiration);
        }

        Pattern incPat = null;
        Pattern excPat = null;

        if (inclusionPattern != null) {
            try {
                incPat = Pattern.compile(inclusionPattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Illegal inclusionPattern: " + inclusionPattern, e);
            }
        }

        if (exclusionPattern != null) {
            try {
                excPat = Pattern.compile(exclusionPattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Illegal exclusionPattern: " + exclusionPattern, e);
            }
        }

        return new CloudResourceBundleControl(serviceAccount, mode, cacheExpiration,
                incPat, excPat, nameMapper);
    }


    /**
     * Package local constructor.
     * 
     * @param serviceAccount    The service account
     * @param mode              The resource bundle lookup mode
     * @param ttl               The cache expiration time in milliseconds.
     *                          -1 (Control.TTL_DONT_CACHE) to disable cache,
     *                          -2 (Control.TTL_NO_EXPIRATION_CONTROL) to disable cache expiration.
     * @param inclusionPattern  The regular expression pattern specifying the inclusive bundle
     *                          base names.
     *                          
     * @param nameMapper        The Java base name to IBM Globalization Pipeline bundle ID
     *                          mapper, or null if same names are used.
     */
    CloudResourceBundleControl(ServiceAccount serviceAccount, LookupMode mode, long ttl,
            Pattern inclusionPattern, Pattern exclusionPattern, NameMapper nameMapper) {
        this.serviceAccount = serviceAccount;
        this.mode = (mode == null) ? initMode() : mode;
        this.ttl = ttl;
        this.inclusionPattern = inclusionPattern;
        this.exclusionPattern = exclusionPattern;
        this.nameMapper = nameMapper;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return mode.getFormatList();
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
        // bundle is expired, reload the contents from the Globalization
        // Pipeline service instance.
        return true;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
        throws IllegalAccessException, InstantiationException, IOException {

        if (!format.equals(FORMAT_GP_CLOUD_BUNDLE)) {
            // When requested resource format is not GP cloud bundle,
            // just delegate the request to the Java's default implementation.
            return super.newBundle(baseName, locale, format, loader, reload);
        }

        if (isExcluded(baseName)) {
            return null;
        }

        if (locale.getLanguage().isEmpty()) {
            // Globalization Pipeline does not support a locale
            // with no language code, including root locale
            return null;
        }

        // Map the input baseName to GP's bundleId if NameMapper is available
        String bundleId = nameMapper != null ? nameMapper.getBundleID(baseName) : baseName;
        if (bundleId == null) {
            return null;
        }

        // loadBundle returns null if locale is not available
        return CloudResourceBundle.loadBundle(serviceAccount, bundleId, locale);
    }

    private boolean isExcluded(String baseName) {
        if (DEFAULT_EXCLUSION.matcher(baseName).matches()) {
            return true;
        }

        if (exclusionPattern != null
                && exclusionPattern.matcher(baseName).matches()) {
            return true;
        }

        if (inclusionPattern != null
                && !inclusionPattern.matcher(baseName).matches()) {
            return true;
        }

        return false;
    }

    private static long initCacheExpiration() {
        Map<String, String> env = System.getenv();
        String envCacheExp = env.get(GP_CACHE_EXPIRATION);

        if (envCacheExp != null) {
            try {
                long cacheExpiration = Long.parseLong(envCacheExp);
                if (cacheExpiration >= -2) {
                    return cacheExpiration;
                }
            } catch (NumberFormatException e) {
                // Fall through
            }
        }

        return DEFAULT_CACHE_EXPIRATION;
    }

    private static LookupMode initMode() {
        Map<String, String> env = System.getenv();
        String envMode = env.get(GP_LOOKUP_MODE);

        LookupMode mode = DEFAULT_LOOKUP_MODE;
        if (envMode != null) {
            try {
                mode = LookupMode.valueOf(envMode.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return mode;
    }
}
