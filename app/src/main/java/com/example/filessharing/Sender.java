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
        byte[] file_name = (byte[]) objects[2];
        byte[] file_content = (byte[]) objects[3];

        byte[] magic_number = "FLSH".getBytes();
        byte[] file_type = getFileType(file_name);
        byte[] file_extension = getFileExtension(file_name);
        byte[] file_size = ByteBuffer.allocate(4).putInt(file_content.length).array();

        try {
            DataOutputStream dos;
            Socket s = new Socket(ip, port);
            dos = new DataOutputStream(s.getOutputStream());

            //BUFFER
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(magic_number);  // 4 bytes
            baos.write(file_type);  // 3 bytes
            baos.write(file_extension);  // 3 bytes
            baos.write(file_size);  // 4 bytes
            baos.write(file_content);  // a lot :)
            //

            byte[] c = baos.toByteArray();
            dos.write(c, 0, c.length);

            dos.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getFileType(byte[] file_name) {
        String file_name_str = new String(file_name);
        switch (file_name_str.substring(0, 5)) {
            case "image":
                return "IMG".getBytes();
            case "video":
                return "VID".getBytes();
            case "audio":
                return "AUD".getBytes();
        }
        return "DOC".getBytes();
    }

    private byte[] getFileExtension(byte[] file_name) {
        String file_name_str = new String(file_name);
        String file_extension = file_name_str.substring(file_name_str.length() - 3);
        return file_extension.getBytes();
    }
}
