package com.redhat.cloud.notifications.sender;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/")
public class WebHook {

    @POST
    public Response hook(String payload) {

        System.out.println("-->>---------------\n   -->> Got  " + payload + "\n-->>-------------");
        return Response.noContent().build();
    }

}