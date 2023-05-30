package de.hsos.swa.chess.gateway;

import de.hsos.swa.chess.boundary.rest.dto.create.CreatePlayerDTO;
import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.entity.Player;
import de.hsos.swa.chess.shared.Color;
import de.hsos.swa.chess.shared.PlayerStatus;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequestScoped
public class PlayerRepository {
    private static final Logger log = Logger.getLogger(PlayerRepository.class);

    @Inject
    EntityManager em;

    @Transactional
    public Player addPlayerToGame(CreatePlayerDTO newPlayer, Long gameId) {
        Game game = em.find(Game.class, gameId);
        if(game == null || game.getPlayers().size() >= 2) {
            return null;
        }
        Player player = new Player();
        player.setName(newPlayer.playerName);
        game.getPlayers().add(player);
        em.persist(game);
        log.info("new added Player has id: "+player.getId() );
        return player;
    }

    public Player findById(Long playerId) {
        Player player = em.find(Player.class, playerId);
        return player;
    }

    @Transactional
    public Player deletePlayer(Long playerId) {
        Player player = em.find(Player.class, playerId);
        if(player != null) {
            em.remove(player);
        }
        return player;
    }

    public Set<Long> getAllPlayerIds() {
        TypedQuery<Long> query = this.em.createQuery("" +
                        "SELECT p.id " +
                        "FROM Player p ", Long.class);
        Set<Long> playerIds = new HashSet<>(query.getResultList());
        return playerIds;
    }

    public PlayerStatus getStatus(Long playerId) {
        Player player = em.find(Player.class, playerId);
        if(player == null) {
            return null;
        }
        return player.getPlayerStatus();
    }

    @Transactional
    public Player setStatus(Long playerId, PlayerStatus status) {
        Player player = em.find(Player.class, playerId);
        if(player == null || status == null) {
            return null;
        }
        player.setPlayerStatus(status);
        em.persist(player);
        return player;
    }

    public Long getOpponentId(Long gameId, Long playerId) {
        TypedQuery<Long> query = this.em.createQuery("" +
                "SELECT p.id " +
                "FROM Player p " +
                "WHERE game_id = ?1 " +
                "AND id != ?2 ", Long.class).
                setParameter(1, gameId).
                setParameter(2, playerId);
        if(query.getResultList().isEmpty()) {
            return null;
        }
        Long opponentId = query.getSingleResult();
        return opponentId;
    }

    public PlayerStatus getOpponentStatus(Long gameId, Long playerId) {
        TypedQuery<PlayerStatus> query = this.em.createQuery("" +
                        "SELECT p.playerStatus " +
                        "FROM Player p " +
                        "WHERE game_id = ?1 " +
                        "AND id != ?2 ", PlayerStatus.class).
                setParameter(1, gameId).
                setParameter(2, playerId);
        if(query.getResultList() == null || query.getResultList().isEmpty()) {
            return null;
        }
        PlayerStatus opponentStatus = query.getSingleResult();
        return opponentStatus;
    }

    @Transactional
    public Player setColor(Long playerId, Color color) {
        Player player = em.find(Player.class, playerId);
        if(player == null) {
            return null;
        }
        player.setColor(color);
        em.persist(player);
        return player;
    }

    public Color getColor(Long playerId) {
        Player player = em.find(Player.class, playerId);
        if(player == null) {
            return null;
        }
        Color color = player.getColor();
        return color;
    }


}
