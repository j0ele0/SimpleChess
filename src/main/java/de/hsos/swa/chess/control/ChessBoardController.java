package de.hsos.swa.chess.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsos.swa.chess.entity.Field;
import de.hsos.swa.chess.gateway.ChessBoardRepository;
import de.hsos.swa.chess.gateway.GameRepository;
import de.hsos.swa.chess.gateway.MessageRepository;
import de.hsos.swa.chess.gateway.PlayerRepository;
import de.hsos.swa.chess.shared.*;
import io.smallrye.mutiny.tuples.Tuple2;
import org.jboss.logging.Logger;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.sse.SseEventSink;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class ChessBoardController implements ChessBoardService{
    private static final Logger log = Logger.getLogger(ChessBoardController.class);

    @Inject
    ChessBoardRepository chessBoardRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    GameRepository gameRepository;

    @Override
    public Optional<Field[][]> getBoard(Long gameId) {
        Field[][] board = chessBoardRepository.getBoard(gameId);
        return Optional.ofNullable(board);
    }

    @Override
    public void subscribeToBoard(Long gameId, SseEventSink sink) {
        if(messageRepository.subscribeToBroadcaster(gameId, sink)) {
            Field[][] board = chessBoardRepository.getBoard(gameId);
            this.sendBoard(gameId, board);
            return;
        }
        log.debug("cant subscribe to chessboard: chessboard not available");
        sink.close();
    }

    @Override
    public boolean moveChessPieceTo(Long gameId, Long playerId, BoardPosition pos, BoardPosition newPos) {
        PlayerStatus status = playerRepository.getStatus(playerId);
        if(!PlayerStatus.ON_TURN.equals(status)) {
            log.debug("player is not on turn");
            return false;
        }
        Field[][] board = chessBoardRepository.getBoard(gameId);
        Color color = playerRepository.getColor(playerId);
        if(board == null || color == null) { return false;}
        Tuple2<Integer, Integer> coordinates = this.boardPosToCoordinates(pos);
        Tuple2<Integer, Integer> targetCoordinates = this.boardPosToCoordinates(newPos);

        if( !isPieceFriendly(color, board, coordinates) ||
            isPieceFriendly(color, board, targetCoordinates) ||
            !isCoordinateOnBoard(coordinates) || !isCoordinateOnBoard(targetCoordinates) ||
            (coordinates.getItem1().equals(targetCoordinates.getItem1()) &&
             coordinates.getItem2().equals(targetCoordinates.getItem2())) ) {
            log.debug("selected piece or target invalid");
            return false;
        }
        Field field = board[coordinates.getItem1()][coordinates.getItem2()];
        Field targetField = board[targetCoordinates.getItem1()][targetCoordinates.getItem2()];
        boolean canPiecePerformMovement = switch(field.getChessPiece()) {
            case PAWN -> {
                if(color.equals(Color.WHITE)) {
                    // is pawn moving one step, capturing or en passant
                    if(this.canWhitePawnPerformeMove(coordinates, targetCoordinates, targetField)) {
                        yield true;
                    }
                    if(this.canWhitePawnCaptureEnPassant(coordinates, targetCoordinates, targetField)) {
                        yield true;
                    }
                    // is pawn moving two fields forward
                    List<Field> pathFields = this.getPathFields(board, coordinates, targetCoordinates);
                    if(pathFields == null || pathFields.size() != 1) {
                        yield false;
                    }
                    Field pathField = pathFields.get(0);
                    if(this.canWhitePawnMoveTwoFieldsForward(coordinates, targetCoordinates, pathField, targetField)) {
                        board[coordinates.getItem1()][coordinates.getItem2()+1].setEnPassant(true);
                        board[coordinates.getItem1()][coordinates.getItem2()+1].setColor(color);
                        yield true;
                    }
                    yield false;
                }
                else {
                    // is pawn moving one step, capturing or en passant
                    if(this.canBlackPawnPerformeMove(coordinates, targetCoordinates, targetField)) {
                        yield true;
                    }
                    if(this.canBlackPawnCaptureEnPassant(coordinates, targetCoordinates, targetField)) {
                        yield true;
                    }
                    // is pawn moving two fields forward
                    List<Field> pathFields = this.getPathFields(board, coordinates, targetCoordinates);
                    if(pathFields == null || pathFields.size() != 1) {
                        yield false;
                    }
                    Field pathField = pathFields.get(0);
                    if(this.canBlackPawnMoveTwoFieldsForward(coordinates, targetCoordinates, pathField, targetField)) {
                        board[coordinates.getItem1()][coordinates.getItem2()-1].setEnPassant(true);
                        board[coordinates.getItem1()][coordinates.getItem2()-1].setColor(color);
                        yield true;
                    }
                    yield false;
                }
            }
            case ROOK -> {
                if(!this.isPieceMovingStraight(coordinates, targetCoordinates)) {
                    yield false;
                }
                if(!this.isThePathFree(board, coordinates, targetCoordinates)) {
                    yield false;
                }
                yield true;
            }
            case KNIGHT -> {
                if(!this.isPieceMovementKnightLike(coordinates, targetCoordinates)) {
                    yield false;
                }
                yield true;
            }
            case BISHOP -> {
                if(!this.isPieceMovingDiagonal(coordinates, targetCoordinates)) {
                    yield false;
                }
                if(!this.isThePathFree(board, coordinates, targetCoordinates)) {
                    yield false;
                }
                yield true;
            }
            case QUEEN -> {
                if( !this.isPieceMovingStraight(coordinates, targetCoordinates) &&
                    !this.isPieceMovingDiagonal(coordinates, targetCoordinates)) {
                    yield false;
                }
                if(!this.isThePathFree(board, coordinates, targetCoordinates)) {
                    yield false;
                }
                yield true;
            }
            case KING -> {
                if( !this.isPieceMovementKingLike(coordinates, targetCoordinates)) {
                    yield false;
                }
                yield true;
            }
        };

        if(!canPiecePerformMovement) {
            log.debug("invalid move: "+field.getChessPiece().name()+" from "+pos.name()+" to "+newPos.name());
            return false;
        }

        if(ChessPiece.KING.equals(targetField.getChessPiece())) {
            Long opponentId = playerRepository.getOpponentId(gameId, playerId);
            messageRepository.publishMessage(opponentId, "You Won.");
            messageRepository.publishMessage(playerId, "You Lost.");
            this.removeOldEnPassant(board, color);
            this.sendBoard(gameId, board);
            chessBoardRepository.setBoard(gameId,board);

            messageRepository.removeBroadcast(playerId);
            messageRepository.removeBroadcast(opponentId);
            messageRepository.removeBroadcast(gameId);
            gameRepository.deleteGame(gameId);
            return true;
        }

        log.debug("move "+field.getChessPiece()+" from "+pos.name()+" to "+newPos.name());
        this.performChessMove(board, coordinates, targetCoordinates);


        // can pawn promote
        if( ((color.equals(Color.WHITE) && targetCoordinates.getItem2().equals(7)) ||
            (color.equals(Color.BLACK) && targetCoordinates.getItem2().equals(0))) &&
            ChessPiece.PAWN.equals( targetField.getChessPiece() )) {
            playerRepository.setStatus(playerId, PlayerStatus.PROMOTING_PAWN);
        }
        else {
            playerRepository.setStatus(playerId, PlayerStatus.READY);
            Long opponentId = playerRepository.getOpponentId(gameId, playerId);
            playerRepository.setStatus(opponentId, PlayerStatus.ON_TURN);
        }

        this.removeOldEnPassant(board, color);
        this.sendBoard(gameId, board);
        chessBoardRepository.setBoard(gameId,board);
        return true;
    }

    @Override
    public boolean promotePawnTo(Long gameId, Long playerId, ChessPiece chessPiece) {
        if( !PlayerStatus.PROMOTING_PAWN.equals(playerRepository.getStatus(playerId)) ||
            chessPiece == null || ChessPiece.PAWN.equals(chessPiece)) {
            return false;
        }
        Field[][] board = chessBoardRepository.getBoard(gameId);
        Color color = playerRepository.getColor(playerId);
        if(board == null || color == null) {return false;}
        if(Color.WHITE.equals(color)) {
            for(int i=0; i<8; i++) {
                if(board[i][7].getChessPiece() != null && ChessPiece.PAWN.equals(board[i][7].getChessPiece())) {
                    board[i][7].setChessPiece(chessPiece);
                    this.sendBoard(gameId, board);
                    chessBoardRepository.setBoard(gameId,board);
                    playerRepository.setStatus(playerId, PlayerStatus.READY);
                    Long opponentId = playerRepository.getOpponentId(gameId, playerId);
                    playerRepository.setStatus(opponentId, PlayerStatus.ON_TURN);
                    return true;
                }
            }
        }
        else {
            for(int i=0; i<8; i++) {
                if(board[i][0].getChessPiece() != null && ChessPiece.PAWN.equals(board[i][0].getChessPiece())) {
                    board[i][0].setChessPiece(chessPiece);
                    this.sendBoard(gameId, board);
                    chessBoardRepository.setBoard(gameId,board);
                    playerRepository.setStatus(playerId, PlayerStatus.READY);
                    Long opponentId = playerRepository.getOpponentId(gameId, playerId);
                    playerRepository.setStatus(opponentId, PlayerStatus.ON_TURN);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean sendBoard(Long gameId, Field[][] board) {
        ObjectMapper o = new ObjectMapper();
        String jsonStr = new String();
        try {
            jsonStr = o.writeValueAsString(board);
        } catch(Exception ex) {
            log.error(ex);
        }
        messageRepository.publishMessage(gameId, jsonStr);
        return true;
    }

    private List<Field> boardToFields(Field[][] board) {
        List<Field> fields = new ArrayList<>();
        for(int row=0; row<8; row++) {
            for(int i=0; i<8; i++) {
                fields.add(board[i][row]);
            }
        }
        return fields;
    }

    private Tuple2<Integer, Integer> boardPosToCoordinates(BoardPosition pos) {
        int file = pos.ordinal() / 8; // spalte
        int rank = pos.ordinal() % 8; // reihe
        Tuple2<Integer,Integer> cord = Tuple2.of(file,rank);
        return cord;
    }

    private boolean isPieceFriendly(Color playerColor, Field[][] board, Tuple2<Integer, Integer> coordinates) {
        Color pieceColor = board[coordinates.getItem1()][coordinates.getItem2()].getColor();
        log.debug("playerColor: "+playerColor+", pieceColor: "+pieceColor+", equal: "+playerColor.equals(pieceColor));
        log.debug("Field info: "+board[coordinates.getItem1()][coordinates.getItem2()].toString());
        return playerColor.equals(pieceColor);
    }

    private boolean isCoordinateOnBoard(Tuple2<Integer, Integer> coordinates) {
        if(coordinates.getItem1() < 0 || coordinates.getItem1() > 7 ||
           coordinates.getItem2() < 0 || coordinates.getItem2() > 7) {
            return false;
        }
        return true;
    }

    private boolean canWhitePawnPerformeMove(Tuple2<Integer, Integer> coordinates,
                                             Tuple2<Integer, Integer> targetCoordinates,
                                             Field targetField) {
        // one step forward
        if( targetCoordinates.getItem2().equals(coordinates.getItem2()+1)   &&
            targetCoordinates.getItem1().equals(coordinates.getItem1())     &&
            targetField.getChessPiece() == null) {
            log.debug("pawn moves one step forward");
            return true;
        }

        // capturing
        if( targetCoordinates.getItem2().equals(coordinates.getItem2()+1)   &&
            (targetCoordinates.getItem1().equals(coordinates.getItem1()+1)  ||
            targetCoordinates.getItem1().equals(coordinates.getItem1()-1))  &&
            !(targetField.getChessPiece() == null)) {
            log.debug("pawn captures +"+targetField.getChessPiece().name());
            return true;
        }
        return false;
    }

    private boolean canWhitePawnCaptureEnPassant(Tuple2<Integer, Integer> coordinates,
                                                    Tuple2<Integer, Integer> targetCoordinates,
                                                    Field targetField) {
        if( targetField.isEnPassant() && targetCoordinates.getItem2().equals(coordinates.getItem2()+1)  &&
                (targetCoordinates.getItem1().equals(coordinates.getItem1()+1)  ||
                        targetCoordinates.getItem1().equals(coordinates.getItem1()-1) ) ) {
            log.debug("pawn en passant");
            return true;
        }
        return false;
    }

    private boolean canWhitePawnMoveTwoFieldsForward(Tuple2<Integer, Integer> coordinates,
                                                  Tuple2<Integer, Integer> targetCoordinates,
                                                  Field pathField, Field targetField) {
        if( targetCoordinates.getItem2().equals(coordinates.getItem2()+2)   &&
            targetCoordinates.getItem1().equals(coordinates.getItem1())     &&
            coordinates.getItem2().equals(1) &&
            pathField.getChessPiece() == null && targetField.getChessPiece() == null) {
            log.debug("pawn moves two steps forward");
            return true;
        }
        return false;
    }

    private boolean canBlackPawnPerformeMove(Tuple2<Integer, Integer> coordinates,
                                             Tuple2<Integer, Integer> targetCoordinates,
                                             Field targetField) {
        // one step forward
        if( targetCoordinates.getItem2().equals(coordinates.getItem2()-1)   &&
                targetCoordinates.getItem1().equals(coordinates.getItem1())     &&
                targetField.getChessPiece() == null) {
            log.debug("pawn moves one step forward");
            return true;
        }
        // capturing
        if( targetCoordinates.getItem2().equals(coordinates.getItem2()-1)   &&
            (targetCoordinates.getItem1().equals(coordinates.getItem1()+1)  ||
            targetCoordinates.getItem1().equals(coordinates.getItem1()-1))  &&
            !(targetField.getChessPiece() == null)) {
            log.debug("pawn captures +"+targetField.getChessPiece().name());
            return true;
        }
        return false;
    }

    private boolean canBlackPawnCaptureEnPassant(Tuple2<Integer, Integer> coordinates,
                                                 Tuple2<Integer, Integer> targetCoordinates,
                                                 Field targetField) {
        if( targetField.isEnPassant() && targetCoordinates.getItem2().equals(coordinates.getItem2()-1)  &&
                (targetCoordinates.getItem1().equals(coordinates.getItem1()+1)  ||
                        targetCoordinates.getItem1().equals(coordinates.getItem1()-1) ) ) {
            log.debug("pawn en passant");
            return true;
        }
        return false;
    }

    private boolean canBlackPawnMoveTwoFieldsForward(Tuple2<Integer, Integer> coordinates,
                                                     Tuple2<Integer, Integer> targetCoordinates,
                                                     Field pathField, Field targetField) {
        if( targetCoordinates.getItem2().equals(coordinates.getItem2()-2)   &&
            targetCoordinates.getItem1().equals(coordinates.getItem1())     &&
            coordinates.getItem2().equals(6) &&
            pathField.getChessPiece() == null && targetField.getChessPiece() == null) {
            log.debug("pawn moves two steps forward");
            return true;
        }
        return false;
    }

    private List<Field> getPathFields(Field[][] board, Tuple2<Integer, Integer> coordinates,
                                      Tuple2<Integer, Integer> targetCoordinates) {
        List<Field> fields = new ArrayList<>();
        // move straight:
        // in a file:
        if(coordinates.getItem1().equals(targetCoordinates.getItem1())) {
            // upward:
            if(coordinates.getItem2().compareTo(targetCoordinates.getItem2()) < 0 ) {
                for(int i=1; coordinates.getItem2()+i < targetCoordinates.getItem2(); i++) {
                    fields.add(board[coordinates.getItem1()][coordinates.getItem2()+i]);
                }
            }
            // downward:
            else {
                for(int i=1; coordinates.getItem2()-i > targetCoordinates.getItem2(); i++) {
                    fields.add(board[coordinates.getItem1()][coordinates.getItem2()-i]);
                }
            }
        }
        // in a rank:
        else if(coordinates.getItem2().equals(targetCoordinates.getItem2())){
            // right
            if(coordinates.getItem1().compareTo(targetCoordinates.getItem1()) < 0) {
                for(int i=1; coordinates.getItem1()+i < targetCoordinates.getItem1(); i++) {
                    fields.add(board[coordinates.getItem1()+i][coordinates.getItem2()]);
                }
            }
            // left
            else {
                for(int i=1; coordinates.getItem1()-i > targetCoordinates.getItem1(); i++) {
                    fields.add(board[coordinates.getItem1()-i][coordinates.getItem2()]);
                }
            }
        }
        // move diagonal:
        else {
            int x = Math.abs(coordinates.getItem1() - targetCoordinates.getItem1());
            int y = Math.abs(coordinates.getItem2() - targetCoordinates.getItem2());
            if(x != y) {
                log.info("movement is not straight diagonally(abs(x)!=abs(y))");
                return null;
            }
            // upward-right:
            if( coordinates.getItem1().compareTo(targetCoordinates.getItem1()) < 0 &&
                coordinates.getItem2().compareTo(targetCoordinates.getItem2()) < 0) {
                for(int i=1; i < x; i++) {
                    fields.add(board[coordinates.getItem1()+i][coordinates.getItem2()+i]);
                }
            }
            // upward-left:
            else if( coordinates.getItem1().compareTo(targetCoordinates.getItem1()) > 0 &&
                     coordinates.getItem2().compareTo(targetCoordinates.getItem2()) < 0) {
                for(int i=1; i < x; i++) {
                    fields.add(board[coordinates.getItem1()-i][coordinates.getItem2()+i]);
                }
            }
            // downward-right:
            else if( coordinates.getItem1().compareTo(targetCoordinates.getItem1()) < 0 &&
                    coordinates.getItem2().compareTo(targetCoordinates.getItem2()) > 0) {
                for(int i=1; i < x; i++) {
                    fields.add(board[coordinates.getItem1()+i][coordinates.getItem2()-i]);
                }
            }
            // downward-right:
            else {
                for(int i=1; i < x; i++) {
                    fields.add(board[coordinates.getItem1()-i][coordinates.getItem2()-i]);
                }
            }
        }
        return fields;
    }

    private void performChessMove(Field[][] board, Tuple2<Integer, Integer> coordinates,
                                     Tuple2<Integer, Integer> targetCoordinates) {
        ChessPiece piece = board[coordinates.getItem1()][coordinates.getItem2()].getChessPiece();
        Color color = board[coordinates.getItem1()][coordinates.getItem2()].getColor();
        if(board[targetCoordinates.getItem1()][targetCoordinates.getItem2()].isEnPassant()) {
            if(Color.WHITE.equals(color)) {
                board[targetCoordinates.getItem1()][targetCoordinates.getItem2()-1].setChessPiece(null);
                board[targetCoordinates.getItem1()][targetCoordinates.getItem2()-1].setColor(null);
            }
            else {
                board[targetCoordinates.getItem1()][targetCoordinates.getItem2()+1].setChessPiece(null);
                board[targetCoordinates.getItem1()][targetCoordinates.getItem2()+1].setColor(null);
            }
        }
        board[coordinates.getItem1()][coordinates.getItem2()].setChessPiece(null);
        board[coordinates.getItem1()][coordinates.getItem2()].setColor(null);
        board[targetCoordinates.getItem1()][targetCoordinates.getItem2()].setChessPiece(piece);
        board[targetCoordinates.getItem1()][targetCoordinates.getItem2()].setColor(color);
        board[targetCoordinates.getItem1()][targetCoordinates.getItem2()].setEnPassant(false);
    }

    private void removeOldEnPassant(Field[][] board, Color color) {
        for(int file = 0; file<8; file++) {
            for(int rank = 0; rank<8; rank++) {
                if(board[file][rank].isEnPassant() && !board[file][rank].getColor().equals(color)) {
                    board[file][rank].setEnPassant(false);
                    board[file][rank].setColor(null);
                    return;
                }
            }
        }
    }

    boolean isPieceMovingStraight(Tuple2<Integer, Integer> coordinates, Tuple2<Integer, Integer> targetCoordinates) {
        if( coordinates.getItem1().equals(targetCoordinates.getItem1()) ||
            coordinates.getItem2().equals(targetCoordinates.getItem2())) {
            return true;
        }
        return false;
    }

    private boolean isPieceMovingDiagonal(Tuple2<Integer, Integer> coordinates,
                                          Tuple2<Integer, Integer> targetCoordinates) {
        int x = Math.abs(coordinates.getItem1() - targetCoordinates.getItem1());
        int y = Math.abs(coordinates.getItem2() - targetCoordinates.getItem2());
        if(x != y) {
            log.debug("movement is not straight diagonally(abs(x)!=abs(y))");
            return false;
        }
        return true;
    }

    private boolean isPieceMovementKnightLike(Tuple2<Integer, Integer> coordinates,
                                              Tuple2<Integer, Integer> targetCoordinates) {
        int x = Math.abs(coordinates.getItem1() - targetCoordinates.getItem1());
        int y = Math.abs(coordinates.getItem2() - targetCoordinates.getItem2());
        if( (x==2)&&(y==1) || (x==1)&&(y==2) ) {
            return true;
        }
        return false;
    }

    private boolean isPieceMovementKingLike(Tuple2<Integer, Integer> coordinates,
                                              Tuple2<Integer, Integer> targetCoordinates) {
        int x = Math.abs(coordinates.getItem1() - targetCoordinates.getItem1());
        int y = Math.abs(coordinates.getItem2() - targetCoordinates.getItem2());
        log.debug("King move: "+x+", "+y);
        if( x < 2 && y < 2 ) {
            return true;
        }
        return false;
    }

    private boolean isThePathFree(Field[][] board, Tuple2<Integer, Integer> coordinates,
                                  Tuple2<Integer, Integer> targetCoordinates) {
        List<Field> fields = this.getPathFields(board, coordinates, targetCoordinates);
        if(fields == null) {
            return false;
        }
        for(Field field: fields) {
            if(field.getChessPiece() != null) {
                return false;
            }
        }
        return true;
    }


}
