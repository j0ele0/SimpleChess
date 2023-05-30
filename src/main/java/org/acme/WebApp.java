package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.Objects.requireNonNull;

@Path("web")
@RequestScoped
public class WebApp {

    private final Template main;

    public WebApp(Template main) {
        this.main = requireNonNull(main, "page is required");
    }

    @GET
    @Path("/page")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getPageNr() {
        return main.instance();
    }

}
