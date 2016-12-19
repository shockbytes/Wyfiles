package mc.fhooe.at.wyfiles.games.chess;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class Queen extends ChessFigure {


    public Queen(Color color) {
        super(color);
    }

    @Override
    public int getIcon() {
        return R.mipmap.ic_chess_queen;
    }

    @Override
    public boolean moveTo(int x, int y, int destX, int destY) {
        // Combination of Rook and Bishop
        return (Math.abs(x - destX) == Math.abs(y - destY))
                || (x == destX) || (y == destY);
    }

    @Override
    public boolean strike(int x, int y, int destX, int destY) {
        return moveTo(x, y, destX, destY);
    }
}
