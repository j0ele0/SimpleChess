package de.hsos.swa.chess.entity;

import de.hsos.swa.chess.shared.ChessPiece;
import de.hsos.swa.chess.shared.Color;

import javax.persistence.*;


@Embeddable
public class Field {

    @Enumerated
    private ChessPiece chessPiece;
    @Enumerated
    private Color color;
    private boolean enPassant = false;

    public Field() {}

    public Field(ChessPiece chessPiece, Color color) {
        this.chessPiece = chessPiece;
        this.color = color;
    }

    public ChessPiece getChessPiece() {
        return chessPiece;
    }

    public void setChessPiece(ChessPiece chessPiece) {
        this.chessPiece = chessPiece;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public void setEnPassant(boolean enPassant) {
        this.enPassant = enPassant;
    }

    public String toString() {
        if(chessPiece == null) {
            if(isEnPassant()) {
                return "en Passant";
            }
            return "null";
        }
        return color.name()+"_"+chessPiece.name();
    }
}
