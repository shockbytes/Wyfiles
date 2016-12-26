package mc.fhooe.at.wyfiles.util;

import org.json.JSONException;
import org.json.JSONObject;

import mc.fhooe.at.wyfiles.communication.WyfilesManager;

/**
 * @author Martin Macheiner
 *         Date: 25.12.2016.
 */

public class WyUtils {

    public static final String ACTION_EXIT = "exit";
    public static final String ACTION_GAME_INIT = "game_init";
    public static final String ACTION_GAME_REMATCH = "game_rematch";
    public static final String ACTION_GAME_QUIT = "game_quit";
    public static final String ACTION_GAME_CHESS_MOVE = "game_chess_move";
    public static final String ACTION_GAME_BATTLESHIPS_ATTACK_REQUEST = "game_battleships_attack_request";
    public static final String ACTION_GAME_BATTLESHIPS_ATTACK_RESPONSE = "game_battleships_attack_respone";

    public static String createConnectionMessage(String btDev, String wifiDev, String role,
                                                 WyfilesManager.AuthLevel auth, String key, String iv) {

        JSONObject object = new JSONObject();
        try {
            object.put("action", "connect");
            object.put("role", role);
            object.put("auth", auth.name());
            object.put("btdev", btDev);
            object.put("wifidev", wifiDev);
            if (auth == WyfilesManager.AuthLevel.STANDARD) {
                object.put("initvec", iv);
                object.put("authkey", key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    public static String createChessMessage(int from, int to) {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_GAME_CHESS_MOVE);
            object.put("from", from);
            object.put("to", to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String createGameRequestMessage(int gameId) {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_GAME_INIT);
            object.put("game", gameId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();

    }

    public static String createExitMessage() {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_EXIT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String createBattleshipAttackRequestMessage(int position) {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_GAME_BATTLESHIPS_ATTACK_REQUEST);
            object.put("position", position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String createBattleshipAttackResponseMessage(int position, boolean isShip) {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_GAME_BATTLESHIPS_ATTACK_RESPONSE);
            object.put("position", position);
            object.put("isShip", isShip);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String createQuitMessage() {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_GAME_QUIT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String createRematchMessage() {

        JSONObject object = new JSONObject();
        try {
            object.put("action", ACTION_GAME_REMATCH);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String getActionFromMessage(JSONObject object) {

        try {
            return object.getString("action");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getGameIdFromMessage(JSONObject object) {

        try {
            return object.getInt("game");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }


    }

}
