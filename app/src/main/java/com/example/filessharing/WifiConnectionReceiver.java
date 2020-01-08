package com.example.filessharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class WifiConnectionReceiver extends BroadcastReceiver {

    private MainActivity mActivity;
    private WifiManager wifiManager;

    WifiConnectionReceiver(WifiManager wifiManager, MainActivity mActivity) {
        this.mActivity = mActivity;
        this.wifiManager = wifiManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiManager.WIFI_STATE_ENABLED) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                if (wifiInfo.getNetworkId() == -1) {
                    try {
                        final Intent mIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        context.startActivity(mIntent);
                    } catch (Exception ignored) {}
                }

                final String ssid  = wifiInfo.getSSID();
                mActivity.getChecked_text_view().setText(ssid);
                mActivity.getChecked_text_view().setTextSize(12);
                mActivity.getChecked_text_view().setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
                mActivity.getWifiButton().setEnabled(false);
            }
            else {
                System.out.println("WIFI_STATE = " + state);
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
                mActivity.getChecked_text_view().setText("Please connect to Wifi");
                mActivity.getChecked_text_view().setCompoundDrawablesWithIntrinsicBounds(R.drawable.uncheck_mark, 0, 0, 0);
                mActivity.getWifiButton().setEnabled(true);
            }
        }
    }
}
