package com.redhat.cloud.notifications.sender;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Just a sample web-hook that we can send to for demo purposes.
 */
@Path("/")
public class WebHook {

    @POST
    public Response hook(String payload) {

        System.out.println("-->>-----Incoming payload----------\n" + payload + "\n-->>------------------");
        return Response.noContent().build();
    }

}
