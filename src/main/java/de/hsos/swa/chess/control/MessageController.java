package de.hsos.swa.chess.control;

import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.entity.Player;
import de.hsos.swa.chess.gateway.GameRepository;
import de.hsos.swa.chess.gateway.MessageRepository;
import de.hsos.swa.chess.gateway.MinigameRepository;
import de.hsos.swa.chess.gateway.PlayerRepository;
import de.hsos.swa.chess.shared.GameStatus;
import de.hsos.swa.chess.shared.PlayerMessage;
import de.hsos.swa.chess.shared.PlayerStatus;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.sse.SseEventSink;
import java.util.Optional;

@RequestScoped
public class MessageController implements MessageService {
    private static final Logger log = Logger.getLogger(MessageController.class);

    @Inject
    MessageRepository messageRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    GameRepository gameRepository;
    @Inject
    MinigameRepository minigameRepository;

    @Override
    public Optional<PlayerMessage> publishMessage(Long playerId, Long gameId, PlayerMessage playerMessage) {
        Player player = playerRepository.findById(playerId);
        //messageRepository.
        if(player == null) {
            log.info("cant publish message: player unknown");
            return Optional.empty();
        }

        switch(playerMessage) {
            case READY -> {
                if(player.getPlayerStatus().equals(PlayerStatus.NOT_READY)) {
                    playerRepository.setStatus(playerId, PlayerStatus.READY);
                    GameStatus gameStatus = gameRepository.getStatus(gameId);
                    PlayerStatus opponentStatus = playerRepository.getOpponentStatus(gameId, playerId);

                    if( gameStatus != null && gameStatus.equals(GameStatus.SETUP) &&
                        opponentStatus != null && opponentStatus.equals(PlayerStatus.READY)) {
                        gameRepository.setStatus(gameId, GameStatus.MINIGAME);
                        minigameRepository.createMinigame(gameId);
                    }
                }
                messageRepository.publishMessage(playerId, playerMessage.name());
                return Optional.of(playerMessage);
            }
            case GIVE_UP -> {
                messageRepository.publishMessage(playerId, playerMessage.name());
                Long opponentId = playerRepository.getOpponentId(gameId, playerId);
                if(opponentId != null) {
                    messageRepository.removeBroadcast(opponentId);
                }
                messageRepository.removeBroadcast(playerId);
                messageRepository.removeBroadcast(gameId);
                gameRepository.deleteGame(gameId);
                return Optional.of(playerMessage);

            }
            default -> {
                log.info("invalid message");
                return Optional.empty();
            }
        }
    }

    @Override
    public void subscribeToOpponentsMessages(Long playerId, Long gameId, SseEventSink sink) {
        Game game = gameRepository.findById(gameId);
        if(game == null) {
            log.info("cant subscribe to messages: game unknown");
            sink.close();
            return;
        }
        for(Player player: game.getPlayers()) {
            if(player.getId() != playerId) {
                if(messageRepository.subscribeToBroadcaster(player.getId(), sink)) {
                    return;
                }
            }
        }
        log.info("cant subscribe to messages: There is no registered Opponent");
        sink.close();
    }

    @Override
    public void subscribeToPlayerOneMessages(Long gameId, SseEventSink sink) {
        Game game = gameRepository.findById(gameId);
        if(game == null) {
            log.info("cant subscribe to messages: game unknown");
            sink.close();
            return;
        }
        if(game.getPlayers().size() >= 1) {
            messageRepository.subscribeToBroadcaster(game.getPlayers().get(0).getId(), sink);
            return;
        }
        log.info("cant subscribe to messages: playerOne does not exist");
        sink.close();
    }

    @Override
    public void subscribeToPlayerTwoMessages(Long gameId, SseEventSink sink) {
        Game game = gameRepository.findById(gameId);
        if(game == null) {
            log.info("cant subscribe to messages: game unknown");
            sink.close();
            return;
        }
        if(game.getPlayers().size() == 2) {
            messageRepository.subscribeToBroadcaster(game.getPlayers().get(1).getId(), sink);
            return;
        }
        log.info("cant subscribe to messages: playerTwo does not exist");
        sink.close();
    }


}
