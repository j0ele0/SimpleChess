package de.hsos.swa.chess.entity;


import de.hsos.swa.chess.shared.Color;
import de.hsos.swa.chess.shared.PlayerStatus;

import javax.persistence.*;


@Entity(name = "Player")
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publisherSeq")
    @SequenceGenerator(name = "publisherSeq", sequenceName = "game_player_id_seq",allocationSize=1)
    private Long id;
    private String name;
    @Enumerated
    private PlayerStatus playerStatus = PlayerStatus.NOT_READY;
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    private Game game;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }
}
