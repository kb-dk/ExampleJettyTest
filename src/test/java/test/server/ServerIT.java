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

public class ServerIT {

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