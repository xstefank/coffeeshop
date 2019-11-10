package io.xstefank;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Path("/ping")
public class PingResource {
    
    @ConfigProperty(name = "test.value", defaultValue = "DEFAULT")
    String testValue;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Counted(name = "hello-counter")
    public String hello() {
        return "hello " + testValue;
    }
    
    @GET
    @Path("host")
    public String helloHost() throws UnknownHostException {
        return "hello from " + InetAddress.getLocalHost().getHostName();
    }
    
    @GET
    @Path("timed")
    @Timed
    public String timed() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "timed";
    }
    
    @ConfigProperty(name = "barista.url", defaultValue = "http://localhost:8081")
    String baristaURL;
    
    @GET
    @Path("call")
    @Retry(delay = 1000, maxRetries = 5)
    @Fallback(fallbackMethod = "fallback")
    public String callBarista() {
        Response response = ClientBuilder.newClient()
            .target(baristaURL)
                .path("/ping")
            .request()
            .get();

        if (response.getStatus() != 200) {
            throw new IllegalStateException("barista ping failed");
        }
        
        return response.readEntity(String.class);
    }
    
    private String fallback() {
        return "fallback string as we can't contact barista";
    }
}
