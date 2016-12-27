package mc.fhooe.at.wyfiles.core;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.peak.salut.Salut;

import javax.inject.Inject;

import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;
import mc.fhooe.at.wyfiles.fragments.ConnectionFragment;
import mc.fhooe.at.wyfiles.util.WyCipher;
import mc.fhooe.at.wyfiles.util.WyUtils;

public class ConnectionActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private static final int PERM_REQ_CODE = 0x3431;
    private static final int REQ_CODE_BT = 0x1235;

    @Inject
    protected Vibrator vibrator;

    @Inject
    protected NfcAdapter nfcAdapter;

    @Inject
    protected BluetoothAdapter bluetoothAdapter;

    @Inject
    protected WyCipher cipher;

    private String wifiP2pDeviceName;

    private PendingIntent nfcPendingIntent;

    private final String MIME_TYPE = "application/vnd.at.fhooe.mc.wyfiles";

    private IntentFilter filter = new IntentFilter(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            wifiP2pDeviceName = device.deviceName;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WyApp) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_connection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new ConnectionFragment())
                .commit();

        registerReceiver(receiver, filter);

        if (!Salut.isWiFiEnabled(this)){
            Salut.enableWiFi(this);
        }

        // Bluetooth permission denied, ask for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERM_REQ_CODE);
        } else {
            // Permission granted, just initialize bluetooth
            setupBluetooth();
        }
        setupNfc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERM_REQ_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            setupBluetooth();
        } else {
            finishWithToastMessage(getString(R.string.grant_permissions));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_BT && resultCode != RESULT_OK) {
            finishWithToastMessage(getString(R.string.text_enable_bluetooth));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter[] nfcTagFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcTagFilters, null);

        String payload = processNfcIntent(getIntent());
        if (payload != null) {
            openMainActivity(payload, false);
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {

        String text = WyUtils.createConnectionMessage(bluetoothAdapter.getName(),
                wifiP2pDeviceName, "server", WyfilesManager.AuthLevel.STANDARD,
                cipher.getEncodedSecretKey(), cipher.getEncodedInitializationVector());

        return new NdefMessage(new NdefRecord[]{
                NdefRecord.createMime(MIME_TYPE, text.getBytes())
                //, NdefRecord.createApplicationRecord(getPackageName())
        });
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                openMainActivity(null, true);
            }
        });
    }

    private void setupNfc() {

        if (nfcAdapter == null) {
            finishWithToastMessage(getString(R.string.nfc_not_available));
        } else if (!nfcAdapter.isEnabled()) {
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }

        nfcAdapter.setNdefPushMessageCallback(this, this);
        nfcAdapter.setOnNdefPushCompleteCallback(this, this);

        nfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void setupBluetooth() {

        if (bluetoothAdapter == null) {
            finishWithToastMessage(getString(R.string.bluetooth_not_available));
        } else {
            int btState = bluetoothAdapter.getState();
            if (btState == BluetoothAdapter.STATE_OFF) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQ_CODE_BT);
                }
            }
        }
    }

    private String processNfcIntent(Intent intent) {

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                for (Parcelable rawMsg : rawMsgs) {
                    NdefMessage msg = (NdefMessage) rawMsg;
                    for (NdefRecord r : msg.getRecords()) {
                        String type = new String(r.getType());
                        if (type.equals(MIME_TYPE)) {
                            return new String(r.getPayload());
                        }
                    }
                }
            }
        }
        return null;
    }

    private void finishWithToastMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        supportFinishAfterTransition();
    }

    private void openMainActivity(String payload, boolean isServer) {
        startActivity(MainActivity.newIntent(this, payload, isServer),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        supportFinishAfterTransition();
    }
}
