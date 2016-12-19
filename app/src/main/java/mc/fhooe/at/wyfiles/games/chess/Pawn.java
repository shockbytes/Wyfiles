package mc.fhooe.at.wyfiles.games.chess;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class Pawn extends ChessFigure {

    private boolean isFirstStep;

    public Pawn(Color color) {
        super(color);
        isFirstStep = true;
    }

    @Override
    public int getIcon() {
        return R.mipmap.ic_chess_pawn;
    }

    @Override
    public boolean moveTo(int x, int y, int destX, int destY) {

        if (isFirstStep) {
            isFirstStep = false;

            // Upwards
            if (color == Color.WHITE) {
                return (y == destY) && ((x - destX) <= 2);
            } else {
                // Downwards
                return (y == destY) && ((destX - x) <= 2);
            }
        } else {

            // Upwards
            if (color == Color.WHITE) {
                return (y == destY) && ((x - destX) == 1);
            } else {
                // Downwards
                return (y == destY) && ((destX - x) == 1);
            }
        }
    }

    @Override
    public boolean strike(int x, int y, int destX, int destY) {

        // White can only strike upwards
        if (color == Color.WHITE) {
            return (Math.abs(y - destY) == 1) && ((x - destX) == 1);
        } else {
            // Black can only strike downwards
            return (Math.abs(y - destY) == 1) && ((destX - x) == 1);
        }
    }
}
