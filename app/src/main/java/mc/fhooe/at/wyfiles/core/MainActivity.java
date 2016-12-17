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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.fragments.FilesFragment;
import mc.fhooe.at.wyfiles.fragments.GamesFragment;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private static final String ARG_PRIMARY = "arg_primary";
    private static final String ARG_PRIMARY_DARK = "arg_primary_dark";
    private static final String ARG_TAB_POSITION = "arg_tab_position";

    @Bind(R.id.main_tablayout)
    protected TabLayout tabLayout;

    @Bind(R.id.main_toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.main_appbar)
    protected AppBarLayout appBar;

    private int primaryOld;
    private int primaryDarkOld;
    private int initialTabPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        initialize();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
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
}
