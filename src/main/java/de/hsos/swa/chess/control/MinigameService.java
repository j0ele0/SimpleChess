package de.hsos.swa.chess.control;

import de.hsos.swa.chess.shared.Color;

import javax.ws.rs.sse.SseEventSink;
import java.util.Optional;

public interface MinigameService {
    public void subscribeToMinigame(Long gameId, Long playerId, SseEventSink sink);
    public boolean pickColor(Long gameId, Long playerId, Color color);
    //public boolean guessColor(Long gameId, Long playerId, Color color);
}
