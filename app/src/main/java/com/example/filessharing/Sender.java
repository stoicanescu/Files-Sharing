package com.example.filessharing;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

class Sender extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] objects) {
        InetAddress ip = (InetAddress) objects[0];
        int port = (int) objects[1];
        byte[] message = (byte[]) objects[2];
        try {
            DataOutputStream dos;
            Socket s = new Socket(ip, port);
            dos = new DataOutputStream(s.getOutputStream());

            byte[] file_name = (byte[]) objects[3];


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(file_name);
            System.out.println("zize: " + file_name.length);
            baos.write(ByteBuffer.allocate(4).putInt(message.length).array());  // file length
            baos.write(message);

            byte[] c = baos.toByteArray();
            dos.write(c, 0, c.length);


            dos.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
