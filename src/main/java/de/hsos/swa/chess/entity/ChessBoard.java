package de.hsos.swa.chess.entity;


import de.hsos.swa.chess.shared.BoardPosition;
import de.hsos.swa.chess.shared.ChessBoardAdapter;
import org.hibernate.annotations.Type;
import org.jboss.resteasy.reactive.server.jaxrs.OutboundSseEventImpl;
import org.jboss.resteasy.reactive.server.jaxrs.SseBroadcasterImpl;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.*;
import javax.transaction.TransactionScoped;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseBroadcaster;
import java.util.ArrayList;
import java.util.List;


@Entity(name = "ChessBoard")
@Table(name = "chessboard")
@JsonbTypeAdapter(value = ChessBoardAdapter.class)
public class ChessBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publisherSeq")
    @SequenceGenerator(name = "publisherSeq", sequenceName = "game_player_id_seq",allocationSize=1)
    @Column(name = "game_id")
    private Long id;

    @ElementCollection
    @CollectionTable(
            name="board",
            joinColumns=@JoinColumn(name="chessboard_id")
    )
    @OrderColumn
    private List<Field> fields = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
