package com.geekbrains.cloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class ClientController {
    @FXML
    ListView<String> listView;
    @FXML
    TextField textField;
    @FXML
    ListView<String> listViewServer;
    @FXML
    TextField textFieldServer;

    private DataInputStream is; // Входящий поток
    private DataOutputStream os;// Исходящий поток
    private BufferedInputStream bis;//  Входящий поток буфера
    private File currentDir; // Текущая дирректория
    private byte[] buf; // Буфер
    private static final int SIZE = 256; //размер буфера
    private static final int PORT = 8185; //порт
    private static final String HOST = "localhost"; // адресс хоста
    private Socket socket;// сокет
    private ExecutorService executorService; // сервис потоков
    private static String command;
    String fileName;

    public void connect(){ //Подключение к серверу
        try {
            buf = new byte[SIZE];// устанавливаем размер буфера
            currentDir = new File("home");//определяем домашнюю дирректорию
            socket= new Socket(HOST, PORT);//создаем подключение
            is = new DataInputStream(socket.getInputStream()); // создаём входящий поток данных
            os = new DataOutputStream(socket.getOutputStream());//создаём исхлдящий поток данных
            System.out.println("Connected accept");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void loadFile(ActionEvent actionEvent) {
    }

    public void sendFile(ActionEvent actionEvent) {
    }



}
