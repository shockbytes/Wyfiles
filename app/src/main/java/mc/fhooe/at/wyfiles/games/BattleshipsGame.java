package mc.fhooe.at.wyfiles.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

public class BattleshipsGame {

    private int boardSize;
    private int ships;

    private int ownShipCount;
    private int enemyShipCount;

    private boolean isTurnAllowed;

    public BattleshipsGame(int boardSize, int ships, boolean isHost) {
        this.boardSize = boardSize;
        this.ships = ships;

        ownShipCount = ships;
        enemyShipCount = ships;

        isTurnAllowed = isHost;
    }

    public void changeTurn() {
        isTurnAllowed = !isTurnAllowed;
    }

    public boolean isTurnAllowed() {
        return isTurnAllowed;
    }

    public int decreaseEnemyCount() {
        return --enemyShipCount;
    }

    public int decreaseOwnCount() {
        return --ownShipCount;
    }

    public List<BattleshipField> createEnemyBoard() {

        int fullBoardSize = boardSize * boardSize;
        List<BattleshipField> fields = new ArrayList<>(fullBoardSize);
        for (int j = 0; j < fullBoardSize; j++) {
            fields.add(new BattleshipField(BattleshipField.FieldState.WATER));
        }
        return fields;
    }

    public List<BattleshipField> createOwnBoard() {

        Random random = new Random();
        int fullBoardSize = boardSize * boardSize;
        List<Boolean> board = new ArrayList<>(fullBoardSize);

        // Default initialize all with false
        for (int i = 0; i < fullBoardSize; i++) {
            board.add(false);
        }

        boolean isAlreadySet;
        int pos;
        for (int k = 0; k < ships; k++) {

            do {

                isAlreadySet = false;
                pos = random.nextInt(fullBoardSize);
                if (board.get(pos)) {
                    isAlreadySet = true;
                } else {
                    board.set(pos, true);
                }
            } while (isAlreadySet);
        }

        // Convert to BattleShipField List
        List<BattleshipField> fields = new ArrayList<>();
        for (int j = 0; j < board.size(); j++) {

            BattleshipField tmp = (board.get(j))
                    ? new BattleshipField(BattleshipField.FieldState.SHIP)
                    : new BattleshipField(BattleshipField.FieldState.WATER);
            fields.add(tmp);
        }

        return fields;
    }

}
