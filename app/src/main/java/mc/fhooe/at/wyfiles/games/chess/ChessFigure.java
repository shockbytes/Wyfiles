package mc.fhooe.at.wyfiles.games.chess;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public abstract class ChessFigure {

    public enum Color { BLACK, WHITE }

    protected Color color;

    public ChessFigure(Color color){
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public abstract int getIcon();

    // Call this method only if field with coordinates destX and destY
    // contain a null reference of ChessFigure
    public abstract boolean moveTo(int x, int y, int destX, int destY);

    // Call this method only if field with coordinates destX and destY
    // contain a valid reference to a ChessFigure
    public abstract boolean strike(int x, int y, int destX, int destY);

}
