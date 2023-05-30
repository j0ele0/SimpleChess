package de.hsos.swa.chess.control;

import de.hsos.swa.chess.gateway.GameRepository;
import de.hsos.swa.chess.gateway.MessageRepository;
import de.hsos.swa.chess.gateway.MinigameRepository;
import de.hsos.swa.chess.gateway.PlayerRepository;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class GameValidator {

    @Inject
    GameRepository gameRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    MinigameRepository minigameRepository;

    @Blocking
    @Scheduled(cron="{cron.expr}")
    public void deleteOldGames() {
        Set<Long> oldIds = gameRepository.deleteOldGames();
        deleteOldMinigames(oldIds);
    }

    private void deleteOldMinigames(Set<Long> oldIds) {
        for(Long id: oldIds) {
            minigameRepository.deleteMinigame(id);
        }
    }

    @Blocking
    @Scheduled(cron="{cron.expr}")
    public void deleteOldBroadcasts() {
        Set<Long> playerIds = playerRepository.getAllPlayerIds();
        Set<Long> oldPlayerIds = new HashSet<>();
        for(Long playerId: messageRepository.getPublisherIds()) {
            if(!playerIds.contains(playerId)) {
                oldPlayerIds.add(playerId);
            }
        }
        messageRepository.removeOldBroadcasts(oldPlayerIds);
    }


}
