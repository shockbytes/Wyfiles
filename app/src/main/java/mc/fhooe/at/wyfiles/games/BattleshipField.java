package mc.fhooe.at.wyfiles.games;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 19.12.2016.
 */

public class BattleshipField {

    public enum FieldState { WATER, SHIP, FAILED_SHOT, SHOT }

    private int iconId;
    private boolean isClicked;
    private FieldState state;

    public BattleshipField(FieldState state) {
        this.state = state;
        isClicked = false;
        iconId = (state == FieldState.WATER)
                ? R.mipmap.ic_battleship_water
                : R.mipmap.ic_battleship_ship;
    }

    public void changeFieldState(FieldState state) {

        this.state = state;
        setIconByState();
    }

    public FieldState getState() {
        return state;
    }

    public boolean isAlreadySelected() {
        return (state == FieldState.FAILED_SHOT) || (state == FieldState.SHOT);
    }

    public int getIcon() {
        return iconId;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean isClicked) {
        this.isClicked = isClicked;
    }

    private void setIconByState() {

        if (state == FieldState.WATER) {
            iconId = R.mipmap.ic_battleship_water;
        } else if (state == FieldState.SHIP) {
            iconId = R.mipmap.ic_battleship_ship;
        } else if (state == FieldState.FAILED_SHOT) {
            iconId = R.mipmap.ic_battleship_failed_shot;
        } else if (state == FieldState.SHOT) {
            iconId = R.mipmap.ic_battleship_shot;
        }
    }

}
