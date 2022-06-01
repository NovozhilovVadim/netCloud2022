package com.geekbrains.cloud.server;

import java.io.*;
import java.net.Socket;

public class FileProcessorHandler implements Runnable {
    private DataInputStream is;
    private DataOutputStream os;
    private BufferedOutputStream bos;

    private byte[] buf;
    private static final int SIZE = 256;

    public FileProcessorHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[SIZE];
        bos = new BufferedOutputStream(socket.getOutputStream());

    }

    @Override
    public void run(){
        try {
            while (true){
                String command = is.readUTF();
                System.out.println("Command: " + command);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
