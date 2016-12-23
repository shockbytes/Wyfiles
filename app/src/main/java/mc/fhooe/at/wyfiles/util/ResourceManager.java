package mc.fhooe.at.wyfiles.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 17.12.2016.
 */

public class ResourceManager {

    private static final Map<String, Integer> extensionMap = new HashMap<>();
    static {
        initializeMap();
    }

    private static void initializeMap(){
        extensionMap.put("directory", R.mipmap.ic_file_folder);
        extensionMap.put("exe", R.mipmap.ic_file_exe);
        extensionMap.put("apk", R.mipmap.ic_file_apk);
        extensionMap.put("app", R.mipmap.ic_file_app);
        extensionMap.put("dmg", R.mipmap.ic_file_dmg);
        extensionMap.put("xml", R.mipmap.ic_file_xml);
        extensionMap.put("txt", R.mipmap.ic_file_txt);
        extensionMap.put("pdf", R.mipmap.ic_file_pdf);
        extensionMap.put("doc", R.mipmap.ic_file_word);
        extensionMap.put("docx", R.mipmap.ic_file_word);
        extensionMap.put("xls", R.mipmap.ic_file_excel);
        extensionMap.put("xlsx", R.mipmap.ic_file_excel);
        extensionMap.put("ppt", R.mipmap.ic_file_powerpoint);
        extensionMap.put("pptx", R.mipmap.ic_file_powerpoint);
        extensionMap.put("tar", R.mipmap.ic_file_archive);
        extensionMap.put("bz2", R.mipmap.ic_file_archive);
        extensionMap.put("gz", R.mipmap.ic_file_archive);
        extensionMap.put("7z", R.mipmap.ic_file_archive);
        extensionMap.put("zip", R.mipmap.ic_file_archive);
        extensionMap.put("rar", R.mipmap.ic_file_archive);
        extensionMap.put("png", R.mipmap.ic_file_images);
        extensionMap.put("gif", R.mipmap.ic_file_images);
        extensionMap.put("jpg", R.mipmap.ic_file_images);
        extensionMap.put("jpeg", R.mipmap.ic_file_images);
        extensionMap.put("webp", R.mipmap.ic_file_images);
        extensionMap.put("mp3", R.mipmap.ic_file_audio);
        extensionMap.put("mpeg", R.mipmap.ic_file_audio);
        extensionMap.put("wav", R.mipmap.ic_file_audio);
        extensionMap.put("wma", R.mipmap.ic_file_audio);
        extensionMap.put("ogg", R.mipmap.ic_file_audio);
        extensionMap.put("flac", R.mipmap.ic_file_audio);
        extensionMap.put("c", R.mipmap.ic_file_code);
        extensionMap.put("cpp", R.mipmap.ic_file_code);
        extensionMap.put("cs", R.mipmap.ic_file_code);
        extensionMap.put("css", R.mipmap.ic_file_code);
        extensionMap.put("h", R.mipmap.ic_file_code);
        extensionMap.put("m", R.mipmap.ic_file_code);
        extensionMap.put("java", R.mipmap.ic_file_code);
        extensionMap.put("js", R.mipmap.ic_file_code);
        extensionMap.put("groovy", R.mipmap.ic_file_code);
        extensionMap.put("html", R.mipmap.ic_file_code);
        extensionMap.put("php", R.mipmap.ic_file_code);
        extensionMap.put("swift", R.mipmap.ic_file_code);
        extensionMap.put("playground", R.mipmap.ic_file_code);
        extensionMap.put("py", R.mipmap.ic_file_code);
        extensionMap.put("sh", R.mipmap.ic_file_code);
        extensionMap.put("rb", R.mipmap.ic_file_code);
        extensionMap.put("asm", R.mipmap.ic_file_code);
        extensionMap.put("jar", R.mipmap.ic_file_jar);
        extensionMap.put("psd", R.mipmap.ic_file_psd);
    }

    public static int getImageIconForFileExtension(String extension) {

        Integer image = extensionMap.get(extension);
        //If image not null return image, otherwise no mapping was found --> return default icon
        return (image != null) ? image : R.mipmap.ic_file_default;
    }

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
