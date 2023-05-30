package de.hsos.swa.chess.shared;

import de.hsos.swa.chess.boundary.rest.GameResource;
import de.hsos.swa.chess.entity.ChessBoard;
import de.hsos.swa.chess.entity.Field;
import org.jboss.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

public class ChessBoardAdapter implements JsonbAdapter<ChessBoard, JsonArray> {
    private static final Logger log = Logger.getLogger(ChessBoardAdapter.class);

    @Override
    public JsonArray adaptToJson(ChessBoard chessBoard) throws Exception {
        if(chessBoard.getFields() == null) {
            return Json.createArrayBuilder().build();
        }
        JsonArrayBuilder jsonBoard = Json.createArrayBuilder();
        JsonArrayBuilder jsonRow;
        for(int row=0; row<8; row++) {
            jsonRow = Json.createArrayBuilder();
            for(int i=0; i<8; i++) {
                jsonRow.add(chessBoard.getFields().get(row*8+i).toString());
            }
            jsonBoard.add(jsonRow);
        }
        return jsonBoard.build();
    }

    @Override
    public ChessBoard adaptFromJson(JsonArray jsonValues) throws Exception {
        return null;
    }


}
