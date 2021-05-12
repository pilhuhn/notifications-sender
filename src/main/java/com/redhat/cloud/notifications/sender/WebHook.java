package com.redhat.cloud.notifications.sender;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Just a sample web-hook that we can send to for demo purposes.
 */
@Path("/")
public class WebHook {

    static Map<String,String> cache = new HashMap<>();

    @POST
    @Path("/{p:/?}{id}")
    public Response hook(@PathParam("id") String id, String payload) {

        if (id!=null) {
            cache.put(id, payload);
        }

        System.out.println("-->>-----Incoming payload----------\n"
                + "-->> Id = " + id + "\n"
                + payload + "\n-->>------------------");
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}")
    public Response getHook(@PathParam("id") String id) {
        if (cache.containsKey(id)) {
            return Response.ok().entity(cache.get(id)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}
