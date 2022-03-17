package com.example.sampleserver;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.Executors;

import sun.misc.Resource;

public class MainActivity extends AppCompatActivity {

    private boolean serverUp = false;
    private HttpServer httpServer;

    String msgLog = "";
    String fileName = "";
    String filePath = "";
    int PORT = 5055;

    TextView serverTextView;
    Button serverButton;
    ScrollView scrollView;
    TextView msg;


    // https://www.youtube.com/watch?v=1JgV3eNs-_4
    // https://docs.oracle.com/javase/7/docs/api/java/net/ServerSocket.html
    // https://dev-gang.ru/article/prostoi-http-server-na-java-8mjy9xrxmt/
    // https://medium.com/hacktive-devs/creating-a-local-http-server-on-android-49831fbad9ca

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fileName = "testFile.txt";
        filePath = "testDir";


        serverTextView = (TextView) findViewById(R.id.serverTextView);
        serverButton = (Button) findViewById(R.id.serverButton);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        msg = (TextView) findViewById(R.id.msg);

        if (hasDeviceBeenRebooted(getApplication())) {
            if (!serverUp && isExternalStorageAvaibleForRW()) {
                startServer(PORT);
                serverUp = true;
                wakeLock.acquire();
            }
        }
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serverUp && isExternalStorageAvaibleForRW()) {
                    startServer(PORT);
                    serverUp = true;
                    wakeLock.acquire();
                } else {
                    stopServer();
                    serverUp = false;
                    wakeLock.release();
                }
            }
        });


    }


    // Определяет было ли устройство перезагружено
    public final boolean hasDeviceBeenRebooted(Application app) {
        String REBOOT_PREFS = "reboot prefs";
        String REBOOT_KEY = "reboot key";
        SharedPreferences sharedPrefs = app.getSharedPreferences(REBOOT_PREFS, 0);
        long expectedTimeSinceReboot = sharedPrefs.getLong(REBOOT_KEY, 0L);
        long actualTimeSinceReboot = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        sharedPrefs.edit().putLong(REBOOT_KEY, actualTimeSinceReboot).apply();
        long prev_start_app = expectedTimeSinceReboot - (long) 9000;
        long next_start_app = expectedTimeSinceReboot + (long) 9000;
        if (prev_start_app <= actualTimeSinceReboot) {
            if (next_start_app >= actualTimeSinceReboot) {
                return false;
            }
        }
        return true;
    }

    // Получение локального IP адреса и порта
    private String getIpAddress() {
        String ERROR = "";
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Site Local Address:\n"
                                + inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();

            // Запись сообщения об ошибке с сетью
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            ERROR += "\n\n" + date + " : ERROR get IP !!! " + e.getMessage() + "\n\n";
            writeData(ERROR);
        }
        return ip;
    }


    // Перевод входящего потока(запроса) в строку
    public static String streamToString(InputStream inputStream) {
        Scanner s = (new Scanner(inputStream)).useDelimiter("\\A");
        if (s.hasNext()) {
            return s.next();
        } else {
            return "";
        }
    }

    // Отправка ответа с сервера(телефона) на запрос
    public void sendResponse(HttpExchange httpExchange, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        os.close();
    }


    // Запуск сервера
    private void startServer(int port) {
        String ERROR = "";

        try {

            // УТОЧНИТЬ ЗАЧЕМ !!!

            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.setExecutor(null);


            // 10.0.2.15:5055/messages

            httpServer.createContext("/messages", new messageHandler());
            httpServer.start();
            serverTextView.setText(getIpAddress() + ":" + port + "\n");
            serverButton.setText("Stop Server");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            msgLog += date + " : Server Start\n";

            viewScrollView(msgLog);

        } catch (IOException e) {
            e.printStackTrace();

            // Запись сообщения об ошибке с сетью
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            ERROR += "\n\n" + date + " : ERROR(startServer) IOException !!! " + e.getMessage() + "\n\n";
            writeData(ERROR);
        }
    }


    public class messageHandler implements HttpHandler {
        String ERROR = "";
        String writedata = "";
        String goodRequest = "{ \n" +
                "    \"StatusOnPhone\": \"Yes\", \n" +
                "}";
        String badRequest = "{ \n" +
                "    \"StatusOnPhone\": \"No\", \n" +
                "}";

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            // Проверка, что пришел POST запрос
            if ("POST".equals(httpExchange.getRequestMethod())) {

                // Получение данных из запроса
                InputStream inputStream = httpExchange.getRequestBody();
                String requestBody = streamToString(inputStream);
                JSONObject jsonBody = null;
                try {
                    jsonBody = new JSONObject(requestBody);
                    // Проверка, что в запросе есть нужные данные
                    if (!jsonBody.get("Phone").toString().isEmpty() && !jsonBody.get("Text").toString().isEmpty() && !jsonBody.get("INN").toString().isEmpty()) {
                        // Отправка ответа, если данных корректны

                        String INN = jsonBody.get("INN").toString();
                        String Phone = jsonBody.get("Phone").toString();
                        String Text = jsonBody.get("Text").toString();

                        // Запись полученных данных из JSON
                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
                        String date = dateFormat.format(Calendar.getInstance().getTime());
                        writedata += date + " : NEW_DATA:" + "\n" +
                                "\t" + INN + "\n" +
                                "\t" + Phone + "\n" +
                                "\t" + Text + "\n";
                        writeData(writedata);

                        // Отправка SMS
                        Telephony.SMS(Phone, Text);

                        sendResponse(httpExchange, goodRequest.toString());
                    } else {
                        // Отправка ответа, если данных повреждены

                        sendResponse(httpExchange, badRequest.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendResponse(httpExchange, e.getMessage());

                    // Запись сообщения об ошибке с распознаванием JSON
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
                    String date = dateFormat.format(Calendar.getInstance().getTime());
                    ERROR += "\n\n" + date + " : ERROR(messageHandler)  JSONException!!! " + e.getMessage() + "\n\n";
                    writeData(ERROR);
                }
            }
        }
    }

    // Остановка сервера
    private void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
            serverTextView.setText("Server is down");
            serverButton.setText("Start Server");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            msgLog += date + " : Server Stop\n\n";
            viewScrollView(msgLog);
        }
    }

    // Вывод данных на дисплей в ScrollView
    private void viewScrollView(String data) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                writeData(data);
                msg.setText(data);
            }
        });
    }


    // Проверка, что во внешнее хранилище можно записывать данные
    private boolean isExternalStorageAvaibleForRW() {
        String exStorageState = Environment.getExternalStorageState();
        if (exStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    // Запись данных в файл
    private void writeData(String data) {
        File myExternalStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        File documentsFile = new File(myExternalStorage, fileName);
        FileOutputStream fos = null;
        try {
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