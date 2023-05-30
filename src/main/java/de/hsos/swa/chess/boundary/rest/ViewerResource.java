package de.hsos.swa.chess.boundary.rest;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateViewerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.ViewerResponseDTO;
import de.hsos.swa.chess.control.ViewerService;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RequestScoped
@Path("game/{gameName}/viewer")
public class ViewerResource {
    private static final Logger log = Logger.getLogger(ViewerResource.class);

    @Inject
    ViewerService viewerService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Counted(name = "joinedViewers", description = "How many viewers join to games.")
    @Timed(name = "joinAsViewerDuration", description = "How long does it take to " +
            "join to a Game.", unit = MetricUnits.MILLISECONDS)
    @Timeout(value=2000, unit= ChronoUnit.MILLIS)
    public RestResponse<ViewerResponseDTO> addViewerToGame(@Valid CreateViewerDTO newViewer, @RestPath String gameName) {
        Optional<ViewerResponseDTO> jwt = viewerService.addViewerToGame(newViewer, gameName);
        if(jwt.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.CREATED, jwt.get());
    }


}
