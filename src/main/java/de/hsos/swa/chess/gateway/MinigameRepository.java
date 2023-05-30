package de.hsos.swa.chess.gateway;

import de.hsos.swa.chess.entity.Minigame;
import de.hsos.swa.chess.shared.Color;
import de.hsos.swa.chess.shared.MinigameMessage;
import de.hsos.swa.chess.shared.MinigameStatus;
import io.smallrye.mutiny.tuples.Tuple2;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.jaxrs.OutboundSseEventImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MinigameRepository {
    private static final Logger log = Logger.getLogger(MinigameRepository.class);
    private static final OutboundSseEvent.Builder builder = new OutboundSseEventImpl.BuilderImpl();

    private Map<Long, Minigame> minigames = new HashMap<>();

    public boolean subscribeAsPlayerOne(Long gameId, Long playerId, SseEventSink sink) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null) {
            return false;
        }
        minigame.setPlayerOne(Tuple2.of(playerId, sink));
        return true;
    }

    public boolean subscribeAsPlayerTwo(Long gameId, Long playerId, SseEventSink sink) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null) {
            return false;
        }
        minigame.setPlayerTwo(Tuple2.of(playerId, sink));
        return true;
    }

    public Long getPlayerOneId(Long gameId) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null || minigame.getPlayerOne() == null) {
            return null;
        }
        return minigame.getPlayerOne().getItem1();
    }

    public Long getPlayerTwoId(Long gameId) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null || minigame.getPlayerTwo() == null) {
            return null;
        }
        return minigame.getPlayerTwo().getItem1();
    }

    public boolean sendMessageToPlayerOne(Long gameId, MinigameMessage message) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null || minigame.getPlayerOne() == null) {
            return false;
        }
        minigame.getPlayerOne().getItem2().send(builder.data(message.name()).build());
        return true;
    }

    public boolean sendMessageToPlayerTwo(Long gameId, MinigameMessage message) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null || minigame.getPlayerTwo() == null) {
            return false;
        }
        minigame.getPlayerTwo().getItem2().send(builder.data(message.name()).build());
        return true;
    }

    public boolean setChoosenColor(Long gameId, Color color) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null) {
            return false;
        }
        minigame.setChoosenColor(color);
        return true;
    }

    public Color getChoosenColor(Long gameId) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null) {
            return null;
        }
        return minigame.getChoosenColor();
    }

    public boolean setMinigameStatus(Long gameId, MinigameStatus status) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null) {
            return false;
        }
        minigame.setMinigameStatus(status);
        return true;
    }

    public MinigameStatus getMinigameStatus(Long gameId) {
        Minigame minigame = minigames.get(gameId);
        if(minigame == null) {
            return null;
        }
        return minigame.getMinigameStatus();
    }

    public Minigame createMinigame(Long gameId) {
        Minigame minigame = new Minigame();
        minigames.put(gameId, minigame);
        return minigame;
    }

    public Minigame deleteMinigame(Long gameId) {
        Minigame minigame = minigames.remove(gameId);
        if(minigame.getPlayerOne() != null && !minigame.getPlayerOne().getItem2().isClosed()) {
            minigame.getPlayerOne().getItem2().close();
        }
        if(minigame.getPlayerTwo() != null && !minigame.getPlayerTwo().getItem2().isClosed()) {
            minigame.getPlayerTwo().getItem2().close();
        }
        return minigame;
    }

}
