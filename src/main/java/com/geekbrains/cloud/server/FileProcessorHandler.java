package com.geekbrains.cloud.server;

import javax.sound.midi.Patch;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

public class FileProcessorHandler implements Runnable {//наследуемся от ранбл
    private DataInputStream is;//создаём потоки
    private DataOutputStream os;
    private BufferedOutputStream bos;//BufferedOutputStream переопределяет (override) методы, унаследованные от его родительского класса,
    // такие как write(), write(byte[]) и т. д., чтобы гарантировать, что данные будут записаны в buffer, а не в целевой объект (например, файл).
    // Но когда buffer заполнен, все данные в buffer будут помещены OutputStream и buffer будет освобожден.

    private byte[] buf;//Массив бвйт для буфера
    private File currentDir; //Базовый каталог пользователя
    private static final int SIZE = 256;

    public FileProcessorHandler(Socket socket) throws IOException {//Инициализирем всё
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[SIZE];
        bos = new BufferedOutputStream(socket.getOutputStream());
        currentDir = new File("serverDir"); //пока так. Потом реализуем поиск каталога по базе пользователей и будем присваивать

    }

    @Override
    public void run(){//переопределяем ранбл
        try {
            while (true){
                String command = is.readUTF();//Ждём комманду
                System.out.println("Command: " + command);
                if (command.equals("#SEND#FILE#")){//команда для загрузки файла
                    String fileName = is.readUTF();//получаем имя файла
                    long size = is.readLong();//получаем размер файла
                    System.out.println("Created file " + fileName);
                    System.out.println("Size file: " + size);
                    Path currentPath = currentDir.toPath().resolve(fileName);//создаём файл с полученным именем
                    try (FileOutputStream fos = new FileOutputStream(currentPath.toFile())){//создаём поток для записи в файл
                        for (int i = 0; i < (size + SIZE - 1) / SIZE; i++ ){//количество проходов размер + 254 и разделить на размер
                            int read = is.read(buf);//побайтно читаем буфер
                            fos.write(buf, 0, read);//пишем в файл
                        }
                    }
                    os.writeUTF("File successfully uploaded");
                    os.flush();
                }
                if (command.equals("#LIST#")){//запрос списмка файлов
                    String[] files = currentDir.list();//создаём строковый массив из списка имен вайлов и каталогов
                    if (files != null){//убеждаемся что список не пустой
                        os.writeUTF("#LIST#");//отправляем команду листинга
                        os.writeInt(files.length);//отправляем размер строкового массива
                        System.out.println(files.length);
                        for (String file: files) {//проходим циклом по массиву и
                            System.out.println(file);
                            os.writeUTF(file);//и отправляем имена файлов и каталогов
                        }
                    }
                    System.out.println("FileList send...");
                    os.flush();
                }
                if (command.equals("#DIR#UP")){//каталог вверх ДОБАВИТЬ ОГРАНИЧЕНИЕ ПЕРЕХОДОВ
                    System.out.println("command: " + command);
                }
                if (command.equals("#DIR#DOWN#")){//войти в дирректорию
                    System.out.println("command: " + command);
                }
                if (command.equals("#LOAD#FILE#")) {//комманда для отправки файла
                    System.out.println("command: " + command);
                    String fileName = is.readUTF();//получаем из потока имя вайла который надо отправить
                    System.out.println(fileName);
                    File currentFile = currentDir.toPath().resolve(fileName).toFile();//находим нужный файл
                    os.writeUTF("#SEND#FILE#");//команда передачи файла
                    os.writeUTF(fileName);//имя передаваемого файла
                    System.out.println(currentFile);
                    os.writeLong(currentFile.length());//размер
                    try (FileInputStream fis = new FileInputStream(currentFile)) {//читаем сам файл и
                        while (true) {
                            int read = fis.read(buf);
                            if (read == -1) {//если буфер пуст выходим из цикла
                                break;
                            }
                            os.write(buf, 0, read);//отправляем
                        }
                    }
                    System.out.println("File Send");
                    os.flush();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
