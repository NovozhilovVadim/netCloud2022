package com.geekbrains.cloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8185;//порт пока статика финал

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT); //сервер сокет с заданным портом
        System.out.println("SERVER STARTED...");
        while (true){//бесконечный цикл
            Socket socket = server.accept();//ждем подключения
            System.out.println("Client accepted...");
            FileProcessorHandler handler = new FileProcessorHandler(socket);//каждое подключение к серверу создаём экземпляр файл процессора
            new Thread(handler).start();//в новом потоке
        }
    }

}
