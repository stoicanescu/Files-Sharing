package com.example.filessharing;

import android.app.Activity;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

class Receiver implements Runnable{
    private int port;
    private ServerSocket ss;
    private Socket mySocket;
    private DataInputStream dis;
    private byte[] file_received;
    private Activity mActivity;
    private byte[] message_length_bytes;
    Receiver(Activity mActivity, int port) {
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
                byte[] file_name = new byte[11];
                dis.read(file_name, 0, 11);
                System.out.println("file name: " + file_name);

                message_length_bytes = new byte[4];
                dis.read(message_length_bytes, 0, 4);

                int file_size = ByteBuffer.wrap(message_length_bytes).getInt();
                file_received = new byte[file_size];
                int bytes_read = dis.read(file_received, 0, file_size);
                int total_bytes_read = bytes_read;
                while(total_bytes_read != file_size) {
                    bytes_read = dis.read(file_received, total_bytes_read, file_size - total_bytes_read);
                    total_bytes_read += bytes_read;
                }

                System.out.println("bytes: " + String.format("%02x", file_received[4]) + String.format("%02x", file_received[5]) + String.format("%02x", file_received[6]));
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File file = new File(mActivity.getExternalFilesDir(null), "sent_file.jpg");
                            OutputStream os;
                            os = new FileOutputStream(file);
                            os.write(file_received);
                            os.close();
                        }
                        catch (Exception e){}
                        Toast.makeText(mActivity.getApplicationContext(), "File received!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
