package de.hsos.swa.chess.boundary.rest.dto.response;

import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.entity.Player;
import io.quarkus.resteasy.reactive.jackson.SecureField;

import java.util.ArrayList;
import java.util.List;


public class GameResponseDTO {
    public String gameName;
    public String playerJoinToken;
    public String viewerJoinToken;
    public List<PlayerResponseDTO> players;

    public GameResponseDTO(Game game) {
        this.gameName = game.getGameName();
        this.playerJoinToken = game.getPlayerJoinToken();
        this.viewerJoinToken = game.getViewerJoinToken();
        this.players = new ArrayList<>();
        for(Player player: game.getPlayers()) {
            this.players.add(new PlayerResponseDTO(player));
        }
    }

}
