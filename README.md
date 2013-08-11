ssi-servlet
===========

Is a [Maven](http://maven.apache.org/) enabled Java project for building a utility [JAR](http://en.wikipedia.org/wiki/JAR_\(file_format\)) that contains a reusable [Java Enterprise Edition (JEE)](http://en.wikipedia.org/wiki/Java_EE) [Servlet](http://en.wikipedia.org/wiki/Java_Servlet) used for [Server-Side Includes (SSI)](http://en.wikipedia.org/wiki/Server_Side_Includes).

This Servlet extends the SSI Servlet from [Apache Tomcat](http://tomcat.apache.org/tomcat-5.5-doc/ssi-howto.html) and [JBoss Web](https://www.jboss.org/jbossweb/introduction) to provide the following additional features.

* HTML conpression of the response can be enabled with a Servlet configuration init parameter
* When the HTML compression feature is enabled, then in-line CSS minimization can also be enabled with another init parameter
* Also with the HTML compression feature, there is in-line JavaScript minimization that can be enabled with another init parameter
* The update HTML lang and dir attributes of either the html or body element feature will automatically update the HTML response to have the correct values for the current request culture (language and country), when enabled with another init parameter
