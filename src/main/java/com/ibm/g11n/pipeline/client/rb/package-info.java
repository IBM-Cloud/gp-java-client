/**
 * Provides the classes necessary to access translated resource strings
 * managed by IBM Globalization Pipeline service through the standard
 * Java ResourceBundle APIs.
 *
 * <p>
 * The implementation uses a class extending Java
 * {@link java.util.ResourceBundle.Control ResourceBundle.Control}. The custom
 * <code>Control</code> subclass looks up resources in IBM Globalization Pipeline
 * bundle first, then if no matching bundle is not found, use the standard
 * Java resource bundles (.class/.properties) on local system as fallback.
 * Therefore, with this custom <code>Control</code> implementation, you can
 * package root bundle in your application (as final fallback) and localized
 * resources from Globalization Pipeline service.
 *
 * <p>
 * The steps below explains a typical usage example in Java Web server application
 * on Bluemix.
 * <p>
 * 1. Add Globalization Pipeline service to your application and bind it.
 * <p>
 * 2. Log onto Globalization Pipeline service dashboard on Bluemix and create a new bundle.
 *    It is recommended to create a bundle using a name matching your translatable
 *    resource bundle's base name. For example, if you use "com.ibm.myapp.MyMessages" as the
 *    bundle's base name, create a bundle with the same name "com.ibm.myapp.MyMessages".
 *    Upload your resource contents.
 * <p>
 * 3. Include the Globalization Pipeline Java client SDK library (this package) in your
 *    applciation's library path.
 * <p>
 * 4. In the application code, use the code snippet below to access Java ResourceBundle.
 * <pre>
 *      import java.util.Locale;
 *      import java.util.ResourceBundle;
 *      import java.util.ResourceBundle.Control;
 *      import com.ibm.g11n.pipeline.client.rb.CloudResourceBundleControl;
 *      ...
 *      
 *      Locale locale;
 *      ...
 *      
 *      // Specify the custom control when accessing a resource bundle.
 *      ResourceBundle rb = ResourceBundle.getBundle(
 *                              "com.ibm.myapp.MyMessages",
 *                              locale,
 *                              CloudResourceBundleControl.getInstance());
 *      
 *      // Access localized resource 
 *      String msg = rb.getString("msg1");
 *      ...
 * </pre>
 * <p>
 * If your Java runtime version is 8 or later, you can integrate this custom <code>Control</code>
 * implementation as a Java runtime extension. This allow you to integrate IBM Globalization
 * Pipeline service to your application to use a common code utilizing <code>ResourceBundle</code>
 * without updating the source code. For example,
 * 
 * <pre>
 *      import java.util.Locale;
 *      import java.util.ResourceBundle;
 *      ...
 *      
 *      Locale locale;
 *      ...
 *      
 *      // Specify the custom control when accessing a resource bundle.
 *      ResourceBundle rb = ResourceBundle.getBundle(
 *                              "com.ibm.myapp.MyMessages",
 *                              locale);
 *      
 *      // Access localized resource 
 *      String msg = rb.getString("msg1");
 * </pre>
 * 
 * The code above look for standard Java resource bundles (MyMessages_[locale].properties and
 * MyMessages_[locale].class) available in the application's class path. This Java client SDK
 * package contains an implementation of {@link java.util.spi.ResourceBundleControlProvider},
 * so putting the client SDK jar in JRE's extension classpath automatically enables the custom
 * <code>Control</code> in this package.
 * <p>
 * For an application running on Bluemix, putting the client SDK jar in the JRE's extension
 * classpath can be done by
 * <a href="https://www.ng.bluemix.net/docs/starters/liberty/index.html#customizingjre">Customizing
 * the JRE</a>. The SDK jar file should be placed in resources/.java-overlay/.java/.jre/lib/ext
 * folder in your application package.
 */
package com.ibm.g11n.pipeline.client.rb;
