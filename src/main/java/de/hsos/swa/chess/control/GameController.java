package de.hsos.swa.chess.control;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateGameDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.GameResponseDTO;
import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.gateway.ChessBoardRepository;
import de.hsos.swa.chess.gateway.GameRepository;
import de.hsos.swa.chess.gateway.MessageRepository;

import net.bytebuddy.utility.RandomString;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.jboss.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;


@RequestScoped
public class GameController implements GameService{
    private static final Logger log = Logger.getLogger(GameController.class);

    @Inject
    @ConfigProperty(name = "jointoken_length", defaultValue = "5")
    Integer joinTokenLength;

    @Inject
    GameRepository gameRepository;
    @Inject
    ChessBoardRepository chessBoardRepository;
    @Inject
    MessageRepository messageRepository;

    @Override
    @Counted(name = "createdGamesWithFixedName", description = "How many games with fixed names have been created.")
    public Optional<GameResponseDTO> createGame(CreateGameDTO newGame) {
        String newPlayerJoinToken = RandomString.make(joinTokenLength);
        String newViewerJoinToken = RandomString.make(joinTokenLength);
        while(!areTokensAvailable(newPlayerJoinToken,newViewerJoinToken, newGame.gameName)) {
            newPlayerJoinToken = RandomString.make(joinTokenLength);
            newViewerJoinToken = RandomString.make(joinTokenLength);
        }
        Game game = gameRepository.createGame(newGame, newPlayerJoinToken, newViewerJoinToken);
        chessBoardRepository.initBoard(game.getId());
        messageRepository.createBroadcast(game.getId());
        GameResponseDTO gameDTO = new GameResponseDTO(game);
        return Optional.ofNullable(gameDTO);
    }

    @Override
    public Optional<GameResponseDTO> deleteGame(Long gameId) {
        Game game = gameRepository.deleteGame(gameId);
        if(game == null) {
            return Optional.empty();
        }
        return Optional.of(new GameResponseDTO(game));
    }

    @Override
    @Counted(name = "createdGamesWithRandomName", description = "How many games with random names have been created.")
    public Optional<GameResponseDTO> createGameWithRandomName() {
        String newGameName = RandomString.make(6);
        CreateGameDTO newGame = new CreateGameDTO();
        newGame.gameName = newGameName;
        return this.createGame(newGame);
    }

    private boolean areTokensAvailable(String tokenA, String tokenB, String gameName) {
        if(tokenA.equals(tokenB)) { return false;}
        for(Game game: gameRepository.findByName(gameName)) {
            if(game.getPlayerJoinToken().equals(tokenA) || game.getPlayerJoinToken().equals(tokenB) ||
               game.getViewerJoinToken().equals(tokenA) || game.getViewerJoinToken().equals(tokenB) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Game> findByName(String gameName) {
        List<Game> games = gameRepository.findByName(gameName);
        return games;
    }

    @Override
    public Optional<GameResponseDTO> findById(Long gameId) {
        Game game = gameRepository.findById(gameId);
        if(game == null) {
            return Optional.empty();
        }
        return Optional.of(new GameResponseDTO(game));
    }

}
