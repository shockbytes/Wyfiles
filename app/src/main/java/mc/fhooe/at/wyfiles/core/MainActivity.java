package mc.fhooe.at.wyfiles.core;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;
import mc.fhooe.at.wyfiles.fragments.FilesFragment;
import mc.fhooe.at.wyfiles.fragments.GamesFragment;
import mc.fhooe.at.wyfiles.util.Game;
import mc.fhooe.at.wyfiles.util.ResourceManager;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, WyfilesManager.WyfilesCallback {

    public static Intent newIntent(Context context, String payload, boolean isServer) {
        return new Intent(context, MainActivity.class)
                .putExtra(ARG_PAYLOAD, payload)
                .putExtra(ARG_IS_SERVER, isServer);
    }

    private static final String ARG_PAYLOAD = "arg_payload";
    private static final String ARG_PRIMARY = "arg_primary";
    private static final String ARG_IS_SERVER = "arg_is_server";
    private static final String ARG_PRIMARY_DARK = "arg_primary_dark";
    private static final String ARG_TAB_POSITION = "arg_tab_position";

    @Bind(R.id.main_tablayout)
    protected TabLayout tabLayout;

    @Bind(R.id.main_toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.main_appbar)
    protected AppBarLayout appBar;

    @Inject
    protected WyfilesManager wyfilesManager;

    @Inject
    protected Vibrator vibrator;

    private int primaryOld;
    private int primaryDarkOld;
    private int initialTabPosition;

    private Subscriber<String> bluetoothReadSubscriber = new Subscriber<String>() {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Log.wtf("Wyfiles", "ReadSubscriber: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "MainSubscriber: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNext(String s) {

            if (s.equals("exit")) {
                Toast.makeText(getApplicationContext(), "Connection terminated from the other side", Toast.LENGTH_LONG).show();
                //supportFinishAfterTransition();
            } else if (s.startsWith("game")) {
                int gameId = Integer.parseInt(s.split(":")[1]);
                Game g = ResourceManager.getGameById(MainActivity.this, gameId);
                if (g != null) {
                    startActivity(GameActivity.newIntent(MainActivity.this, g, false),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WyApp) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        } else{
            primaryOld = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
            primaryDarkOld = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
            initialTabPosition = 0;
        }

        String payload = getIntent().getStringExtra(ARG_PAYLOAD);
        if (payload != null) {
            getDataFromPayload(payload);
        }

        boolean isServer = getIntent().getBooleanExtra(ARG_IS_SERVER, false);
        if (isServer) {
            wyfilesManager.startBluetoothServer(this);
        }

        initialize();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        bluetoothReadSubscriber.unsubscribe();
        wyfilesManager.destroy();
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreFromInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_PRIMARY, primaryOld);
        outState.putInt(ARG_PRIMARY_DARK, primaryDarkOld);
        outState.putInt(ARG_TAB_POSITION, tabLayout.getSelectedTabPosition());
        super.onSaveInstanceState(outState);
    }

    private void restoreFromInstanceState(Bundle savedInstanceState) {
        primaryOld = savedInstanceState.getInt(ARG_PRIMARY);
        primaryDarkOld = savedInstanceState.getInt(ARG_PRIMARY_DARK);
        initialTabPosition = savedInstanceState.getInt(ARG_TAB_POSITION);
    }

    private void animateHeader(int tab) {

        int primary = 0;
        int primaryDark= 0;
        switch (tab) {

            case 0:

                primary = ContextCompat.getColor(this, R.color.colorPrimary);
                primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
                break;

            case 1:

                primary = ContextCompat.getColor(this, R.color.colorPrimaryGames);
                primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDarkGames);
                break;
        }

        ObjectAnimator animatorAppBar = ObjectAnimator.ofObject(appBar, "backgroundColor",
                new ArgbEvaluator(), primaryOld, primary)
                .setDuration(300);
        ObjectAnimator animatorToolbar = ObjectAnimator.ofObject(toolbar, "backgroundColor",
                new ArgbEvaluator(), primaryOld, primary)
                .setDuration(300);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                primaryDarkOld, primaryDark)
                .setDuration(300);
        // Supress lint, because we are only setting listener, when api is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @SuppressLint("NewApi")
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue());
                }
            });
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorAppBar, animatorToolbar, colorAnimation);
        set.start();

        primaryOld = primary;
        primaryDarkOld = primaryDark;
    }

    private void initialize() {

        tabLayout.addOnTabSelectedListener(this);
        TabLayout.Tab initialTab = tabLayout.getTabAt(initialTabPosition);
        if (initialTab != null) {
            onTabSelected(initialTab);
            initialTab.select();
        }
    }

    private void getDataFromPayload(String payload) {

        String[] loads = payload.split(";");
        String bluetoothClientDevice = loads[1].split(":")[1];

        wyfilesManager.connectWithBluetoothDevice(bluetoothClientDevice, this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        switch (tab.getPosition()) {

            case 0:

                transaction.replace(R.id.main_content, FilesFragment.newInstance());
                break;

            case 1:

                transaction.replace(R.id.main_content, GamesFragment.newInstance());
                break;
        }

        transaction.commit();
        appBar.setExpanded(true, true);
        animateHeader(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onBluetoothError(Throwable t) {

        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
        Log.wtf("Wyfiles", t.getMessage());
    }

    @Override
    public void onBluetoothConnected(String remoteDeviceName) {

        setTitle(getString(R.string.title_main, remoteDeviceName));
        vibrator.vibrate(200);
        // Add read subscriber
        wyfilesManager.requestBluetoothReadConnection().subscribe(bluetoothReadSubscriber);
        // Write hello message
        wyfilesManager.sendBluetoothHelloMessage();
    }

}
