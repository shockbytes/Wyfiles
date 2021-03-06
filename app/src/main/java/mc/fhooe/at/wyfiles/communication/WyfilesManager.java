package mc.fhooe.at.wyfiles.communication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.util.WyCipher;
import mc.fhooe.at.wyfiles.util.WyUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

public class WyfilesManager {

    public enum AuthLevel {NONE, STANDARD}

    public interface WyfilesCallback {

        enum Severity {WARNING, ERROR}

        void onBluetoothError(Throwable t);

        void onBluetoothConnected(String remoteDeviceName);

        void onWifiDirectError(String message, Severity severity);

        void onWifiDirectFileTransferred(String filename);

        void onWifiDirectError(int messageId, Severity severity);

        void onWifiDirectConnected(String remoteDevice);

    }

    private RxBluetooth rxBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    private NfcAdapter nfcAdapter;
    private UUID uuid;
    private WifiP2pManager wifiP2pManager;

    private WyCipher cipher;

    private boolean useCipher;

    private BluetoothConnection bluetoothConnection;

    private String bluetoothClientName;

    // ------------------ WifiDirect variables ------------------

    private boolean isWifiHost;

    private Salut salutNetwork;

    private SalutDevice connectedSalutDevice;

    @Inject
    public WyfilesManager(BluetoothAdapter bluetoothAdapter, RxBluetooth rxBluetooth, UUID uuid,
                          NfcAdapter nfcAdapter, WifiP2pManager wifiP2pManager, WyCipher cipher) {

        this.bluetoothAdapter = bluetoothAdapter;
        this.rxBluetooth = rxBluetooth;
        this.uuid = uuid;
        this.nfcAdapter = nfcAdapter;
        this.wifiP2pManager = wifiP2pManager;
        this.cipher = cipher;

        useCipher = false;
    }

    public void initializeCipherMode(String encodedIv, String encodedKey) throws Exception {
        cipher.initializeCiphers(encodedIv, encodedKey);
        useCipher = true;
    }

    private void setBluetoothClientName(String name) {
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

    public void establishWifiDirectConnection(@NonNull final Activity activity,
                                              boolean isWifiHost,
                                              final String hostDeviceName,
                                              @NonNull final WyfilesCallback callback) {

        this.isWifiHost = isWifiHost;
        int PORT = 52611;

        SalutDataReceiver dataReceiver = new SalutDataReceiver(activity, new SalutDataCallback() {
            @Override
            public void onDataReceived(Object o) {
                try {

                    WyfilesMessage message = LoganSquare.parse(String.valueOf(o), WyfilesMessage.class);

                    if (useCipher) {
                        message = cipher.decryptWyfilesMessage(message);
                    }

                    handleFileStorage(message, callback);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        SalutServiceData serviceData = new SalutServiceData("wyfiles", PORT, "WyfilesNet");

        salutNetwork = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                callback.onWifiDirectError(R.string.wifi_no_support, WyfilesCallback.Severity.ERROR);
            }
        });

        if (isWifiHost) {
            salutNetwork.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    connectedSalutDevice = salutDevice;
                    callback.onWifiDirectConnected(salutDevice.deviceName);
                }
            });
        } else {
            salutNetwork.discoverNetworkServices(new SalutDeviceCallback() {
                @Override
                public void call(final SalutDevice salutDevice) {

                    // Only connect if device names are equal to exchanged wifi direct name
                    if (salutDevice.deviceName.equals(hostDeviceName)) {
                        salutNetwork.registerWithHost(salutDevice, new SalutCallback() {
                            @Override
                            public void call() {
                                connectedSalutDevice = salutDevice;
                                callback.onWifiDirectConnected(salutDevice.deviceName);
                                //salutNetwork.stopServiceDiscovery(false);

                            }
                        }, new SalutCallback() {
                            @Override
                            public void call() {
                                callback.onWifiDirectError(R.string.wifi_register_error, WyfilesCallback.Severity.ERROR);
                            }
                        });
                    }
                }
            }, false);
        }

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

    public String decryptIfNecessary(String encrypted) {

        // Only decrypt message, if cipher is enabled, otherwise pass back plaintext
        if (useCipher) {
            try {
                encrypted = cipher.decryptMessage(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encrypted;
    }

    public void sendBluetoothMessage(String text) {

        if (bluetoothConnection != null) {

            if (useCipher) { // If encryption is available, apply it before sending
                try {
                    text = cipher.encryptMessage(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            text += "\n"; // Always append line break at the end
            bluetoothConnection.send(text);
        }
    }

    public void sendBluetoothGameRequest(int gameId) {
        sendBluetoothMessage(WyUtils.createGameRequestMessage(gameId));
    }

    private void sendBluetoothExitRequest() {
        sendBluetoothMessage(WyUtils.createExitMessage());
    }

    public void sendFileViaWifi(@NonNull File file, @NonNull final WyfilesCallback callback) {

        if (connectedSalutDevice != null) {
            WyfilesMessage msg = craftWyfilesMessage(file);
            if (msg != null) {

                SalutCallback onFailureCallback = new SalutCallback() {
                    @Override
                    public void call() {
                        callback.onWifiDirectError(R.string.wifi_cannot_send_message, WyfilesCallback.Severity.WARNING);
                    }
                };

                if (isWifiHost) {
                    salutNetwork.sendToDevice(connectedSalutDevice, msg, onFailureCallback);
                } else {
                    salutNetwork.sendToHost(msg, onFailureCallback);
                }

            } else {
                callback.onWifiDirectError(R.string.wifi_cannot_craft_message, WyfilesCallback.Severity.WARNING);
            }
        }
    }

    public void destroy() {

        sendBluetoothExitRequest();

        // Tear down bluetooth
        if (rxBluetooth != null) {
            rxBluetooth.cancelDiscovery();
        }
        if (bluetoothConnection != null) {
            bluetoothConnection.closeConnection();
        }

        // Tear down wifi direct
        if (salutNetwork != null) {
            if (isWifiHost) {
                salutNetwork.stopNetworkService(false);
            } else {
                if (salutNetwork.isConnectedToAnotherDevice) {
                    salutNetwork.unregisterClient(false);
                } else {
                    salutNetwork.stopServiceDiscovery(false);
                }
            }
        }
    }

    private void handleFileStorage(WyfilesMessage msg, WyfilesCallback callback) {

        File tmpFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/wyfiles/" + msg.filename);

        try {

            if (!tmpFile.exists()) {
                tmpFile.getParentFile().mkdirs();
                tmpFile.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
            IOUtils.writeChunked(msg.payload, fileOutputStream);
            fileOutputStream.close();
            callback.onWifiDirectFileTransferred(msg.filename);

            // Free occupied memory
            msg.payload = null;

        } catch (IOException e) {
            e.printStackTrace();
            callback.onWifiDirectError(e.getMessage(), WyfilesCallback.Severity.WARNING);
        }
    }

    private WyfilesMessage craftWyfilesMessage(File f) {

        WyfilesMessage msg = null;

        try {
            String filename = f.getName();
            byte[] payload = IOUtils.toByteArray(new FileInputStream(f));
            msg = new WyfilesMessage(filename, payload);

            if (useCipher) {
                msg = cipher.encryptWyfilesMessage(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg;
    }

    @Nullable
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

        rxBluetooth.observeBluetoothSocket("wyfiles_bt_socket", uuid)
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
