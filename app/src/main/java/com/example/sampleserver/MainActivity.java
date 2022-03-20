package com.example.sampleserver;

import static com.example.sampleserver.constants.ctpass;
import static com.example.sampleserver.constants.kspass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.renderscript.Element;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;

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
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class MainActivity extends AppCompatActivity {

    private boolean serverUp = false;
    private HttpServer httpServer;
    private HttpsServer httpsServer;

    String msgLog = "";



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

        serverTextView = (TextView) findViewById(R.id.serverTextView);
        serverButton = (Button) findViewById(R.id.serverButton);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        msg = (TextView) findViewById(R.id.msg);

        if (hasDeviceBeenRebooted(getApplication())) {
            if (!serverUp && WriteData.isExternalStorageAvaibleForRW()) {
                startServer(constants.PORT);
                serverUp = true;
                wakeLock.acquire();
            }
        }


        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serverUp && WriteData.isExternalStorageAvaibleForRW()) {
                    startServer(constants.PORT);
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




    // Запуск сервера
    private void startServer(int port) {
        String ERROR = "";

        try {

            // HTTPS запрос
            // https://stackoverflow.com/questions/31448260/nanohttpd-https-server-in-android-android-and-ios-browser-connect-fail
            // https://ktor.io/docs/ssl.html#convert-certificate
            // https://proandroiddev.com/running-tls-protected-http-server-on-android-using-ktor-49bdbc7f5e1f
            // https://github.com/AlphaGarden/SSL-Client-Server/blob/master/src/main/java/server/SSLServer.java
            // https://stackoverflow.com/questions/31270613/https-server-on-android-device-using-nanohttpd




            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getResources().getAssets().open("certificate-server.p12"),constants.kspass);

           /* KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore,passfrase);*/

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, constants.ctpass);


            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("X509");
            tmFactory.init(keyStore);

            X509TrustManager x509TrustManager = null;
            for (TrustManager trustManager : tmFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManager;
                    break;
                }
            }

            if (x509TrustManager == null) throw new NullPointerException();

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(kmf.getKeyManagers(),tmFactory.getTrustManagers(),null);



            httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
            httpsServer.setExecutor(null);



            HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
            httpsServer.setHttpsConfigurator(httpsConfigurator);


            httpsServer.createContext("/login", new LoginHandler());
            httpsServer.createContext("/messages", new messageHandler());


            httpsServer.start();
            serverTextView.setText(GetIpAddress.getIpAddress() + ":" + port + "\n");
            serverButton.setText("Stop Server");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            msgLog += date + " : HTTPS Server Start\n";

            viewScrollView(msgLog);


//  ДЛЯ HTTP ЗАПРОСА


/*

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

            viewScrollView(msgLog);*/

        } catch (IOException /*| NoSuchAlgorithmException*/ e) {
            e.printStackTrace();

            // Запись сообщения об ошибке с сетью
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            ERROR += "\n\n" + date + " : ERROR(startServer) IOException !!! " + e.getMessage() + "\n\n";
            WriteData.writeData(ERROR);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }




    // Остановка сервера
    private void stopServer() {
        if (httpsServer != null) {
            httpsServer.stop(0);
            serverTextView.setText("Server is down");
            serverButton.setText("Start Server");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            msgLog += date + " : HTTPS Server Stop\n\n";
            viewScrollView(msgLog);
        }
/*        if (httpServer != null) {
            httpServer.stop(0);
            serverTextView.setText("Server is down");
            serverButton.setText("Start Server");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            msgLog += date + " : Server Stop\n\n";
            viewScrollView(msgLog);
        }*/
    }

    // Вывод данных на дисплей в ScrollView
    private void viewScrollView(String data) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WriteData.writeData(data);
                msg.setText(data);
            }
        });
    }






}