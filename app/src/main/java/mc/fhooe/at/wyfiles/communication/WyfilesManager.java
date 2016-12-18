package mc.fhooe.at.wyfiles.communication;

import android.bluetooth.BluetoothAdapter;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.util.UUID;

import javax.inject.Inject;

import mc.fhooe.at.wyfiles.util.Game;
import mc.fhooe.at.wyfiles.util.WyFile;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

public class WyfilesManager {

    private RxBluetooth rxBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    private NfcAdapter nfcAdapter;
    private UUID uuid;
    private WifiP2pManager wifiP2pManager;

    private String bluetoothClientName;

    @Inject
    public WyfilesManager(BluetoothAdapter bluetoothAdapter, RxBluetooth rxBluetooth, UUID uuid,
                          NfcAdapter nfcAdapter, WifiP2pManager wifiP2pManager) {

        this.bluetoothAdapter = bluetoothAdapter;
        this.rxBluetooth = rxBluetooth;
        this.uuid = uuid;
        this.nfcAdapter = nfcAdapter;
        this.wifiP2pManager = wifiP2pManager;
    }

    public void setBluetoothClientName(String name) {
        bluetoothClientName = name;
    }

    public void connectWithBluetoothDevice() {

        // TODO
    }

    public void establishWifiP2pConnection() {

        // TODO
    }

    public void startGame(Game g) {

        // TODO
    }

    public void sendFile(WyFile file) {

        // TODO
    }

    public void destroy() {
        if (rxBluetooth != null) {
            rxBluetooth.cancelDiscovery();
        }

        // Unsubscribe bluetooth subscription
    }


}
