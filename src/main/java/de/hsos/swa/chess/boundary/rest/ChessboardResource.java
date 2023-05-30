package de.hsos.swa.chess.boundary.rest;

import de.hsos.swa.chess.control.ChessBoardService;
import de.hsos.swa.chess.entity.ChessBoard;
import de.hsos.swa.chess.entity.Field;
import de.hsos.swa.chess.shared.BoardPosition;
import de.hsos.swa.chess.shared.ChessPiece;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@RequestScoped
@Path("game/{gameName}/chessboard")
public class ChessboardResource {
    private static final Logger log = Logger.getLogger(ChessboardResource.class);

    @Inject
    ChessBoardService chessBoardService;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed({"player","viewer"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timeout(value=2000, unit= ChronoUnit.MILLIS)
    public RestResponse<Field[][]> getChessboard() {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        Optional<Field[][]> board = chessBoardService.getBoard(gameId);
        if(board.isEmpty()) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.OK, board.get());
    }

    @GET
    @Path("events")
    @RolesAllowed({"player","viewer"})
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public void subscribeToChessboard(@Context SseEventSink sink) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        chessBoardService.subscribeToBoard(gameId, sink);
    }

    @POST
    @Path("piece/{pos}/target/{newPos}")
    @RolesAllowed("player")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Counted(name = "movedChessPieces", description = "How many chessPieces have been moved.")
    @Timed(name = "moveChessPieceDuration", description = "How long does it take to " +
            "move a chessPiece.", unit = MetricUnits.MILLISECONDS)
    @Timeout(value=3000, unit= ChronoUnit.MILLIS)
    public RestResponse<ChessBoard> moveChessPieceTo(@RestPath String gameName, @RestPath BoardPosition pos,
                                                     @RestPath BoardPosition newPos) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        Long playerId = Long.valueOf(jwt.getClaim("playerId").toString());
        boolean successful = chessBoardService.moveChessPieceTo(gameId, playerId, pos, newPos);
        if(!successful) {
            return RestResponse.status(RestResponse.Status.BAD_REQUEST);
        }
        return RestResponse.status(RestResponse.Status.OK);
    }

    @POST
    @Path("piece/{pos}/promote/{chessPiece}")
    @RolesAllowed("player")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Counted(name = "promotedPawns", description = "How many pawns have been promoted.")
    @Timeout(value=2000, unit= ChronoUnit.MILLIS)
    public RestResponse<ChessBoard> promotePawn(@RestPath String gameName, @RestPath ChessPiece chessPiece) {
        Long gameId = Long.valueOf(jwt.getClaim("gameId").toString());
        Long playerId = Long.valueOf(jwt.getClaim("playerId").toString());
        boolean successful = chessBoardService.promotePawnTo(gameId, playerId, chessPiece);
        if(successful) {
            return RestResponse.status(RestResponse.Status.NO_CONTENT);
        }
        return RestResponse.status(RestResponse.Status.OK);
    }

}
