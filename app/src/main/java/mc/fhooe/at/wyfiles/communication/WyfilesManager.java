package mc.fhooe.at.wyfiles.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.util.UUID;

import javax.inject.Inject;

import mc.fhooe.at.wyfiles.util.WyFile;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

public class WyfilesManager {

    public interface WyfilesCallback {

        void onBluetoothError(Throwable t);

        void onBluetoothConnected(String remoteDeviceName);

    }

    private RxBluetooth rxBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    private NfcAdapter nfcAdapter;
    private UUID uuid;
    private WifiP2pManager wifiP2pManager;

    private BluetoothConnection bluetoothConnection;

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

    public void connectWithBluetoothDevice(String bluetoothClientName,
                                           @NonNull final WyfilesCallback callback) {

        setBluetoothClientName(bluetoothClientName);
        BluetoothDevice device = getBluetoothDeviceByName();

        if (device != null) {
            rxBluetooth.observeConnectDevice(device, uuid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<BluetoothSocket>() {
                        @Override
                        public void call(BluetoothSocket bluetoothSocket) {
                            setupBluetoothConnection(bluetoothSocket, callback);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            callback.onBluetoothError(throwable);
                        }
                    });
        } else {
            callback.onBluetoothError(new Throwable("Cannot find bluetooth device!"));
        }

    }

    public void establishWifiP2pConnection() {

        // TODO
    }

    public Observable<String> requestBluetoothReadConnection() {

        if (bluetoothConnection == null) {
            Log.wtf("Wyfiles", "Bluetooth connection broken...");
            return Observable.empty();
        }

        return bluetoothConnection.observeStringStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public void sendBluetoothMessage(String text) {

        if (bluetoothConnection != null) {
            text += "\n"; // Always append line break at the end
            bluetoothConnection.send(text);
        }
    }

    public void sendBluetoothHelloMessage() {
        sendBluetoothMessage("Hello from " + bluetoothAdapter.getName());
    }

    public void sendBluetoothGameRequest(int gameId) {
        sendBluetoothMessage("game:" + gameId);
    }

    public void sendBluetoothExitRequest() {
        sendBluetoothMessage("exit");
    }

    public void sendFileViaWifi(WyFile file) {

        // TODO
    }

    public void destroy() {

        sendBluetoothExitRequest();

        if (rxBluetooth != null) {
            rxBluetooth.cancelDiscovery();
        }

        if (bluetoothConnection != null) {
            bluetoothConnection.closeConnection();
        }
    }

    private BluetoothDevice getBluetoothDeviceByName() {

        for (BluetoothDevice d : bluetoothAdapter.getBondedDevices()) {
            if (d.getName().equals(bluetoothClientName)) {
                return d;
            }
        }

        return null;
    }

    private void setupBluetoothConnection(BluetoothSocket socket,
                                          @NonNull WyfilesCallback callback) {

        if (socket == null) {
            callback.onBluetoothError(new Throwable("BluetoothSocket is null!"));
            return;
        }

        try {

            bluetoothConnection = new BluetoothConnection(socket);
            callback.onBluetoothConnected(socket.getRemoteDevice().getName());

        } catch (Exception e) {
            e.printStackTrace();
            callback.onBluetoothError(e.getCause());
        }
    }

    public void startBluetoothServer(@NonNull final WyfilesCallback callback) {

        rxBluetooth.observeBluetoothSocket("wyfiles_socket", uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<BluetoothSocket>() {
                    @Override
                    public void call(BluetoothSocket bluetoothSocket) {

                        setupBluetoothConnection(bluetoothSocket, callback);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onBluetoothError(throwable);
                    }
                });
    }

}
