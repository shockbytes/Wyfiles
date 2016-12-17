package mc.fhooe.at.wyfiles.core;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.fragments.ConnectionFragment;

public class ConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new ConnectionFragment())
                .commit();

        // TODO Ask for permissions

    }
}
