package com.example.filessharing;

import android.app.Activity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server implements Runnable{
    private int port;
    private ServerSocket ss;
    private Socket mySocket;
    private DataInputStream dis;
    private BufferedReader bufferedReader;
    private String message;
    private Activity mActivity;

    Server(Activity mActivity, int port) {
        this.port = port;
        this.mActivity = mActivity;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(port);

            while(true) {
                mySocket = ss.accept();
                dis = new DataInputStream(mySocket.getInputStream());
                message = dis.readUTF();

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity.getApplicationContext(), "Message: " + message + " received!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
