package mc.fhooe.at.wyfiles.games.chess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class ChessGame {

    private ChessFigure.Color ownColor;

    private boolean isTurnAllowed;

    public ChessGame(boolean isHost) {

        isTurnAllowed = isHost;
        ownColor = isHost ? ChessFigure.Color.WHITE : ChessFigure.Color.BLACK;
    }

    public ChessFigure.Color ownColor() {
        return ownColor;
    }

    public void changeTurn() {
        isTurnAllowed = !isTurnAllowed;
    }

    public boolean isTurnAllowed() {
        return isTurnAllowed;
    }

    public List<ChessField> createBoard() {

        List<ChessField> fields = new ArrayList<>();

        // Black figures
        fields.add(new ChessField(new Rook(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Knight(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Bishop(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Queen(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new King(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Bishop(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Knight(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Rook(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.BLACK)));

        // Empty fields
        for (int i = 0; i < 32; i++) {
            fields.add(new ChessField(null));
        }

        // White figures
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Pawn(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Rook(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Knight(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Bishop(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new King(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Queen(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Bishop(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Knight(ChessFigure.Color.WHITE)));
        fields.add(new ChessField(new Rook(ChessFigure.Color.WHITE)));


        return fields;
    }

}
