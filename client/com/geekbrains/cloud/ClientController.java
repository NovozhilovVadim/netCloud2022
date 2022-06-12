package com.geekbrains.cloud;

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
    private static String command;
    private String fileName;

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
        clientView.getItems().add("src");//безусловныый переход на дирректорию вверх
        clientView.getItems().addAll(currentDir.list());//Читаем и передаем в панель список файлов и дирректорий
    }

    private void fillServerDirFile() throws IOException {//получаем список файлов сервера
        serverView.getItems().clear();
        serverView.getItems().add("src");
        os.writeUTF("#LIST#");//запрос списка
        System.out.println("send command #LIST#");

        command = is.readUTF();//ловим ответ
        System.out.println(" Get command: " + command );
        if (command.equals("#LIST#")){//убеждаемся, что сервер нас понял
            int count = is.readInt();// размер списка
            System.out.println(" Count is: " + count);
            for (int i = 0; i < count; i++) {
                fileName = is.readUTF();
                System.out.println(fileName);
                serverView.getItems().add(fileName);
            }
        }
        os.flush();
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
                if (fileNameServer.equals("src")){//если выбрана директория
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

    public void read(){//Слушатель потока
        try {
            System.out.println("Thread read - accept");
            while (true){//цикл
                System.out.println("restart cicle");
                command = is.readUTF();//Слушаем команду из входящего потока
                System.out.println(command);
                if (command.equals("#LIST#")) {//сверяем команду, если список
                    Platform.runLater(() -> serverView.getItems().clear());//очищаем поле списка
                    int count = is.readInt();//читаем размер архива(Количество строчек)
                    for (int i = 0; i < count; i++) {//количество циклов чтения равно количеству строк
                        String fileName = is.readUTF();//читаем имя файла
                        Platform.runLater(() -> serverView.getItems().add(fileName));//пишем имя файла в листвью
                    }
                }
                if (command.equals("#SEND#FILE#")) {//команда на получение файла
                    fileName = is.readUTF();//получаем имя файла
                    buf = new byte[SIZE];//создаём буфер размера SIZE
                    long size = is.readLong();// получаем размер файла от сервера
                    System.out.println("Created file: " + fileName);
                    System.out.println("File size: " + size);
                    Path currentPath = currentDir.toPath().resolve(fileName);//Создаём целевой файл в текущей дирректории
                    try (FileOutputStream fos = new FileOutputStream(currentPath.toFile())) {//создаём поток записи в файл
                        for (int i = 0; i < (size + SIZE - 1) / SIZE; i++) {//запускаем цикл от 0 до размер файла + размер буфера - 1 ии разделить на размер буфера
                            int read = is.read(buf);//читаем поток блоками равными буферу
                            fos.write(buf, 0, read);//пишем в файл
                        }
                    }
                    // client state updated
                    Platform.runLater(this::fillCurrentDirFiles);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            //reconnect to server
            connect();//в случае вылета делаем реконект
        }
    }



    //Скачиваем файл
    public void download(ActionEvent actionEvent) throws IOException {
        fileName = serverLabel.getText();//получаем имя файла из textFieldServer
        os.writeUTF("#LOAD#FILE#");//отправляем команду серверу отправить файл
        System.out.println("#LOAD#FILE#");
        os.writeUTF(fileName);//отправляем серверу имя выбранного файла
        os.flush();//очищаем поток
        serverLabel.setText("");//очищаем поле выбора

    }

    //отправляем файл
    public void upload(ActionEvent actionEvent) throws IOException {
        fileName = clientLabel.getText();//получаем имя файла из textField
        System.out.println(fileName);
        File currentFile = currentDir.toPath().resolve(fileName).toFile();// создаем объект
        System.out.println(currentFile);
        os.writeUTF("#SEND#FILE#");//отправляем команду серверу на прием файла
        os.writeUTF(fileName);//передаём имя файла
        os.writeLong(currentFile.length());//предаем размер файла
        try (FileInputStream is = new FileInputStream(currentFile)){//создаём поток чтения файла
            while (true){//создаем бесконечный цикл
                int read = is.read(buf); // создаём числовую переменную из потока чтения файла
                if (read == -1) { //если прочитали всё - прерываем цикл
                    break;
                }
                os.write(buf, 0, read);// пишем в исходящий поток
            }
        }
        os.flush();//очищаем исходящий поток
        System.out.println("Файл отправлен");
        clientLabel.setText("");//очищаем поле выбора

    }

    //Переопределяем инишалаёзбл
    @Override
    public void initialize(URL location, ResourceBundle resources){//Получаем урл и папку с ресурсом
        try {
            connect();
            System.out.println("connect passed...");
            fillCurrentDirFiles();
            System.out.println("fillCurrentDirFiles passed...");
            fillServerDirFile();
            System.out.println("fillServerDirFile passed...");
            initClickListener();
            System.out.println("initClickListener passed...");

            //запускаем слушателя в демоне, чтобы он убивался после остановки приложения
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
