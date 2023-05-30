package de.hsos.swa.chess.gateway;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateGameDTO;
import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.shared.GameStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequestScoped
public class GameRepository {
    private static final Logger log = Logger.getLogger(GameRepository.class);

    @Inject
    @ConfigProperty(name = "game_lifetime_in_days", defaultValue = "31")
    Integer gameLifetime;

    @Inject
    EntityManager em;

    @Transactional
    public Game createGame(CreateGameDTO newGame, String newPlayerJoinToken, String newViewerJoinToken) {
        Game game = new Game();
        game.setGameName(newGame.gameName);
        game.setPlayerJoinToken(newPlayerJoinToken);
        game.setViewerJoinToken(newViewerJoinToken);
        em.persist(game);
        return game;
    }

    @Transactional
    public Game deleteGame(Long gameId) {
        Game game = em.find(Game.class, gameId);
        if(game != null) {
            em.remove(game);
        }
        return game;
    }

    @Transactional
    public Set<Long> deleteOldGames() {
        TypedQuery<Game> query = this.em.createQuery("" +
                "SELECT g " +
                "FROM Game g " +
                "WHERE createdAt < current_date - ?1 ", Game.class)
                .setParameter(1, gameLifetime);
        log.debug("deleting "+query.getResultList().size()+" old Games...");
        Set<Long> oldIds = new HashSet<>();
        for(Game game: query.getResultList()) {
            oldIds.add(game.getId());
            em.remove(game);
        }
        return oldIds;
    }

    public Game findById(Long id) {
        Game game = em.find(Game.class, id);
        return game;
    }

    public List<Game> findByName(String gameName) {
        TypedQuery<Game> query = this.em.createQuery("" +
                        "SELECT g " +
                        "FROM Game g " +
                        "WHERE gamename = ?1 ", Game.class)
                .setParameter(1, gameName);
        List<Game> games = query.getResultList();
        return games;
    }

    @Transactional
    public Game setStatus(Long gameId, GameStatus status) {
        Game game = em.find(Game.class, gameId);
        if(game == null || status == null) {
            return null;
        }
        game.setGameStatus(status);
        em.persist(game);
        return game;
    }

    public GameStatus getStatus(Long gameId) {
        Game game = em.find(Game.class, gameId);
        if(game == null) { return null; }
        GameStatus status = game.getGameStatus();
        return status;
    }

}