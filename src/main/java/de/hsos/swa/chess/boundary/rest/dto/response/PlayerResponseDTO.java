package de.hsos.swa.chess.boundary.rest.dto.response;

import de.hsos.swa.chess.entity.Player;

public class PlayerResponseDTO {
    public String playerName;
    public String jwt;

    public PlayerResponseDTO(Player player) {
        this.playerName = player.getName();
    }
}
