package com.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import sun.nio.ch.Net;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class ClientController implements Initializable {
    //делаем разметку
    @FXML
    ListView<String> clientView;
    @FXML
    ListView<String> serverView;
    @FXML
    Label clientLabel;
    @FXML
    Label serverLabel;

    //Запас переменных и статических значений, не забыть почистить от ненужного

    private Socket socket;//сокет
    private DataInputStream is;//входящий поток
    private DataOutputStream os;//исходящий поток
    private File currentDir; //текущая директория
    private BufferedInputStream bis;// буферные потоки... может будут нужны... не забыть почистить
    private BufferedOutputStream bos;// буферные потоки... может будут нужны... не забыть почистить
    private byte[] buf;//Буфер для чтения
    private static final int SIZE = 256;//Размер для буфера
    private static final int PORT = 8185;//порт пока статика финал
    private static final String HOST = "localhost";//Адрес сервера
//    private Files files;
//    private ListView<String> list;

//    private ExecutorService executorService;
//    private static String command;
//    private String fileName;

    //Инициализируем подключение, переменные и т.п
    public void connect(){
        try {
            socket = new Socket(HOST, PORT);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            buf = new byte[SIZE];
            currentDir = new File("src/resources");
            bis = new BufferedInputStream(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
            System.out.println("Connected accept...");

        } catch (IOException e) {
            System.out.println("Exception connect... ");
            e.printStackTrace();
        }
    }

    //Заполняем панель домашней дирректории
    private void fillCurrentDirFiles(){
        clientView.getItems().clear();//чистим поле
        clientView.getItems().add("..");//безусловныый переход на дирректорию вверх
        clientView.getItems().addAll(currentDir.list());//Читаем и передаем в панель список файлов и дирректорий
    }

    //Контролим действия мыши
    private void initClickListener(){//ловим действия мыши
        clientView.setOnMouseClicked(e -> {//создаем обработчик для домашнего фрейма
            if (e.getClickCount() == 2) {//если двойной клик
                String fileName = clientView.getSelectionModel().getSelectedItem();//вытаскиваем имя файла
                System.out.println("Выбран файл " + fileName);
                Path path = currentDir.toPath().resolve(fileName);//предаем обьекту дирректории имя папки
                if (Files.isDirectory(path)){//если выбрана директория
                    currentDir = path.toFile();//устанавливаем новую папку текущей
                    fillCurrentDirFiles();//перегружаем список
                    clientLabel.setText("");//очищаем поле подсказки
                }else {
                    clientLabel.setText(fileName);//показываем в поле подсказки имя выбранного файла
                }
            }
        });

        serverView.setOnMouseClicked(e -> {//создаем обработчик для домашнего фрейма
            if (e.getClickCount() == 2) {//если двойной клик
                String fileNameServer = serverView.getSelectionModel().getSelectedItem();//вытаскиваем имя файла
                if (fileNameServer.equals("..")){//если выбрана директория
                    try {
                        os.writeUTF("#DIR#UP#");//отправляем команду на шаг выше
                        System.out.println("#DIR#UP#");
                        os.flush();//записываем в поток
//                        fillServerDirFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }else {
                    System.out.println("Выбран файл " + fileNameServer);
                    Path pathServer = currentDir.toPath().resolve(fileNameServer);//задел на дальше
                    serverLabel.setText(fileNameServer);//показываем в поле подсказки имя выбранного файла
                }
            }
        });
    }

    //Скачиваем файл
    public void download(ActionEvent actionEvent) {
    }

    //отправляем файл
    public void upload(ActionEvent actionEvent) {
    }

    //Переопределяем инишалаёзбл
    @Override
    public void initialize(URL location, ResourceBundle resources){//Получаем урл и папку с ресурсом
        try {
            connect();
            fillCurrentDirFiles();
            initClickListener();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
