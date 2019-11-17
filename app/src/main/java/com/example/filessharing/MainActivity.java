package com.example.filessharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button wifiButton;

    WifiConnectionReceiver wifiReceiver;
    IntentFilter mIntentFilter;

    WifiManager wifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        wifiButton = (Button) findViewById(R.id.wifi_button);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiConnectionReceiver(wifiManager, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
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

    public void setWifiOn(View view) {
        wifiManager.setWifiEnabled(true);
    }
}
