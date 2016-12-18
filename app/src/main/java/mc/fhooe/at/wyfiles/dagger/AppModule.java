package mc.fhooe.at.wyfiles.dagger;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Vibrator;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

@Module
public class AppModule {

    private Application application;

    public AppModule(Application app) {
        application = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public NfcAdapter provideNfcAdapter() {
        return NfcAdapter.getDefaultAdapter(application);
    }

    @Provides
    @Singleton
    public BluetoothAdapter provideBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    @Provides
    @Singleton
    public WifiP2pManager provideWifiP2pManager() {
        return (WifiP2pManager) application.getSystemService(Context.WIFI_P2P_SERVICE);
    }

    @Provides
    @Singleton
    public UUID provideUUID() {
        return UUID.fromString("5f77cdee-8f48-4614-9958-d2736d9027c5");
    }

    @Provides
    @Singleton
    public RxBluetooth provideRxBluetooth() {
        return new RxBluetooth(application);
    }

    @Provides
    @Singleton
    public WyfilesManager provideWyfilesManager(RxBluetooth bluetooth, UUID uuid,
                                                BluetoothAdapter bluetoothAdapteradapter,
                                                WifiP2pManager wifiP2pManager,
                                                NfcAdapter nfcAdapter) {
        return new WyfilesManager(bluetoothAdapteradapter, bluetooth, uuid, nfcAdapter, wifiP2pManager);
    }

    @Provides
    @Singleton
    public Vibrator provideVibrator() {
        return (Vibrator) application.getSystemService(Context.VIBRATOR_SERVICE);
    }


}
