package de.hsos.swa.chess.gateway;

import de.hsos.swa.chess.entity.ChessBoard;
import de.hsos.swa.chess.entity.Field;
import de.hsos.swa.chess.entity.Game;
import de.hsos.swa.chess.shared.ChessPiece;
import de.hsos.swa.chess.shared.Color;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class ChessBoardRepository {
    private static final Logger log = Logger.getLogger(ChessBoardRepository.class);

    @Inject
    EntityManager em;

    public Field[][] getBoard(Long gameId) {
        ChessBoard chessBoard = em.find(ChessBoard.class, gameId);
        if(chessBoard == null || chessBoard.getFields() == null) {
            return null;
        }
        Field[][] board = new Field[8][8];
        for(int row=0; row<8; row++) {
            for(int i=0; i<8; i++) {
                board[i][row] = chessBoard.getFields().get(row*8+i);
            }
        }
        return board;
    }

    @Transactional
    public Field[][] setBoard(Long gameId, Field[][] newBoard) {
        ChessBoard board = em.find(ChessBoard.class, gameId);
        if(board == null || newBoard == null) {
            return null;
        }
        for(int row=0; row<8; row++) {
            for(int i=0; i<8; i++) {
                board.getFields().set(row*8+i, newBoard[i][row]);
            }
        }
        em.persist(board);
        return newBoard;
    }

    @Transactional
    public Field[][] initBoard(Long gameId) {
        ChessBoard board = em.find(ChessBoard.class, gameId);
        if(board == null) {
            return null;
        }

        Field[][] newBoard = new Field[8][8];
        for (int i = 0; i < 8; i++) {
            newBoard[2][i] = newBoard[3][i] = newBoard[4][i] = newBoard[5][i] = new Field();
        }
        for (int i = 0; i < 8; i++) {
            newBoard[1][i] = new Field(ChessPiece.PAWN, Color.WHITE);
            newBoard[6][i] = new Field(ChessPiece.PAWN, Color.BLACK);
        }
        newBoard[0][0] = newBoard[0][7] = new Field(ChessPiece.ROOK, Color.WHITE);
        newBoard[7][0] = newBoard[7][7] = new Field(ChessPiece.ROOK, Color.BLACK);
        newBoard[0][1] = newBoard[0][6] = new Field(ChessPiece.KNIGHT, Color.WHITE);
        newBoard[7][1] = newBoard[7][6] = new Field(ChessPiece.KNIGHT, Color.BLACK);
        newBoard[0][2] = newBoard[0][5] = new Field(ChessPiece.BISHOP, Color.WHITE);
        newBoard[7][2] = newBoard[7][5] = new Field(ChessPiece.BISHOP, Color.BLACK);
        newBoard[0][3] = new Field(ChessPiece.QUEEN, Color.WHITE);
        newBoard[7][3] = new Field(ChessPiece.QUEEN, Color.BLACK);
        newBoard[0][4] = new Field(ChessPiece.KING, Color.WHITE);
        newBoard[7][4] = new Field(ChessPiece.KING, Color.BLACK);
        List<Field> fields = new ArrayList<>();
        for(Field[] row: newBoard) {
            fields.addAll(List.of(row));
        }
        board.setFields(fields);
        em.persist(board);
        return newBoard;
    }

}
