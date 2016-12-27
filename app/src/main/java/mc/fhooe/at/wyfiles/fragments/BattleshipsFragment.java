package mc.fhooe.at.wyfiles.fragments;


import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
import mc.fhooe.at.wyfiles.util.WyUtils;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BattleshipsFragment extends Fragment implements BattleshipsAdapter.OnItemClickListener, RematchDialogFragment.OnRematchSelectedListener {

    private static final String ARG_IS_HOST = "arg_is_host";

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
        }

        @Override
        public void onNext(String s) {

            JSONObject object;
            try {
                object = new JSONObject(wyfilesManager.decryptIfNecessary(s));
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            String action = WyUtils.getActionFromMessage(object);
            switch (action) {

                // THIS WILL ONLY AFFECT ENEMY BOARD
                case WyUtils.ACTION_GAME_BATTLESHIPS_ATTACK_RESPONSE:

                    try {

                        int position = object.getInt("position");
                        boolean isShip = object.getBoolean("isShip");
                        BattleshipField enemyField = enemyAdapter.getItemAtPosition(position);

                        if (isShip) {
                            enemyField.changeFieldState(BattleshipField.FieldState.SHOT);
                            int enemyShips = game.decreaseEnemyCount();
                            String strEnemy = getString(R.string.game_ships_enemy_ships, enemyShips);
                            txtEnemyCount.setText(strEnemy);
                        } else {
                            enemyField.changeFieldState(BattleshipField.FieldState.FAILED_SHOT);
                        }
                        enemyAdapter.notifyItemChanged(position);
                        game.changeTurn();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case WyUtils.ACTION_GAME_BATTLESHIPS_ATTACK_REQUEST:

                    try {
                        int position = object.getInt("position");
                        BattleshipField bsField = ownAdapter.getItemAtPosition(position);
                        bsField.setClicked(true);
                        boolean isShip = ownAdapter.isFieldAShip(position);
                        if (isShip) {
                            int ownShips = game.decreaseOwnCount();
                            String strOwn = getString(R.string.game_ships_own_ships, ownShips);
                            txtOwnCount.setText(strOwn);

                            if (ownShips == 0) {
                                gameOver();
                            }
                        }
                        ownAdapter.notifyItemChanged(position);
                        game.changeTurn();
                        wyfilesManager.sendBluetoothMessage(
                                WyUtils.createBattleshipAttackResponseMessage(position, isShip));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case WyUtils.ACTION_GAME_REMATCH:

                    initializeGame();
                    break;

                case WyUtils.ACTION_GAME_QUIT:
                    Toast.makeText(getContext(), R.string.text_game_exit, Toast.LENGTH_LONG).show();
                    getActivity().supportFinishAfterTransition();
                    break;
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

        wyfilesManager.requestBluetoothReadConnection().subscribe(readSubscriber);
    }

    @Override
    public void onDestroy() {
        wyfilesManager.sendBluetoothMessage(WyUtils.createQuitMessage());
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

        txtOwnCount.setText(getString(R.string.game_ships_own_ships, ships));
        txtEnemyCount.setText(getString(R.string.game_ships_enemy_ships, ships));
    }

    private void gameOver() {

        RematchDialogFragment rdf = RematchDialogFragment
                .newInstance(R.string.text_battleship_rematch, R.mipmap.ic_game_ships);
        rdf.setOnRematchSelectedListener(BattleshipsFragment.this);
        rdf.show(getFragmentManager(), "rematch-dialog");
    }

    @Override
    public void onItemClick(BattleshipField f, View v, int pos) {

        if (f.isAlreadySelected()) {
            Snackbar.make(getView(), R.string.text_battleship_already_selected, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!game.isTurnAllowed()) {
            Snackbar.make(getView(), R.string.text_game_not_your_turn, Snackbar.LENGTH_SHORT).show();
            return;
        }

        vibrator.vibrate(100);
        wyfilesManager.sendBluetoothMessage(WyUtils.createBattleshipAttackRequestMessage(pos));
    }

    @Override
    public void onRematchSelected() {

        initializeGame();
        wyfilesManager.sendBluetoothMessage(WyUtils.createRematchMessage());
    }
}
