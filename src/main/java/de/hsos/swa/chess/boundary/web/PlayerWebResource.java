package de.hsos.swa.chess.boundary.web;

import de.hsos.swa.chess.boundary.rest.dto.create.CreatePlayerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.PlayerResponseDTO;
import de.hsos.swa.chess.control.PlayerService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.jaxrs.RestResponseBuilderImpl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.util.Optional;

@RequestScoped
@Path("web/game/{gameName}/player")
public class PlayerWebResource {
    private static final Logger log = Logger.getLogger(PlayerWebResource.class);

    @Inject
    PlayerService playerService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse joinGameAsPlayerWeb(CreatePlayerDTO newPlayer, @RestPath String gameName) {
        Optional<PlayerResponseDTO> player = playerService.addPlayerToGame(newPlayer, gameName);
        if(player.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        NewCookie nc = new NewCookie("Bearer",player.get().jwt,"/game/"+gameName, "", null , 2592000, true, true);
        return RestResponseBuilderImpl.create(RestResponse.Status.CREATED).cookie(nc).build();
    }
}
