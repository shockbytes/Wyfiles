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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;
import mc.fhooe.at.wyfiles.fragments.FilesFragment;
import mc.fhooe.at.wyfiles.fragments.GamesFragment;
import mc.fhooe.at.wyfiles.util.Game;
import mc.fhooe.at.wyfiles.util.ResourceManager;
import mc.fhooe.at.wyfiles.util.WyUtils;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, WyfilesManager.WyfilesCallback {


    public interface OnFileReceivedListener {

        void onFileReceived(String filename);

        void openReceiveFolder();
    }

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

    private Menu menu;

    private int primaryOld;
    private int primaryDarkOld;
    private int initialTabPosition;

    private OnFileReceivedListener onFileReceivedListener;

    private Subscriber<String> bluetoothReadSubscriber = new Subscriber<String>() {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Log.wtf("Wyfiles", e.getMessage());
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
            if (action.equals(WyUtils.ACTION_EXIT)) {
                Snackbar.make(findViewById(android.R.id.content), R.string.connection_terminated,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.close, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                supportFinishAfterTransition();
                            }
                        }).show();

            } else if (action.equals(WyUtils.ACTION_GAME_INIT)) { // Indicates a new game connection request
                int gameId = WyUtils.getGameIdFromMessage(object);
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
            wyfilesManager.establishWifiDirectConnection(this, true, null, this);
        }

        initializeTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
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

    private void initializeTabs() {

        tabLayout.addOnTabSelectedListener(this);
        TabLayout.Tab initialTab = tabLayout.getTabAt(initialTabPosition);
        if (initialTab != null) {
            onTabSelected(initialTab);
            initialTab.select();
        }
    }

    private void getDataFromPayload(String payload) {

        try {

            JSONObject object = new JSONObject(payload);
            String bluetoothClientDevice = object.getString("btdev");
            String wifiHostName = object.getString("wifidev");

            wyfilesManager.connectWithBluetoothDevice(bluetoothClientDevice, this);
            wyfilesManager.establishWifiDirectConnection(this, false, wifiHostName, this);

            String authMode = object.getString("auth");
            if (WyfilesManager.AuthLevel.valueOf(authMode) == WyfilesManager.AuthLevel.STANDARD) {
                String encodedIv = object.getString("initvec");
                String encodedKey = object.getString("authkey");
                wyfilesManager.initializeCipherMode(encodedIv, encodedKey);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(R.id.main_content), R.string.no_secure_line,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    public void adjustForFileExplorer(boolean onCreated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int elevation = onCreated ? 0 : (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8, getResources().getDisplayMetrics());
            appBar.setElevation(elevation);
        }

    }

    public void registerFileListener(OnFileReceivedListener listener) {
        onFileReceivedListener = listener;
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
        menu.getItem(1).setVisible(true);
        vibrator.vibrate(100);
        wyfilesManager.requestBluetoothReadConnection().subscribe(bluetoothReadSubscriber);
    }

    @Override
    public void onWifiDirectError(final String message, Severity severity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWifiDirectFileTransferred(final String filename) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(android.R.id.content), filename + " successfully transferred!", Snackbar.LENGTH_LONG)
                        .setAction("SHOW", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (onFileReceivedListener != null) {
                                    onFileReceivedListener.openReceiveFolder();
                                }
                            }
                        }).show();
            }
        });
        if (onFileReceivedListener != null) {
            onFileReceivedListener.onFileReceived(filename);
        }
    }

    @Override
    public void onWifiDirectError(int messageId, Severity severity) {
        onWifiDirectError(getString(messageId), severity);
    }

    @Override
    public void onWifiDirectConnected(String remoteDevice) {
        menu.getItem(0).setVisible(true);
    }

}
