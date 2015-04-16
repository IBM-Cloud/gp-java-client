/**
 * Provides the classes necessary to access localized resource strings
 * managed by Bluemix IBM Globalization service through the standard
 * Java ResourceBundle.
 *
 * <p>
 * The implementation uses a class extending Java
 * {@link java.util.ResourceBundle.Control ResourceBundle.Control}. The custom
 * <code>Control</code> subclass looks up resources in Bluemix IBM Globalization
 * project first, then if no matching bundle is not found, use the standard
 * Java resource bundles (.class/.properties) on local system as fallback.
 * Therefore, with this custom <code>Control</code> implementation, you can
 * package root bundle in your application (as final fallback) and localized
 * resources from the IBM Globalization service.
 *
 * <p>
 * The steps below explains a typical usage example in Java Web server application
 * on Bluemix.
 * <p>
 * 1. Add IBM Globalization service to your application and bind it.
 * <p>
 * 2. Log onto the IBM Globalization service dashboard and create a translation project.
 *    It is recommended to create a project using a name matching your translatable
 *    resource bundle's base name. For example, if you use "com.ibm.MyMessages" as the
 *    bundle's base name, create a project with the same name "com.ibm.MyMessages".
 *    Upload your resource contents (for now, Java properties file format is supported).
 * <p>
 * 3. Include the bluemix IBM Globalization Java client library (this package) in your
 *    applciation's library path.
 * <p>
 * 4. In the application code, use the code snippet below to access Java ResourceBundle.
 * <pre>
 *      import java.util.Locale;
 *      import java.util.ResourceBundle;
 *      import java.util.ResourceBundle.Control;
 *      import com.ibm.gaas.CloudResourceBundleControl;
 *      import com.ibm.gaas.ServiceAccount;
 *      ...
 *      
 *      Locale locale;
 *      ...
 *      
 *      // Create a service account object. The no-args factory method
 *      // will load the access configuration from VCAP_SERVICES. If there is
 *      // only one IBM Globalization service instance is bound to the application,
 *      // this is sufficient.
 *      ServiceAccount account = ServiceAccount.getInstance();
 *      
 *      // Create an instance of the custom resource bundle control.
 *      // The control instance is thread safe and reusable. Your application
 *      // may create an instance once, and reuse it.
 *      Control ctrl = CloudResourceBundleControl.getInstance(account);
 *
 *      // Specify the custom control when accessing a resource bundle.
 *      ResourceBundle rb = ResourceBundle.getBundle("com.ibm.MyMessages", locale, ctrl);
 *      
 *      // Access localized resource 
 *      String myMessage = rb.getString("myMessage");
 *      ...
 * </pre>
 */
package com.ibm.gaas;

