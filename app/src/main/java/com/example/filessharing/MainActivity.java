package com.example.filessharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.nsd.NsdManager;
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
    static boolean discoveryStarted = false;

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
        nsdHelper.registerService(9000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiReceiver, mIntentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(wifiReceiver != null)
            unregisterReceiver(wifiReceiver);
    }


    public void setWifiOn(View view) {
        wifiManager.setWifiEnabled(true);
    }

    public void discoverServices(View view) {
        if(!discoveryStarted)
            nsdHelper.startDiscoverServices();
            discoveryStarted  = true;
    }
}
