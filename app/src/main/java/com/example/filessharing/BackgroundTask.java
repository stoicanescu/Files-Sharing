package com.example.filessharing;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class BackgroundTask extends AsyncTask {
    Socket s;
    DataOutputStream dos;
    InetAddress ip;
    int port;
    String message;
    Activity mActivity;

    @Override
    protected Object doInBackground(Object[] objects) {
        ip = (InetAddress) objects[0];
        port = (int) objects[1];
        message = (String) objects[2];
        mActivity = (Activity) objects[3];
        try {
            s = new Socket(ip, port);
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(message);

            dos.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
