package de.hsos.swa.chess.control;

import de.hsos.swa.chess.boundary.rest.dto.create.CreatePlayerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.PlayerResponseDTO;
import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.entity.Player;
import de.hsos.swa.chess.gateway.GameRepository;
import de.hsos.swa.chess.gateway.MessageRepository;
import de.hsos.swa.chess.gateway.PlayerRepository;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class PlayerController implements PlayerService{
    private static final Logger log = Logger.getLogger(PlayerController.class);

    @Inject
    @ConfigProperty(name = "jwt_lifetime_in_days", defaultValue = "30")
    Integer jwtLifetime;

    @Inject
    PlayerRepository playerRepository;
    @Inject
    GameRepository gameRepository;
    @Inject
    MessageRepository messageRepository;

    @Override
    public Optional<PlayerResponseDTO> addPlayerToGame(CreatePlayerDTO newPlayer, String gameName) {
        List<Game> games = gameRepository.findByName(gameName);
        for(Game game: games) {
            if(game.getPlayerJoinToken().equals(newPlayer.joinToken)) {
                if(game.getPlayers().size() >= 2) { return Optional.empty(); }
                if(game.getPlayers().size() == 1 && game.getPlayers().get(0).getName().equals(newPlayer.playerName)) {
                    newPlayer.playerName += "#2";
                }
                Player player = playerRepository.addPlayerToGame(newPlayer, game.getId());
                messageRepository.createBroadcast(player.getId());
                if(player == null) { return Optional.empty(); }
                String jwt = Jwt.claim("gameName", gameName).
                        claim("gameId", game.getId()).
                        claim("playerName", player.getName()).
                        claim("playerId", player.getId()).
                        groups("player").
                        expiresIn(Duration.ofDays(jwtLifetime)).
                        sign();
                PlayerResponseDTO playerDTO = new PlayerResponseDTO(player);
                playerDTO.jwt = jwt;
                return Optional.of(playerDTO);
            }
        }
        return Optional.empty();
    }

}
