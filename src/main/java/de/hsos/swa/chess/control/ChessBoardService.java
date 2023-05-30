package de.hsos.swa.chess.control;

import de.hsos.swa.chess.entity.Field;
import de.hsos.swa.chess.shared.BoardPosition;
import de.hsos.swa.chess.shared.ChessPiece;

import javax.ws.rs.sse.SseEventSink;
import java.util.Optional;

public interface ChessBoardService {
    public Optional<Field[][]> getBoard(Long gameId);
    public void subscribeToBoard(Long gameId, SseEventSink sink);
    public boolean moveChessPieceTo(Long gameId, Long playerId, BoardPosition pos, BoardPosition newPos);
    public boolean promotePawnTo(Long gameId, Long playerId, ChessPiece type);
}
