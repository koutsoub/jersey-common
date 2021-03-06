This library bundles up some utility classes and extracts common behaviours
from a number of Java applications. Chances are it may not be suitable for
your applications, but its stuff that we found is often repeated when we
use Java to built lightweight services with simple HTTP interfaces.

Its basically some glue code pulling together some great libraries in 
very much the same mould as [Dropwizard](https://github.com/codahale/dropwizard). These include:

* Jetty for its HTTP server
* Jersey JAX-RS implementation  
* Jackson for JSON wrangling
* Guice dependency injection
* slf4j logging api
* metrics for, err, metrics

Main Classes/Packages of note
=============================

### HttpServer 

Encapsulates the configuration and lifecycle of a HTTP server which is
initialised with two servlet contexts. 
The metrics context serves MetricsServlet straight from Yammer's excellent
metrics-servlet project. Should you choose to use the various metrics-XXX 
libraries to instrument your application any meters, gauges, timers etc that 
you gather will be made available in JSON form from this context. If you
don't collect any such metrics: a) you should be and b) you can still use 
the metrics context to get a thread dump from your app over HTTP. 
See https://github.com/codahale/metrics for the full glory of metrics

The root context is bound to a GuiceServletContextListener. Used in conjunction 
with JerseyServletModule (see below), you can specify the packages containing
your Jersey resources, which will then be served up under the root context.
See the examples below for how to plug all this together.

### ObjectMapperProvider

Does a tiny amount of opinionated configuration to ensure that JSON output 
generated via Jackson's JAXB and POJO conversions is pretty printed. This can
be turned off by disabling the default filters in JerseyServletModule.

### Exceptions

Nothing too exciting or earth-shattering here, just some typed exceptions which
represent particular HTTP responses. The all extend HttpException, which itself
is a RuntimeException. When used in conjunction with DefaultExceptionMapper 
(again, this is on by default, but can be disabled via JerseyServletModule), 
the @GET/@POST/etc methods in your Jersey resources can throw these exceptions,
and a HTTP response will be generated with the appropriate status code, a 
text/plain media type and the message from the exception in the body.     
 
### Auth

This is not production strength authentication for your public web properties.
It consists of a Filter which uses a pluggable Authenicator mechanism to 
identify the principle making a HTTP request (using basic auth), and stores that
as the security context of the request. If the principle is authenticated 
(according to whatever Authenticator implementation you plug in) it is assigned
the role 'admin', which can then be used in JSR-250 role annotations on your 
Jersey resources to restrict or grant access to particular resources or methods. 
As mentioned, this is not an production strength authentication mechanism: we 
implemented it purely as a safety valve to ensure that anything with serious 
implications for the state of a service (like issuing a DELETE to the resource 
which indicates an up status to the load balancer) requires some effort to execute.
A couple of trivial Authenticator implementations are provided - one permissive, 
the other restrictive as well as a Guice module which binds the permissive one
(for apps where you just don't care about this).  
In all honesty, there are much better ways to implement this behaviour but, 
hey, this is legacy and its not been painful enough to warrant a re-do (yet)

### Filters

Some request/response filters we found we were re-using in several places:

* LoggingFilter 

Uses the MDC (mapped diagnostic context) provided by slf4j to attach
a couple of tokens to each incoming request. First, the system time is recorded on 
the way in, so that when the response is emitted we can log the time to service the
request. Second, a (fairly) unique identifier is generated for the request. As this 
is put into the MDC, its available to the underlying logging framework and so can be 
included in every log statement via logging config (see the example below).
We use this extensively with log4j as the underlying logging provider, YMMV with
other providers. A header containing this identifier is also added to the HTTP 
response, which makes debugging much easier.
   
* ServerAgentHeaderFilter
 
Sets the Server header on HTTP responses to a value provided by an injected 
ServerInfo implementation. How you generate this is obviously app specific (we tend
to use a ServerInfo impl which reads from a properties file, created at build time).
GenericServerInfoModule binds ServerInfo to an anonymous instance which reads the 
desired String from a system property ( com.talis.jersey.guice.serverid )
 
### Guice

The main article of interest here is JerseyServletModule. This takes a varargs of
the package names which contain your Jersey resources and configures Jersey to 
use Guice for dependnency injection. It's also a bit opinionated, and configures
the application with a standard set of filters and providers. The filters it adds
are the three custom filters mentioned above, plus Jersey's GZIPEncodingFilter which
provides conditional (de|en)coding of request and response entities based on the 
Content-Encoding & Accept-Encoding headers in the request. The pretty-printing
Jackson ObjectMapperProvider & exception formatting DefaultExceptionMapper (see above)
are also registered with the application by default. 
You can disable all of this default configuration by setting the system property
com.talis.jersey.guice.disable-default-filters to "true".
     
Also in this package are a couple of modules which bind the authentication & server
identifier implementations to defaults. So that if you don't care about these too
much, include these modules. 
     
Examples
========

### Starting an HTTP Server

```java
public static void main(String[] args) throws Exception {
  int httpPort = 8080;
  System.setProperty("com.talis.jersey.guice.serverid", "AnExample");
  Injector injector = Guice.createInjector(
                             new ApplicationModule(),                     // contains app specific bindings
                             new NoopAuthenticationModule(),              // we don't need no stinking authentication
                             new GenericServerInfoModule(),               // to set the Server response header 
                             new JerseyServletModule("com.example.foo",   // packages containing  
                                                     "com.example.bar")); // your JAX-RS resources
			 
  HttpServer webserver = new HttpServer();
  webserver.start(httpPort, injector);    
}
```

### Log4j Appender config that includes request id token

<appender name="stdout" class="org.apache.log4j.ConsoleAppender"> 
  <layout class="org.apache.log4j.PatternLayout"> 
    <param name="ConversionPattern" value="%d{ISO8601} -%5p %-25c{1} :%t: %X{R_UID}|%m%n"/>
  </layout>
</appender>

The ```%X{R_UID}``` is replaced with the value of the request id pulled from the MDC at runtime

Maven
=====

```xml
<repository>  
  <id>talis-public-repo</id>
  <url>http://oss.talisplatform.com/content/groups/public/</url>
</repository>

<dependency>
  <groupId>com.talis</groupId>
  <artifactId>jersey-common</artifactId>
  <version>1.5</version>
</dependency> 
```

