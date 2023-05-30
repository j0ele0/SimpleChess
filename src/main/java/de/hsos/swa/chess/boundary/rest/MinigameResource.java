package de.hsos.swa.chess.boundary.rest;

import de.hsos.swa.chess.control.MinigameController;
import de.hsos.swa.chess.control.MinigameService;
import de.hsos.swa.chess.shared.Color;
import io.smallrye.common.annotation.NonBlocking;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.time.temporal.ChronoUnit;

@RequestScoped
@RolesAllowed("player")
@Path("game/{gameName}/minigame")
public class MinigameResource {
    private static final Logger log = Logger.getLogger(MinigameResource.class);

    @Inject
    MinigameService minigameService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("events")
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public void subscribeToMinigame(@Context SseEventSink sink) {
        Long playerId = Long.valueOf(jwt.getClaim("playerId").toString());
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        minigameService.subscribeToMinigame(gameId, playerId, sink);
    }

    @POST
    @Path("{color}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(value=2000, unit= ChronoUnit.MILLIS)
    public RestResponse<Void> pickColor(@RestPath String gameName, @RestPath Color color) {
        Long playerId = Long.valueOf(jwt.getClaim("playerId").toString());
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        boolean successful = minigameService.pickColor(gameId, playerId, color);
        if(!successful) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.OK);
    }

}
