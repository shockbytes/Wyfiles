package mc.fhooe.at.wyfiles.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 17.12.2016.
 */

public class ResourceManager {

    public static List<Game> loadGames(Context context) {

        List<Game> games = new ArrayList<>();
        games.add(new Game(context.getString(R.string.game_ships),
                context.getString(R.string.game_ships_description),
                Game.BATTLESHIP,
                R.mipmap.ic_game_ships));

        return games;
    }

}
