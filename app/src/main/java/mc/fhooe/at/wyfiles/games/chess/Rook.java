package mc.fhooe.at.wyfiles.games.chess;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class Rook extends ChessFigure {

    public Rook(Color color) {
        super(color);
    }

    @Override
    public int getIcon() {
        return R.mipmap.ic_chess_rook;
    }

    @Override
    public boolean moveTo(int x, int y, int destX, int destY) {
        return (x == destX) || (y == destY);
    }

    @Override
    public boolean strike(int x, int y, int destX, int destY) {
        return moveTo(x, y, destX, destY);
    }
}
