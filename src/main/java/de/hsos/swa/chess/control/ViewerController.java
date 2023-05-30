package de.hsos.swa.chess.control;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateViewerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.ViewerResponseDTO;
import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.gateway.GameRepository;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class ViewerController implements ViewerService {
    private static final Logger log = Logger.getLogger(ViewerController.class);

    @Inject
    @ConfigProperty(name = "jwt_lifetime_in_days", defaultValue = "30")
    Integer jwtLifetime;

    @Inject
    GameRepository gameRepository;

    @Override
    public Optional<ViewerResponseDTO> addViewerToGame(CreateViewerDTO newViewer, String gameName) {
        List<Game> games = gameRepository.findByName(gameName);
        for(Game game: games) {
            if(game.getViewerJoinToken().equals(newViewer.joinToken)) {
                String jwt = Jwt.claim("gameName", gameName).
                        claim("gameId", game.getId()).
                        groups("viewer").
                        expiresIn(jwtLifetime).
                        sign();
                ViewerResponseDTO viewerDTO = new ViewerResponseDTO();
                viewerDTO.jwt = jwt;
                return Optional.of(viewerDTO);
            }
        }
        return Optional.empty();
    }
}
