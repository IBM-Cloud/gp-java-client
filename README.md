<!--
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
-->
Java Client SDK for Globalization Pipeline on IBM Bluemix
==
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ibm.g11n.pipeline/gp-java-client/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.ibm.g11n.pipeline/gp-java-client)

# What is this?

This is a Java client SDK for
[Globalization Pipeline on IBM Bluemix](https://www.ng.bluemix.net/docs/services/GlobalizationPipeline/index.html).
This SDK provides JDK ResourceBundle integration and Java APIs for accessing
Globalization Pipeline's REST endpoints.

## Custom ResourceBundle implementation

In a Java applications, localized UI strings are usually stored in Java
resource bundle class files or in Java properties files. These localized
strings are accessed through
[java.util.ResourceBundle](http://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html)
class.
The ResourceBundle class provides a way to support custom resource bundle
formats by specifying a custom instance of
[ResourceBundle.Control](http://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.Control.html).
This SDK contains an implementation of ResourceBundle.Control which looks up and
load resource strings from an instance of Globalization Pipeline service.

## Java APIs for Globalization Pipeline REST endpoints

Globalization Pipeline provides
[REST APIs](https://gp-rest.ng.bluemix.net/translate/swagger/index.html).
for creating translation bundles, fetching translated strings, managing
user accounts used for accessing the service and various other operations.
This SDK provides Java APIs for these REST endpoints, so you can develop
Java applciation managing end to end translation process.

# Getting started

To get started, you should familiarize yourself with the service itself. A good place
to begin is by reading the [Quick Start Guide](https://github.com/IBM-Bluemix/gp-common#quick-start-guide) and the official [Getting Started with IBM Globalization ](https://www.ng.bluemix.net/docs/services/GlobalizationPipeline/index.html)
documentation.

The documentation explains how to find the service on Bluemix, create a new service
instance, create a new bundle, and access the translated messages.

# Usage

## Minimum requirements

This library requires Java 7 or later version of Java Runtime Environment.

To build the library from the source files, JDK 8 is required.

## Accessing translated resources from a Bluemix Java application

Once a new bundle is created, and the contents in the source language is uploaded, your
Java application can use the translated results through Java's standard
[ResourceBundle](http://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html) class.

Your original code may look like below:

    ...
    Locale locale;  // the target language
    ...
    
    ResourceBundle rb = ResourceBundle.getBundle("com.ibm.app.MyMessages", locale);
    String msg = rb.getString("msg1");

### Using ResourceBundleControlProvider SPI (Java 8 or later)

This SDK implements
[ResourceBundelControlProvider](https://docs.oracle.com/javase/8/docs/api/java/util/spi/ResourceBundleControlProvider.html)
introduced in Java 8. With the provider implementation, you can retrieve translated resource
strings stored in a Globalization Pipeline project without any code changes.

To enable this feature, you can put the SDK jar file (and the dependencies - for now, GSON
jar file only) into Java's extension directory.

If the application is running on Bluemix, then what you need is to package the SDK jar file
(and the dependencies) in your application to the JRE overlay directory corresponding to
the JRE's extension directory 'resources/.java-overlay/.java/jre/lib/ext'. For more details,
please refer
[Customizing the JRE](https://www.ng.bluemix.net/docs/starters/liberty/index.html#customizingjre)

If the application is not running on Bluemix, then you need to supply credentials used for
accessing the Globalization Pipeline service instance. The credentials can be specified by
following environment variables.

* __GP_URL__: Service URL (e.g. https://gp-rest.ng.bluemix.net/translate/rest)
* __GP_INSTANCE_ID__: Service instance ID (e.g. d3f537cd617f34c86ac6b270f3065e73)
* __GP_USER_ID__: User ID (e.g. e92a1282a0e4f97bec93aa9f56fdb838)
* __GP_PASSWORD__: User password (e.g. zg5SlD+ftXYRIZDblLgEA/ILkkCNqE1y)

Please also refer Java Tutorials article
[Installing a Custom Resource Bundle as an Extension](https://docs.oracle.com/javase/tutorial/i18n/serviceproviders/resourcebundlecontrolprovider.html)
about the service provider interface and configuration in general.

### Using CloudResourceBundleControl

If your application is running on JRE 7 or you want to limit the use of Globalization Pipeline
service to specific bundles, then you can use the custom
[ResourceBundle.Control](http://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.Control.html)
implementation included in this SDK.

If the application is running on Bluemix, the source code should be changed to:

    import com.ibm.g11n.pipeline.client.rb.CloudResourceBundleControl;
    ...
    Locale locale;  // the target language
    ...
    // ResourceBundle is created with the custom control
    ResourceBundle rb = ResourceBundle.getBundle("com.ibm.app.MyMessages", locale,
                                                 CloudResourceBundleControl.getInstance());
    String msg = rb.getString("msg1");

With the code above, your application will retrieve a translated resource string from
your translation bundle stored in the Globalization Pipeline service instance.
If the specified language (Locale) is not available in the translation bundle, the custom
control will automatically fallback to local resources (.class or .properties included in
your application).

When the application is not running on Bluemix, you need to supply credentials
manually to create the custom control, instead of calling the no-arg factory method
CloudResourceBundleControl.getInstance(). For example,

    import com.ibm.g11n.pipeline.client.ServiceAccount;
    import com.ibm.g11n.pipeline.client.rb.CloudResourceBundleControl;
    ...
    Locale locale;  // the target language
    ...

    ServiceAccount account = ServiceAccount.getInstance(
        "https://gp-rest.ng.bluemix.net/translate/rest", // service URL
        "d3f537cd617f34c86ac6b270f3065e73",              // instance ID
        "e92a1282a0e4f97bec93aa9f56fdb838",              // user ID
        "zg5SlD+ftXYRIZDblLgEA/ILkkCNqE1y");             // user password

    // ResourceBundle is created with the custom control
    ResourceBundle rb = ResourceBundle.getBundle("com.ibm.app.MyMessages", locale,
                                                 CloudResourceBundleControl.getInstance(account));
    String msg = rb.getString("msg1");

# Community

* View or file GitHub [Issues](https://github.com/IBM-Bluemix/gp-java-client/issues)
* Connect with the open source community on [developerWorks Open](https://developer.ibm.com/open/ibm-bluemix-globalization-pipeline-service/java-sdk/)

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

# License

Apache 2.0. See [LICENSE.txt](LICENSE.txt).

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
> http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
