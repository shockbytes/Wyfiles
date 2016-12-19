package mc.fhooe.at.wyfiles.games.chess;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class Bishop extends ChessFigure {

    public Bishop(Color color) {
        super(color);
    }

    @Override
    public int getIcon() {
        return R.mipmap.ic_chess_bishop;
    }

    @Override
    public boolean moveTo(int x, int y, int destX, int destY) {
        return (Math.abs(x - destX) == Math.abs(y - destY));
    }

    @Override
    public boolean strike(int x, int y, int destX, int destY) {
        return moveTo(x, y, destX, destY);
    }
}
