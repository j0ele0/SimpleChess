package de.hsos.swa.chess.control;

import de.hsos.swa.chess.boundary.rest.dto.create.CreatePlayerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.PlayerResponseDTO;
import de.hsos.swa.chess.entity.Player;

import java.util.Optional;

public interface PlayerService {
    public Optional<PlayerResponseDTO> addPlayerToGame(CreatePlayerDTO newPlayer, String gameName);

}
