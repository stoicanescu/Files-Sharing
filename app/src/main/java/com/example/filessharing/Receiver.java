package com.example.filessharing;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

class Receiver implements Runnable{
    private int port;
    private Activity mActivity;

    private byte[] magic_number = new byte[4];
    private byte[] file_type = new byte[3];
    private byte[] file_extension = new byte[3];
    private byte[] file_size = new byte[4];
    private byte[] file_received;

    Receiver(Activity mActivity, int port) {
        this.port = port;
        this.mActivity = mActivity;
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);

            while(true) {
                Socket mySocket = ss.accept();
                DataInputStream dis = new DataInputStream(mySocket.getInputStream());

                if( dis.read(magic_number, 0, 4) != 4 ||
                        dis.read(file_type, 0, 3) != 3 ||
                        dis.read(file_extension, 0, 3) != 3 ||
                        dis.read(file_size, 0, 4) != 4) {

                    System.out.println("Error while sending!");
                    mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity.getApplicationContext(), "Error while sending!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    return;
                }

                if(!Arrays.equals(magic_number, "FLSH".getBytes())) {
                    System.out.println("Error, not our format!");
                    return;
                }

                int f_size = ByteBuffer.wrap(file_size).getInt();

                file_received = new byte[f_size];
                int bytes_read = dis.read(file_received, 0, f_size);
                int total_bytes_read = bytes_read;
                while(total_bytes_read != f_size) {
                    bytes_read = dis.read(file_received, total_bytes_read, f_size - total_bytes_read);
                    total_bytes_read += bytes_read;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(_saveFile(file_received))
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity.getApplicationContext(), "File received!", Toast.LENGTH_SHORT).show();
                                }
                        });
                    }

                    private String _computeFileName(byte[] file_type, byte[] file_extension, int file_size) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                        formatter.format(date);

                        String f_type = new String(file_type);
                        String f_date = formatter.format(date);

                        String f_extension = new String(file_extension);

                        String file_name = f_type + "_" + f_date + "_" + file_size + "." + f_extension;
                        System.out.println(file_name);
                        return file_name;
                    }

                    private boolean _saveFile(byte[] file_received) {
                        String file_name = _computeFileName(file_type, file_extension, ByteBuffer.wrap(file_size).getInt());

                        File root_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "FilesSharing");
                        if(root_dir.mkdir() || root_dir.exists()) {
                            File file = new File(root_dir, file_name);
                            OutputStream os;
                            try {
                                os = new FileOutputStream(file);
                                MediaScannerConnection.scanFile(mActivity, new String[] { file.getAbsolutePath() }, null, null);
                                os.write(file_received);
                                os.close();
                                return true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            System.out.println("The app doesn't have access to Storage");
                        return false;
                    }

                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
