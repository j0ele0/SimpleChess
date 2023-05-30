package de.hsos.swa.chess.boundary.rest;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateGameDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.GameResponseDTO;
import de.hsos.swa.chess.control.GameService;
import de.hsos.swa.chess.entity.Game;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Path("game")
@RequestScoped
@Timeout(value=2000, unit= ChronoUnit.MILLIS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Spiel Ressource", description = "Verwalte dein Spiel")
public class GameResource {
    private static final Logger log = Logger.getLogger(GameResource.class);

    @Inject
    GameService gameService;

    @POST
    @Operation(
            summary = "Spiel erstellen",
            description = "Ein neues Spiel mit Namen anlegen. Wird kein Name angegeben wird automatisch einer generiert."
    )
    @APIResponse(
            responseCode = "201",
            description = "Spiel erstellt",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "204",
            description = "Spiel konnte nicht erstellt werden"
    )
    @Counted(name = "createdGames", description = "How many games have been created.")
    @Timed(name = "createGameDuration", description = "How long does it take to " +
            "create a new Game.", unit = MetricUnits.MILLISECONDS)
    public RestResponse<GameResponseDTO> createGame(@Valid CreateGameDTO newGame) {
        Optional<GameResponseDTO> game = Optional.empty();
        if(newGame == null || newGame.gameName == null || newGame.gameName.isBlank()) {
            game = gameService.createGameWithRandomName();
        }
        else {
            game = gameService.createGame(newGame);
        }
        if(game.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.CREATED, game.get());
    }

    @DELETE
    @RolesAllowed("player")
    @Counted(name = "deleteGames", description = "How many games have been deleted.")
    @Timed(name = "deleteGameDuration", description = "How long does it take to " +
            "delete a Game.", unit = MetricUnits.MILLISECONDS)
    @Operation(
            summary = "Dein Spiel löschen",
            description = "Löscht das dir zugeordnetes Spiel."
    )
    @APIResponse(
            responseCode = "200",
            description = "Spiel erfolgreich gelöscht",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "204",
            description = "Spiel konnte nicht gelöscht werden"
    )
    public RestResponse<GameResponseDTO> deleteYourGame(@Context JsonWebToken jwt) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        Optional<GameResponseDTO> game = gameService.deleteGame(gameId);
        if(game.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.OK, game.get());
    }

    @GET
    @RolesAllowed("player")
    @Operation(
            summary = "Spiel abfragen",
            description = "Fragt das dir zugeordnete Spiel ab"
    )
    @APIResponse(
            responseCode = "201",
            description = "Spiel abgefragt",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "204",
            description = "Spiel konnte nicht abgefragt werden"
    )
    public RestResponse<GameResponseDTO> getYourGame(@Context JsonWebToken jwt) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        Optional<GameResponseDTO> game = gameService.findById(gameId);
        if(game.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.OK, game.get());
    }


    @GET
    @DenyAll
    @Path("{gameName}")
    public RestResponse<List<Game>> findByName(@RestPath String gameName) {
        List<Game> games = gameService.findByName(gameName);
        return RestResponse.status(RestResponse.Status.OK, games);
    }
}
