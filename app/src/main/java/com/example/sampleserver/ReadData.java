package com.example.sampleserver;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReadData {

    // Запись данных в файл
    public static String readData(String fileName) {
        File myExternalStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        File documentsFile = new File(myExternalStorage, fileName);
        StringBuilder text = new StringBuilder();
        String line;
        BufferedReader br = null;
        try {
            if (!documentsFile.exists()) {
                return "";
            }
            br = new BufferedReader(new FileReader(documentsFile));
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();

            boolean dd = documentsFile.exists();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static void deleteFile(String fileName) {
        File myExternalStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        File documentsFile = new File(myExternalStorage, fileName);

        if (documentsFile.exists()) {
            documentsFile.delete();
        }
    }
}
