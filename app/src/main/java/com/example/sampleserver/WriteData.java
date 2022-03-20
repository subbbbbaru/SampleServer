package com.example.sampleserver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WriteData {



    // Проверка, что во внешнее хранилище можно записывать данные
    public static boolean isExternalStorageAvaibleForRW() {
        String exStorageState = Environment.getExternalStorageState();
        if (exStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }



    // Запись данных в файл
    public static void writeData(String data) {
        File myExternalStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        File documentsFile = new File(myExternalStorage, constants.fileName);
        FileOutputStream fos = null;
        try {
            if (!documentsFile.exists()){
                documentsFile.createNewFile();
            }
            boolean dd = documentsFile.exists();
            fos = new FileOutputStream(documentsFile, true);
            fos.write(data.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
