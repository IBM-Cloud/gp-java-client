/**
 * Provides Java APIs for accessing
 * <a href="https://gp-beta-rest.ng.bluemix.net/translate/swagger/index.html">IBM
 * Globalization Pipeline's REST endpoints</a> which allow you to create, read, modify
 * and delete translation bundles, resource strings and users.
 * <p>
 * {@link com.ibm.g11n.pipeline.client.ServiceClient ServiceClient} is the class providing methods
 * to do CRUD operations on resource maintained by Globalization Pipeline service.
 * <p>
 * When an application running on Bluemix may use
 * {@link com.ibm.g11n.pipeline.client.ServiceClient#getInstance() ServiceClient.getInstance()}
 * to create an instance of <code>ServiceClient</code> initialized with an instance
 * of Globalization Pipeline service bound to the application. When an application
 * is running out of Bluemix, you may need to create
 * {@link com.ibm.g11n.pipeline.client.ServiceAccount ServiceAccount} instance by manually specifying
 * service credentials, or set these credentials in environment. variables. For more details,
 * please refer {@link com.ibm.g11n.pipeline.client.ServiceAccount ServiceAccount} API documentation.
 * <p>
 * Below is a coding example creating a new bundle.
 * <pre>
 *      // Service URL and credentials
 *      final String url = "https://gp-rest.ng.bluemix.net/translate/rest";
 *      final String instanceId = "b3a927cd691f34f89aee6c70cd065e73";
 *      final String userId = "4ef946c2ab709c595a948cf3dc0c5729";
 *      final String password = "CtMzbSNEZA20IP6h5ckrGMrTC5g0wYad";
 *
 *      ServiceAccount account = ServiceAccount.getInstance(url, instanceId, userId, password);
 *      ServiceClient client = ServiceClient.getInstance(account);
 *
 *      // New bundle ID
 *      final String bundleId = "com.acme.myapp.Messages";
 *
 *      // Creates a new bundle configuration with English ("en") as source language
 *      NewBundleData newBundleData = new NewBundleData("en");
 *
 *      // Sets target languages
 *      Set&lt;String&gt; targetLanguages = new HashSet&lt;String&gt;();
 *      targetLanguages.add("fr");      // Add French
 *      targetLanguages.add("zh-Hans"); // Add Simplified Chinese
 *
 *      newBundleData.setTargetLanguages(targetLanguages);
 *
 *      try {
 *          // Creates a new bundle
 *          client.createBundle(bundleId, newBundleData);
 *          System.out.println("Bundle: " + bundleId + " was successfully created.");
 *      } catch (ServiceException e) {
 *          System.out.println("Failed to create a new bundle" + bundleId + ": " + e.getMessage());
 *      }
 * </pre>
 */
package com.ibm.g11n.pipeline.client;