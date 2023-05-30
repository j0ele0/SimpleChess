package de.hsos.swa.chess.entity;



import de.hsos.swa.chess.shared.GameStatus;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


@Entity(name = "Game")
@Table(name = "game")
public class Game {

    @Id
    private Long id;
    private String gameName;
    private String playerJoinToken;
    private String viewerJoinToken;

    @Enumerated
    private GameStatus gameStatus = GameStatus.SETUP;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private List<Player> players = new ArrayList<>();

    @CreationTimestamp
    private Date createdAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private ChessBoard board = new ChessBoard();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getPlayerJoinToken() {
        return playerJoinToken;
    }

    public void setPlayerJoinToken(String playerJoinToken) {
        this.playerJoinToken = playerJoinToken;
    }

    public String getViewerJoinToken() {
        return viewerJoinToken;
    }

    public void setViewerJoinToken(String viewerJoinToken) {
        this.viewerJoinToken = viewerJoinToken;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus status) {
        this.gameStatus = status;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }
}
