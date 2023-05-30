package de.hsos.swa.chess.entity;

import de.hsos.swa.chess.shared.Color;
import de.hsos.swa.chess.shared.MinigameMessage;
import de.hsos.swa.chess.shared.MinigameStatus;
import io.smallrye.mutiny.tuples.Tuple2;
import org.jboss.resteasy.reactive.server.jaxrs.OutboundSseEventImpl;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;


public class Minigame {

    private Tuple2<Long, SseEventSink> playerOne = null;
    private Tuple2<Long, SseEventSink> playerTwo = null;
    private MinigameStatus minigameStatus = MinigameStatus.SETUP;
    private Color choosenColor = null;

    public Color getChoosenColor() {
        return choosenColor;
    }

    public void setChoosenColor(Color choosenColor) {
        this.choosenColor = choosenColor;
    }

    public Tuple2<Long, SseEventSink> getPlayerOne() {
        return playerOne;
    }

    public void setPlayerOne(Tuple2<Long, SseEventSink> playerOne) {
        this.playerOne = playerOne;
    }

    public Tuple2<Long, SseEventSink> getPlayerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(Tuple2<Long, SseEventSink> playerTwo) {
        this.playerTwo = playerTwo;
    }

    public MinigameStatus getMinigameStatus() {
        return minigameStatus;
    }

    public void setMinigameStatus(MinigameStatus minigameStatus) {
        this.minigameStatus = minigameStatus;
    }

}
