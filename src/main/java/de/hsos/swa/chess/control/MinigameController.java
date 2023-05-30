package de.hsos.swa.chess.control;

import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.gateway.*;
import de.hsos.swa.chess.shared.*;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.sse.SseEventSink;

@RequestScoped
public class MinigameController implements MinigameService{
    private static final Logger log = Logger.getLogger(MinigameController.class);

    @Inject
    MinigameRepository minigameRepository;
    @Inject
    GameRepository gameRepository;
    @Inject
    PlayerRepository playerRepository;

    @Override
    public void subscribeToMinigame(Long gameId, Long playerId, SseEventSink sink) {
        Game game = gameRepository.findById(gameId);
        if(game == null || !GameStatus.MINIGAME.equals(game.getGameStatus())) {
            sink.close();
            log.info("cant subscribe to minigame: game invalid");
            return;
        }
        if( minigameRepository.getPlayerOneId(gameId) == null ||
            playerId.equals(minigameRepository.getPlayerOneId(gameId))) {
            minigameRepository.subscribeAsPlayerOne(gameId, playerId, sink);
            log.info("subscribed to minigame as player one");
            if( MinigameStatus.SETUP.equals(minigameRepository.getMinigameStatus(gameId)) ||
                MinigameStatus.CHOOSING.equals(minigameRepository.getMinigameStatus(gameId))) {
                minigameRepository.setMinigameStatus(gameId, MinigameStatus.CHOOSING);
                minigameRepository.sendMessageToPlayerOne(gameId, MinigameMessage.CHOOSE_COLOR);
            }
            else {
                minigameRepository.sendMessageToPlayerOne(gameId, MinigameMessage.WAIT_FOR_OPPONENT);
            }
            return;
        }
        if( minigameRepository.getPlayerTwoId(gameId) == null ||
            playerId.equals(minigameRepository.getPlayerTwoId(gameId))) {
            minigameRepository.subscribeAsPlayerTwo(gameId, playerId, sink);
            log.info("subscribed to minigame as player two");
            if(MinigameStatus.GUESSING.equals(minigameRepository.getMinigameStatus(gameId))) {
                minigameRepository.sendMessageToPlayerTwo(gameId, MinigameMessage.GUESS_COLOR);
            }
            else {
                minigameRepository.sendMessageToPlayerTwo(gameId, MinigameMessage.WAIT_FOR_OPPONENT);
            }
            return;
        }
        sink.close();
        log.info("cant subscribeToMinigame: Minigame is full");
    }

    @Override
    public boolean pickColor(Long gameId, Long playerId, Color color) {
        if( playerId.equals(minigameRepository.getPlayerOneId(gameId)) &&
            MinigameStatus.CHOOSING.equals(minigameRepository.getMinigameStatus(gameId))) {
            if(chooseColor(gameId, color)) {
                minigameRepository.setMinigameStatus(gameId, MinigameStatus.GUESSING);
                minigameRepository.sendMessageToPlayerOne(gameId, MinigameMessage.WAIT_FOR_OPPONENT);
                minigameRepository.sendMessageToPlayerTwo(gameId, MinigameMessage.GUESS_COLOR);
            }
        }
        if( playerId.equals(minigameRepository.getPlayerTwoId(gameId)) &&
            MinigameStatus.GUESSING.equals(minigameRepository.getMinigameStatus(gameId))) {
            Long opponentId = minigameRepository.getPlayerOneId(gameId);
            if(guessColor(gameId, color)) {
                minigameRepository.sendMessageToPlayerOne(gameId, MinigameMessage.YOU_LOST);
                minigameRepository.sendMessageToPlayerTwo(gameId, MinigameMessage.YOU_WON);
                playerRepository.setStatus(playerId, PlayerStatus.ON_TURN);
                playerRepository.setColor(playerId, Color.WHITE);
                playerRepository.setColor(opponentId, Color.BLACK);
            }
            else {
                minigameRepository.sendMessageToPlayerOne(gameId, MinigameMessage.YOU_WON);
                minigameRepository.sendMessageToPlayerTwo(gameId, MinigameMessage.YOU_LOST);
                playerRepository.setStatus(opponentId, PlayerStatus.ON_TURN);
                playerRepository.setColor(playerId, Color.BLACK);
                playerRepository.setColor(opponentId, Color.WHITE);
            }
            gameRepository.setStatus(gameId, GameStatus.PLAYING);
            minigameRepository.deleteMinigame(gameId);
            return true;
        }
        return false;
    }

    private boolean chooseColor(Long gameId, Color color) {
        minigameRepository.setChoosenColor(gameId, color);
        log.info("choosing color: "+color.name());
        return true;
    }

    private boolean guessColor(Long gameId, Color color) {
        return color.equals(minigameRepository.getChoosenColor(gameId));
    }

}
