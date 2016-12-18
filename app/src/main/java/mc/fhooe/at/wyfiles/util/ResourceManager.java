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
        games.add(getGameById(context, Game.BATTLESHIP));
        games.add(getGameById(context, Game.CHESS));
        return games;
    }

    public static Game getGameById(Context context, int id) {

        Game g = null;
        if (id == Game.BATTLESHIP) {
            g = new Game(context.getString(R.string.game_ships),
                    context.getString(R.string.game_ships_description),
                    Game.BATTLESHIP,
                    R.mipmap.ic_game_ships,
                    R.color.colorPrimaryGameBattleships,
                    R.color.colorPrimaryDarkGameBattleships);
        } else if (id == Game.CHESS) {
            g = new Game(context.getString(R.string.game_chess),
                    context.getString(R.string.game_chess_description),
                    Game.CHESS,
                    R.mipmap.ic_game_chess,
                    R.color.colorPrimaryGameChess,
                    R.color.colorPrimaryDarkGameChess);
        }

        return g;
    }

}
