package de.hsos.swa.chess.boundary.rest;

import de.hsos.swa.chess.boundary.rest.dto.create.CreatePlayerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.PlayerResponseDTO;
import de.hsos.swa.chess.control.MessageService;
import de.hsos.swa.chess.control.PlayerService;

import de.hsos.swa.chess.gateway.PlayerRepository;
import de.hsos.swa.chess.shared.Color;
import de.hsos.swa.chess.shared.PlayerMessage;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.jboss.resteasy.reactive.server.jaxrs.RestResponseBuilderImpl;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.sse.SseEventSink;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@RequestScoped
@Path("game/{gameName}/player")
public class PlayerResource {
    private static final Logger log = Logger.getLogger(PlayerResource.class);

    @Inject
    PlayerService playerService;
    @Inject
    MessageService messageService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(value=2000, unit= ChronoUnit.MILLIS)
    public RestResponse<PlayerResponseDTO> joinGameAsPlayer(@Valid CreatePlayerDTO newPlayer, @RestPath String gameName) {
        log.info("name: "+newPlayer.playerName+", token: "+newPlayer.joinToken);
        Optional<PlayerResponseDTO> player = playerService.addPlayerToGame(newPlayer, gameName);
        if(player.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.CREATED, player.get());
    }

    @POST
    @RolesAllowed("player")
    @Path("message/{message}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Counted(name = "publishedMessages", description = "How many messages have been published.")
    @Timed(name = "publishedMessagesDuration", description = "How long does it take to " +
            "publish a message.", unit = MetricUnits.MILLISECONDS)
    @Timeout(value=2000, unit= ChronoUnit.MILLIS)
    public RestResponse<PlayerMessage> publishMessage(@RestPath String gameName, @RestPath PlayerMessage message,
                                                      @Context JsonWebToken jwt) {
        if(message == null) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        Long playerId = Long.valueOf(jwt.getClaim("playerId").toString());
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        Optional<PlayerMessage> msg = messageService.publishMessage(playerId, gameId, message);
        if(msg.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.CREATED, msg.get());
    }

    @GET
    @RolesAllowed("player")
    @Path("events")
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public void subscribeToOpponentsMessages(@RestPath String gameName, @Context SseEventSink sink,
                                             @Context JsonWebToken jwt) {
        Long playerId = Long.valueOf(jwt.getClaim("playerId").toString());
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        messageService.subscribeToOpponentsMessages(playerId, gameId, sink);
    }

    @GET
    @RolesAllowed("viewer")
    @Path("playerOne/events")
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public void subscribeToPlayerOneMessages(@RestPath String gameName, @Context SseEventSink sink,
                                             @Context JsonWebToken jwt) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        messageService.subscribeToPlayerOneMessages(gameId, sink);
    }

    @GET
    @RolesAllowed("viewer")
    @Path("playerTwo/events")
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public void subscribeToPlayerTwoMessages(@RestPath String gameName, @Context SseEventSink sink,
                                             @Context JsonWebToken jwt) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        messageService.subscribeToPlayerTwoMessages(gameId, sink);
    }

}
