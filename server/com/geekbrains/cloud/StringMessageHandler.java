package com.geekbrains.cloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class StringMessageHandler  implements Runnable{
    private DataOutputStream os;//создаём поток данных
    private DataInputStream is;//сщздаём поток данных

    public StringMessageHandler(Socket socket) throws IOException{//инициализируем потоки
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run(){//переопределям ранабл
        try {
            while (true){
                String message = is.readUTF();//читаем строку из потока
                System.out.println("received message: " + message);//вывод в консоль
                os.writeUTF(message);//оправляем обратно в поток
                os.flush();//записываем
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }

}
