package mc.fhooe.at.wyfiles.games.chess;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class Knight extends ChessFigure {

    public Knight(Color color) {
        super(color);
    }

    @Override
    public int getIcon() {
        return R.mipmap.ic_chess_knight;
    }

    @Override
    public boolean moveTo(int x, int y, int destX, int destY) {
        return (Math.abs(x - destX) == 1 && Math.abs(y - destY) == 2)
                || (Math.abs(x - destX) == 2 && Math.abs(y - destY) == 1);
    }

    @Override
    public boolean strike(int x, int y, int destX, int destY) {
        return moveTo(x, y, destX, destY);
    }
}
