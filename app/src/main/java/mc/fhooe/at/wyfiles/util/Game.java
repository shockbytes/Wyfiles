package mc.fhooe.at.wyfiles.util;

import android.support.annotation.DrawableRes;

/**
 * @author Martin Macheiner
 *         Date: 17.12.2016.
 */

public class Game {

    public static final int BATTLESHIP = 1;
    public static final int CHESS = 2;

    private int gameId;
    private int iconId;
    private String name;
    private String description;

    public Game(String name, String description,
                int gameId, @DrawableRes int iconId) {
        this.iconId = iconId;
        this.name = name;
        this.gameId = gameId;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getIconId() {
        return iconId;
    }

    public int getGameId() {
        return gameId;
    }

}
