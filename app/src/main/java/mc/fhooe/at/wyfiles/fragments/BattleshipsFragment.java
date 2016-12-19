package mc.fhooe.at.wyfiles.fragments;


import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.adapter.BattleshipsAdapter;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;
import mc.fhooe.at.wyfiles.core.WyApp;
import mc.fhooe.at.wyfiles.games.battleships.BattleshipField;
import mc.fhooe.at.wyfiles.games.battleships.BattleshipsGame;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BattleshipsFragment extends Fragment implements BattleshipsAdapter.OnItemClickListener {

    private static final String ARG_IS_HOST = "arg_is_host";

    private static final String ATTACK_REQ_STRING = "bs,at,req,";
    private static final String ATTACK_RESP_STRING = "bs,at,resp,";
    private static final String REMATCH_STRING = "game_rematch";
    private static final String EXIT_MATCH_STRING = "game_exit_match";

    public static BattleshipsFragment newInstance(boolean isHost) {
        BattleshipsFragment fragment = new BattleshipsFragment();
        Bundle args = new Bundle(1);
        args.putBoolean(ARG_IS_HOST, isHost);
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected WyfilesManager wyfilesManager;

    @Inject
    protected Vibrator vibrator;

    @Bind(R.id.fragment_battleships_txt_owncount)
    protected TextView txtOwnCount;

    @Bind(R.id.fragment_battleships_txt_enemycount)
    protected TextView txtEnemyCount;

    @Bind(R.id.fragment_battleships_rv_enemy)
    protected RecyclerView rvEnemy;

    @Bind(R.id.fragment_battleships_rv_own)
    protected RecyclerView rvOwn;

    private boolean isHost;

    private BattleshipsGame game;

    private BattleshipsAdapter ownAdapter;
    private BattleshipsAdapter enemyAdapter;

    private Subscriber<String> readSubscriber = new Subscriber<String>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getContext(), "BattleshipFrag; " + e.getMessage() + "\n" +e.getCause().getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(String s) {

            if (s.startsWith(ATTACK_RESP_STRING)) {
                // THIS WILL ONLY AFFECT ENEMY BOARD
                // Example: "bs,at,resp,4;true"

                String msg = s.substring(s.lastIndexOf(",")+1 , s.length());
                String[] sp = msg.split(";");
                int field = Integer.parseInt(sp[0]);
                boolean isShip = Boolean.parseBoolean(sp[1]);

                BattleshipField bsField = enemyAdapter.getItemAtPosition(field);

                if (isShip) {
                    bsField.changeFieldState(BattleshipField.FieldState.SHOT);
                    int enemyShips = game.decreaseEnemyCount();
                    String strEnemy = getString(R.string.game_ships_enemy_ships, enemyShips);
                    txtEnemyCount.setText(strEnemy);
                } else {
                    bsField.changeFieldState(BattleshipField.FieldState.FAILED_SHOT);
                }
                enemyAdapter.notifyItemChanged(field);
                game.changeTurn();

            } else if (s.startsWith(ATTACK_REQ_STRING)) {
                // THIS WILL ONLY AFFECT OWN BOARD
                // Example: "bs,at,req,4"
                int field = Integer.parseInt(s.substring(s.lastIndexOf(",")+1 , s.length()));

                BattleshipField bsField = ownAdapter.getItemAtPosition(field);
                bsField.setClicked(true);
                boolean isShip = ownAdapter.isFieldAShip(field);
                if (isShip) {
                    int ownShips = game.decreaseOwnCount();
                    String strOwn = getString(R.string.game_ships_own_ships, ownShips);
                    txtOwnCount.setText(strOwn);

                    if (ownShips == 0) {
                        wyfilesManager.sendBluetoothMessage(REMATCH_STRING);
                    }
                }
                ownAdapter.notifyItemChanged(field);
                game.changeTurn();

                // Send back response
                String answer = ATTACK_RESP_STRING + field + ";" + isShip;
                wyfilesManager.sendBluetoothMessage(answer);

            } else if (s.equals(REMATCH_STRING)) {
                Toast.makeText(getContext(), R.string.toast_battleship_rematch, Toast.LENGTH_LONG).show();
                // TODO Show dialog for rematch
            } else if (s.equals(EXIT_MATCH_STRING)) {
                Toast.makeText(getContext(), R.string.toast_battleship_exit, Toast.LENGTH_LONG).show();
                getActivity().supportFinishAfterTransition();
            }
        }
    };

    public BattleshipsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WyApp) getActivity().getApplication()).getAppComponent().inject(this);
        isHost = getArguments().getBoolean(ARG_IS_HOST, false);
    }

    @Override
    public void onDestroy() {
        wyfilesManager.sendBluetoothMessage(EXIT_MATCH_STRING);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_battleships, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeGame();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        readSubscriber.unsubscribe();
        super.onDestroyView();
    }

    private void initializeGame() {

        int cols = 5;
        int ships = 10;

        game = new BattleshipsGame(cols, ships, isHost);

        List<BattleshipField> ownShips = game.createOwnBoard();
        List<BattleshipField> enemyShips = game.createEnemyBoard();

        // Setup own views
        ownAdapter = new BattleshipsAdapter(getContext(), ownShips);
        rvOwn.setLayoutManager(new GridLayoutManager(getContext(), cols));
        rvOwn.setAdapter(ownAdapter);

        // Setup enemy views
        enemyAdapter = new BattleshipsAdapter(getContext(), enemyShips);
        enemyAdapter.setOnItemClickListener(this);
        rvEnemy.setLayoutManager(new GridLayoutManager(getContext(), cols));
        rvEnemy.setAdapter(enemyAdapter);

        wyfilesManager.requestBluetoothReadConnection().subscribe(readSubscriber);

        txtOwnCount.setText(getString(R.string.game_ships_own_ships, ships));
        txtEnemyCount.setText(getString(R.string.game_ships_enemy_ships, ships));

    }

    @Override
    public void onItemClick(BattleshipField f, View v, int pos) {

        if (f.isAlreadySelected()) {
            Toast.makeText(getContext(), R.string.toast_battleship_already_selected, Toast.LENGTH_LONG).show();
            return;
        }
        if (!game.isTurnAllowed()) {
            Toast.makeText(getContext(), R.string.toast_game_not_your_turn, Toast.LENGTH_LONG).show();
            return;
        }

        vibrator.vibrate(100);
        wyfilesManager.sendBluetoothMessage(ATTACK_REQ_STRING + pos);
    }
}
