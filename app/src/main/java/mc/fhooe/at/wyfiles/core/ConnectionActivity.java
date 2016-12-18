package mc.fhooe.at.wyfiles.core;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import javax.inject.Inject;

import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.fragments.ConnectionFragment;

public class ConnectionActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback {

    private static final int PERM_REQ_CODE = 0x3431;
    private static final int REQ_CODE_BT = 0x1235;

    @Inject
    protected NfcAdapter nfcAdapter;

    @Inject
    protected BluetoothAdapter bluetoothAdapter;

    private PendingIntent nfcPendingIntent;
    private final String MIME_TYPE = "application/vnd.at.fhooe.mc.wyfiles";

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

        // Bluetooth permission denied, ask for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERM_REQ_CODE);
        } else {
            // Permission granted, just initialize bluetooth
            setupBluetooth();
        }
        setupNfc();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERM_REQ_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            setupBluetooth();
        } else {
            finishWithToastMessage("Sorry, we need Bluetooth permission!");
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
            finishWithToastMessage("Sorry, please enable bluetooth!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter[] nfcTagFilters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcTagFilters, null);

        String payload = processNfcIntent(getIntent());
        if (payload != null) {
            Snackbar.make(findViewById(android.R.id.content), payload, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {

        String delimeter = ";";
        String authType = "none";
        String btName = bluetoothAdapter.getName();
        String role = "server";
        String pin = "1234";

        String text = "auth:" + authType + delimeter + "btdev:" + btName + delimeter
                + "role:" + role + delimeter + "pin:" + pin;

        return new NdefMessage(new NdefRecord[]{
           NdefRecord.createMime(MIME_TYPE, text.getBytes())
           //, NdefRecord.createApplicationRecord(getPackageName())
        });
    }

    private void setupNfc() {

        if (nfcAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "NFC not available on the device... ",
                    Toast.LENGTH_SHORT).show();
            supportFinishAfterTransition();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(),
                    "Please switch on NFC",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }

        nfcAdapter.setNdefPushMessageCallback(this, this);

        nfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void setupBluetooth() {

        if (bluetoothAdapter == null) {
            finishWithToastMessage("Sorry, Bluetooth not available");
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
                for (int i = 0; i < rawMsgs.length; i++) {
                    NdefMessage msg = (NdefMessage) rawMsgs[i];
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

}
