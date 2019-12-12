package com.example.filessharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

public class MainActivity extends Activity {

    Button wifiButton, discover;

    WifiConnectionReceiver wifiReceiver;
    IntentFilter mIntentFilter;

    WifiManager wifiManager;
    NsdHelper nsdHelper;

    String serviceName;
    int servicePort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        createUniqueServiceName();

        initLayoutComponents();
        initWifiConnectionReceiver();
        initNsdHelper();

        servicePort = generatePort(); //get available port
        Receiver s = new Receiver(this, servicePort);
        Thread myThread = new Thread(s);
        myThread.start();

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
        nsdHelper.initializeResolveListener();
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
            nsdHelper.startRegisterService(servicePort);
        registerReceiver(wifiReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setWifiOn(View view) {
        wifiManager.setWifiEnabled(true);
    }

    public void discoverServices(View view) {
        nsdHelper.startDiscoverServices();
    }

    int generatePort() {
        ServerSocket socket = null;
        int port = 9000;
        try {
            socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    private static final int READ_REQUEST_CODE = 42;

    //Storage Access Framework (SAF)
    public void performFileSearch(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = new String[]{};
        intent.setType("*/*");

        switch (view.getId()) {
            case R.id.photo_video_button: {
                mimeTypes = new String[]{"image/*", "video/*"};
                break;
            }
            case R.id.documents_button: {
                mimeTypes = new String[]
                        {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                                "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                                "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                                "text/plain",
                                "application/pdf",
                                "application/zip"};
                break;
            }
            case R.id.music_button: {
                mimeTypes = new String[]{"audio/*"};
                break;
            }
        }
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                Log.i("titi", "Uri: " + uri.toString());
                InputStream iStream = null;
                try {
                    iStream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    File file= new File(uri.getPath());

                    byte[] inputData = getBytes(iStream);
                    byte[] file_name = file.getName().getBytes();
                    Sender bt = new Sender();
                    bt.execute(nsdHelper.getIp_receiver_device(), nsdHelper.getPort_receiver_device(), inputData, file_name);

                }
                catch (Exception e) {}
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
