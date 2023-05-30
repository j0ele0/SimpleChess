package de.hsos.swa.chess.control;

import de.hsos.swa.chess.shared.PlayerMessage;

import javax.ws.rs.sse.SseEventSink;
import java.util.Optional;

public interface MessageService {
    public Optional<PlayerMessage> publishMessage(Long playerId, Long gameId, PlayerMessage playerMessage);
    public void subscribeToOpponentsMessages(Long playerId, Long gameId, SseEventSink sink);
    public void subscribeToPlayerOneMessages(Long gameId, SseEventSink sink);
    public void subscribeToPlayerTwoMessages(Long gameId, SseEventSink sink);
}
