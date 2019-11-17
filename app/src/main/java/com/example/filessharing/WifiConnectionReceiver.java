package com.example.filessharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Button;
import android.widget.Toast;

public class WifiConnectionReceiver extends BroadcastReceiver {

    private WifiManager wifiManager;
    private MainActivity mActivity;
    Button wifiButton;

    WifiConnectionReceiver(WifiManager wifiManager, MainActivity mActivity) {
        this.wifiManager = wifiManager;
        this.mActivity = mActivity;
        wifiButton = (Button) mActivity.findViewById(R.id.wifi_button);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiManager.WIFI_STATE_ENABLED) {
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();
                wifiButton.setEnabled(false);
            }
            else {
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
                wifiButton.setEnabled(true);
            }
        }
    }
}
