package mc.fhooe.at.wyfiles.games.chess;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class ChessField {

    private ChessFigure figure;

    private boolean isHighlighted;


    public ChessField(ChessFigure figure) {
        this.figure = figure;
        isHighlighted = false;
    }

    public ChessFigure getFigure() {
        return figure;
    }

    public void setFigure(ChessFigure figure) {
        this.figure = figure;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public boolean moveFigure(int position, int destination) {

        if (figure == null) {
            return false;
        }

        int x = (int) Math.ceil(position/8);
        int y = position%8;
        int destX = (int) Math.ceil(destination/8);
        int destY = destination%8;

        return figure.moveTo(x, y, destX, destY);
    }

    public boolean tryStrike(int position, int destination) {

        if (figure == null) {
            return false;
        }

        int x = (int) Math.ceil(position/8);
        int y = position%8;
        int destX = (int) Math.ceil(destination/8);
        int destY = destination%8;

        return figure.strike(x, y, destX, destY);
    }


    public boolean strike(int pos) {
        return false;
    }


}
