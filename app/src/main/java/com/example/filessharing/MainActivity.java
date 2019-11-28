package com.example.filessharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button wifiButton, discover;

    WifiConnectionReceiver wifiReceiver;
    IntentFilter mIntentFilter;

    WifiManager wifiManager;
    NsdHelper nsdHelper;

    String serviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createUniqueServiceName();

        initLayoutComponents();
        initWifiConnectionReceiver();
        initNsdHelper();
    }

    private void createUniqueServiceName() {
        serviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
    }

    private void initLayoutComponents() {
        wifiButton = (Button) findViewById(R.id.wifi_button);
        discover = (Button) findViewById(R.id.discover);
    }

    private void initWifiConnectionReceiver() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiConnectionReceiver(wifiManager, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

    }

    private void initNsdHelper() {
        nsdHelper = new NsdHelper(this, serviceName);
        nsdHelper.initializeRegistrationListener();
        nsdHelper.initializeDiscoveryListener();
    }

    @Override
    protected void onPause() {
        if (nsdHelper != null) {
            nsdHelper.tearDown();
        }
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nsdHelper != null)
            nsdHelper.startRegisterService(9000);
        registerReceiver(wifiReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        if (nsdHelper != null)
            nsdHelper.tearDown();
        super.onDestroy();
    }


    public void setWifiOn(View view) {
        wifiManager.setWifiEnabled(true);
    }

    public void discoverServices(View view) {
        nsdHelper.startDiscoverServices();
    }
}
