package org.jboss.resteasy.examples.links;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/link")
public class LinkResource {
    @GET
    public String get() {
        return "";
    }
}
