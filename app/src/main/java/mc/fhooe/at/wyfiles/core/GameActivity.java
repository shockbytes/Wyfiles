package mc.fhooe.at.wyfiles.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.fragments.BattleshipsFragment;
import mc.fhooe.at.wyfiles.fragments.ChessFragment;
import mc.fhooe.at.wyfiles.util.Game;

public class GameActivity extends AppCompatActivity {

    private static final String ARG_GAME_ID = "arg_game_id";
    private static final String ARG_GAME_NAME = "arg_game_name";
    private static final String ARG_GAME_COLOR = "arg_game_color";
    private static final String ARG_GAME_COLOR_DARK = "arg_game_color_dark";
    private static final String ARG_GAME_HOST = "arg_game_host";

    public static Intent newIntent(Context context, Game g, boolean isHost) {
        return new Intent(context, GameActivity.class)
                .putExtra(ARG_GAME_ID, g.getGameId())
                .putExtra(ARG_GAME_NAME, g.getName())
                .putExtra(ARG_GAME_COLOR, g.getPrimaryColor())
                .putExtra(ARG_GAME_COLOR_DARK, g.getPrimaryColorDark())
                .putExtra(ARG_GAME_HOST, isHost);
    }

    @Bind(R.id.game_toolbar)
    protected Toolbar gameToolbar;

    private boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);


        int primaryColor = getIntent().getIntExtra(ARG_GAME_COLOR, 0);
        int primaryColorDark = getIntent().getIntExtra(ARG_GAME_COLOR_DARK, 0);
        int gameId = getIntent().getIntExtra(ARG_GAME_ID, -1);
        String gameName = getIntent().getStringExtra(ARG_GAME_NAME);
        isHost = getIntent().getBooleanExtra(ARG_GAME_HOST, false);

        setupActionBar(primaryColor, primaryColorDark);
        setTitle(gameName);
        chooseGame(gameId);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                supportFinishAfterTransition();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar(int primaryColor, int primaryColorDark) {
        setSupportActionBar(gameToolbar);

        // Tint actionbar with game colors
        gameToolbar.setBackgroundResource(primaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, primaryColorDark));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

    }

    private void chooseGame(int gameId) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        switch(gameId) {

            case Game.BATTLESHIP:

                transaction.replace(R.id.game_main_content, BattleshipsFragment.newInstance(isHost));
                break;

            case Game.CHESS:

                transaction.replace(R.id.game_main_content, ChessFragment.newInstance(isHost));
                break;
        }
        transaction.commit();
    }
}
