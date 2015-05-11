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
