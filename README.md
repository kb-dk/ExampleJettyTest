# ExampleJettyTest

This is an example of how you can make a jersey webservice and a jetty integration test.

### Webservice service

There is only one way to completely test webservice code; run it as a webservice. Now, as Apache Tomcat is our default
 servlet container, this can be a daunting task to do programmatically. To the rescue comes [Eclipse Jetty](https://eclipse.org/jetty/)

> Jetty provides an Web server and javax.servlet container, plus support for SPDY, Web Sockets, OSGi, JMX, JNDI, JASPI, AJP and many other integrations. These components are open source and available for commercial use and distribution. Jetty is used in a wide variety of projects and products. Jetty can be embedded in devices, tools, frameworks, application servers, and clusters.

Once you use jetty in a test, it will never be a good unit test, and it will always exist in the borderland between
unit tests and integration tests. So, with this warning, how to do it:

Clone the  project.

Your pom.xml must contain these dependencies to be able to run a jetty server

    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-server</artifactId>
        <version>9.2.10.v20150310</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-webapp</artifactId>
        <version>9.2.6.v20141205</version>
        <scope>test</scope>
    </dependency>

In order to communicate with the running webservice, you also need

    <!--Http client, needed to talk http to the servlet in test-->
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
        <version>4.4.1</version>
        <scope>test</scope>
    </dependency>

And, of course, to run tests, you need

    <!--The test framework-->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>6.8.7</version>
        <scope>test</scope>
    </dependency>

In order to make a jersey REST servlet, you need these dependencies

    <!--These three are nessesary to make a jersey servlet-->
    <dependency>
        <groupId>com.sun.jersey</groupId>
        <artifactId>jersey-server</artifactId>
        <version>1.18.1</version>
    </dependency>
    <dependency>
        <groupId>com.sun.jersey</groupId>
        <artifactId>jersey-servlet</artifactId>
        <version>1.18.1</version>
    </dependency>
    <dependency>
        <groupId>javax.ws.rs</groupId>
        <artifactId>jsr311-api</artifactId>
        <version>1.1.1</version>
    </dependency>

And a web.xml in src/main/webapp/WEB-INF like this

    <web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">
        <servlet>
            <servlet-name>server</servlet-name>
            <servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer
            </servlet-class>
            <init-param>
                <param-name>com.sun.jersey.config.property.packages</param-name>
                <param-value>test.server</param-value>
            </init-param>
        </servlet>
        <servlet-mapping>
            <servlet-name>server</servlet-name>
            <url-pattern>/*</url-pattern>
        </servlet-mapping>
    </web-app>

If your webresource is this

    package test.server;

    import javax.ws.rs.GET;
    import javax.ws.rs.Path;

    @Path("/")
    public class Server {

        @GET
        @Path("helloWorld")
        public String helloWorld(){
            return "Hello World";
        }
    }


You can then test it with a integration test like this

    package test.server;

    import org.apache.http.HttpResponse;
    import org.apache.http.client.HttpClient;
    import org.apache.http.client.methods.HttpGet;
    import org.apache.http.impl.client.HttpClientBuilder;
    import org.eclipse.jetty.server.Server;
    import org.eclipse.jetty.webapp.WebAppContext;
    import org.testng.Assert;
    import org.testng.annotations.AfterMethod;
    import org.testng.annotations.BeforeMethod;
    import org.testng.annotations.Test;

    import java.io.BufferedReader;
    import java.io.InputStreamReader;

    public class ServerTest {

        private Server server;

        @BeforeMethod
        public void startServer() throws Exception {
            server = new Server(8080);
            server.setStopAtShutdown(true);
            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/app"); //This is the path your webapp will be exposed on
            webAppContext.setResourceBase("src/main/webapp"); //This is where your WEB-INF folder is
            webAppContext.setClassLoader(getClass().getClassLoader());
            server.setHandler(webAppContext);
            server.start();
        }


        @Test
        public void testHelloWorld() throws Exception {

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet("http://localhost:8080/app/helloWorld");
            HttpResponse getResponse = client.execute(getRequest);
            BufferedReader rd = new BufferedReader
                                        (new InputStreamReader(getResponse.getEntity().getContent()));
            Assert.assertEquals(rd.readLine(), "Hello World", "The test webapp should return 'Hello World'");
        }

        @AfterMethod
        public void shutdownServer() throws Exception {
            server.stop();
        }
    }

