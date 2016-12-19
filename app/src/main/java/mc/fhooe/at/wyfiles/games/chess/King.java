package mc.fhooe.at.wyfiles.games.chess;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class King extends ChessFigure {

    public King(Color color) {
        super(color);
    }

    @Override
    public int getIcon() {
        return R.mipmap.ic_chess_king;
    }

    @Override
    public boolean moveTo(int x, int y, int destX, int destY) {
        return (Math.abs(x - destX) <= 1 && Math.abs(y - destY) <= 1);
    }

    @Override
    public boolean strike(int x, int y, int destX, int destY) {
        return moveTo(x, y, destX, destY);
    }
}
