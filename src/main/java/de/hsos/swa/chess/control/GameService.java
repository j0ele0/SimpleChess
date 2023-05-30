package de.hsos.swa.chess.control;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateGameDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.GameResponseDTO;
import de.hsos.swa.chess.entity.Game;

import java.util.List;
import java.util.Optional;

public interface GameService {
    public Optional<GameResponseDTO> createGame(CreateGameDTO newGame);
    public Optional<GameResponseDTO> deleteGame(Long gameId);
    public Optional<GameResponseDTO> createGameWithRandomName();
    public List<Game> findByName(String gameName);
    public Optional<GameResponseDTO> findById(Long gameId);
}
