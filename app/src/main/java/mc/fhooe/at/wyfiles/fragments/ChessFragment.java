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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.adapter.ChessAdapter;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;
import mc.fhooe.at.wyfiles.core.WyApp;
import mc.fhooe.at.wyfiles.games.chess.ChessField;
import mc.fhooe.at.wyfiles.games.chess.ChessGame;
import mc.fhooe.at.wyfiles.games.chess.King;
import mc.fhooe.at.wyfiles.games.chess.Pawn;
import mc.fhooe.at.wyfiles.util.WyUtils;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChessFragment extends Fragment implements ChessAdapter.OnItemClickListener, RematchDialogFragment.OnRematchSelectedListener {

    private static final String ARG_IS_HOST = "arg_is_host";

    public static ChessFragment newInstance(boolean isHost) {
        ChessFragment fragment = new ChessFragment();
        Bundle args = new Bundle(1);
        args.putBoolean(ARG_IS_HOST, isHost);
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected WyfilesManager wyfilesManager;

    @Inject
    protected Vibrator vibrator;

    @Bind(R.id.fragment_chess_rv)
    protected RecyclerView recyclerView;

    @Bind(R.id.fragment_chess_txt_info)
    protected TextView textInfo;

    private boolean isHost;
    private boolean isVibrationEnabled;

    private ChessGame game;
    private ChessAdapter adapter;

    private ChessField selectedField;
    private int selectedPosition = -1;

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
                object = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            String action = WyUtils.getActionFromMessage(object);
            switch (action) {

                case WyUtils.ACTION_GAME_QUIT:
                    Toast.makeText(getContext(), R.string.toast_chess_exit, Toast.LENGTH_LONG).show();
                    getActivity().supportFinishAfterTransition();
                    break;

                case WyUtils.ACTION_GAME_REMATCH:

                    initializeGame();
                    break;

                case WyUtils.ACTION_GAME_CHESS_MOVE:

                    try {
                        int from = object.getInt("from");
                        int to = object.getInt("to");

                        // Set selected values first
                        selectedPosition = from;
                        selectedField = adapter.getItemAtPosition(from);

                        handleAction(adapter.getItemAtPosition(to), to, true);
                        textInfo.setText(R.string.chess_your_turn);
                        game.changeTurn();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public ChessFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WyApp) getActivity().getApplication()).getAppComponent().inject(this);
        isHost = getArguments().getBoolean(ARG_IS_HOST, false);
        isVibrationEnabled = true;

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

        View v = inflater.inflate(R.layout.fragment_chess, container, false);
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
        readSubscriber.unsubscribe();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void gameOver() {
        RematchDialogFragment rdf = RematchDialogFragment
                .newInstance(R.string.text_chess_rematch, R.mipmap.ic_game_chess);
        rdf.setOnRematchSelectedListener(ChessFragment.this);
        rdf.show(getFragmentManager(), "rematch-dialog");
    }

    private void initializeGame() {

        game = new ChessGame(isHost);

        List<ChessField> chessBoard = game.createBoard();

        // Setup own views
        adapter = new ChessAdapter(getContext(), chessBoard);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 8));
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        textInfo.setText("");
    }

    private void handleFigureMovement(ChessField f, int pos, boolean updateGameLogic,
                                      boolean movePawnOverrideFlag, boolean isEnemyMove) {

        // Move path must be empty and figure movement must also be possible
        if ((selectedField.moveFigure(selectedPosition, pos)
                && isMovePathEmpty(selectedPosition, pos))
                || movePawnOverrideFlag) {

            adapter.getItemAtPosition(pos).setFigure(selectedField.getFigure());
            adapter.getItemAtPosition(selectedPosition).setFigure(null);
            adapter.notifyDataSetChanged();

            // Notify bluetooth partner
            if (!isEnemyMove){
                wyfilesManager.sendBluetoothMessage(WyUtils.createChessMessage(selectedPosition, pos));
            }

            selectedField.setHighlighted(false);
            selectedField = null;
            selectedPosition = -1;

            if (updateGameLogic && !isEnemyMove) {
                textInfo.setText(R.string.chess_partner_turn);
                if (isVibrationEnabled) {
                    vibrator.vibrate(40);
                }
                game.changeTurn();
            }

        } else {
            textInfo.setText(R.string.chess_cannot_move_figure);
            if (isVibrationEnabled) {
                vibrator.vibrate(new long[]{0, 50, 100, 40}, -1);
            }
        }
    }

    private void handleFigureStrike(ChessField f, int pos, boolean isEnemyMove) {

        if (selectedField.tryStrike(selectedPosition, pos)
                && isMovePathEmpty(selectedPosition, pos)) {

            boolean isKing = (f.getFigure() instanceof King);
            boolean isPawn = (f.getFigure() instanceof Pawn);

            // Remove figure from chess board
            adapter.getItemAtPosition(pos).setFigure(null);
            // Other things can be handled by figureMovement method
            handleFigureMovement(f, pos, false, isPawn, isEnemyMove);

            if (isKing) {
                textInfo.setText(R.string.chess_checkmate);
                if (isVibrationEnabled) {
                    vibrator.vibrate(new long[]{0, 500, 300, 500}, -1);
                }
                adapter.setOnItemClickListener(null);
                // Ask rematch request
                if (isEnemyMove) {
                    gameOver();
                }
            } else {
                textInfo.setText(isEnemyMove ? R.string.chess_enemy_strike : R.string.chess_good_strike);
                if (isVibrationEnabled) {
                    vibrator.vibrate(200);
                }
            }

            // Only change turn when you are in charge
            if (!isEnemyMove) {
                game.changeTurn();
            }

        } else {
            textInfo.setText(R.string.chess_cannot_strike_figure);
            if (isVibrationEnabled) {
                vibrator.vibrate(new long[]{0, 50, 100, 40}, -1);
            }
        }
    }

    private void handleFigureSelection(ChessField f, int pos) {

        if (selectedField != null) {
            selectedField.setHighlighted(false);
        }

        selectedField = f;
        selectedField.setHighlighted(true);

        adapter.notifyItemChanged(pos);
        if (selectedPosition != -1) {
            adapter.notifyItemChanged(selectedPosition);
        }
        selectedPosition = pos;

        if (isVibrationEnabled) {
            vibrator.vibrate(40);
        }
    }

    private boolean isMovePathEmpty(int from, int to) {

        List<Integer> passingField = getPassingFieldsIn2D(from, to);
        for (int i : passingField) {
            if (adapter.getItemAtPosition(i).getFigure() != null) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> getPassingFieldsIn2D(int from, int to) {

        int x = from % 8;
        int y = (int) Math.ceil(from / 8);
        int destX = to % 8;
        int destY = (int) Math.ceil(to / 8);
        List<Integer> list = new ArrayList<>();

        // Straight vertical rows
        if (x == destX) {
            for (int i = Math.min(from, to) + 8; i < Math.max(from, to); i += 8) {
                list.add(i);
            }
        }
        // Straight horizontal rows
        if (y == destY) {
            int own = (from - to) < 0 ? 1 : 0;
            for (int i = Math.min(from, to) + own; i < Math.max(from, to); i++) {
                list.add(i);
            }
        }
        // Right diagonal
        if (Math.abs(from - to) % 7 == 0) {
            for (int i = Math.min(from, to) + 7; i < Math.max(from, to); i += 7) {
                list.add(i);
            }
        }
        // Left diagonal
        if (Math.abs(from - to) % 9 == 0) {
            for (int i = Math.min(from, to) + 9; i < Math.max(from, to); i += 9) {
                list.add(i);
            }
        }

        return list;
    }

    private void handleAction(ChessField f, int pos, boolean isEnemyMove) {

        // Switch between own figures
        if (f.getFigure() != null && !isEnemyMove
                && f.getFigure().getColor() == game.ownColor()) {
            handleFigureSelection(f, pos);
        } else if (selectedField != null && f.getFigure() == null) {
            // Figure already selected and moved to an empty field
            handleFigureMovement(f, pos, true, false, isEnemyMove);
        } else if ((selectedField != null && f.getFigure() != null
                && f.getFigure().getColor() != game.ownColor())
                || (selectedField != null && f.getFigure() != null
                && isEnemyMove && f.getFigure().getColor() == game.ownColor())) {
            // Figure already selected and moved to an field
            // which is occupied with an enemy
            handleFigureStrike(f, pos, isEnemyMove);
        }
    }

    @Override
    public void onItemClick(ChessField f, View v, int pos) {

        if (!game.isTurnAllowed()) {
            Toast.makeText(getContext(), R.string.toast_game_not_your_turn, Toast.LENGTH_SHORT).show();
            return;
        }

        handleAction(f, pos, false);
    }

    @Override
    public void onRematchSelected() {

        initializeGame();
        wyfilesManager.sendBluetoothMessage(WyUtils.createRematchMessage());
    }
}
