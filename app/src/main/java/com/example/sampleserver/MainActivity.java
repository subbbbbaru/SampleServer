package com.example.sampleserver;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = "testFile.txt";
        filePath = "testDir";


        serverTextView = (TextView) findViewById(R.id.serverTextView);
        serverButton = (Button) findViewById(R.id.serverButton);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        msg = (TextView) findViewById(R.id.msg);



        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!serverUp && isExternalStorageAvaibleForRW()){
                    startServer(PORT);
                    serverUp = true;
                }
                else {
                    stopServer();
                    serverUp = false;
                }
            }
        });

    }

    private String getIpAddress(){
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()){
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if(inetAddress.isSiteLocalAddress()){
                        ip += "Site Local Address:\n"
                                + inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e){
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public static String streamToString(InputStream inputStream){

        Scanner s = (new Scanner(inputStream)).useDelimiter("\\A");
        if(s.hasNext()){
            return s.next();
        }
        else {
            return "";
        }
    }

    public void sendResponse(HttpExchange httpExchange, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, bytes.length);
        //serverTextView.setText(httpExchange.getLocalAddress().toString());
        OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        //os.flush();
        os.close();
    }



    private void startServer(int port) {

       try {
           httpServer = HttpServer.create(new InetSocketAddress(port), 0);
           httpServer.setExecutor(null);

           httpServer.createContext("/", new HttpHandler() {
               @Override
               public void handle(HttpExchange httpExchange) throws IOException {
                   //serverTextView.setText("Server is running..." + httpExchange.getLocalAddress().toString());
                   if("GET".equals(httpExchange.getRequestMethod())){
                       sendResponse(httpExchange, "Hello, Andrey!");
                   } else {

                       sendResponse(httpExchange, "Buy, Andrey!");
                   }
               }
           });

           httpServer.createContext("/messages", new messageHandler());
           httpServer.start();


           serverTextView.setText(getIpAddress() + ":" + port + "\n");
           serverButton.setText("Stop Server");
           DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
           String date = dateFormat.format(Calendar.getInstance().getTime());
           msgLog += date + " : Server Start\n";

           viewScrollView(msgLog);
           File myExtStor = new File(getExternalFilesDir(filePath), fileName);
           FileOutputStream fos = null;
           fos = new FileOutputStream(myExtStor);
           fos.write(msgLog.getBytes(StandardCharsets.UTF_8));

       } catch (FileNotFoundException e){
           e.printStackTrace();
       }
       catch (IOException e) {
           e.printStackTrace();
       }

    }



    public class messageHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if("GET".equals(httpExchange.getRequestMethod())){
                sendResponse(httpExchange, "Would be all messages stringified json");
            } else if("POST".equals(httpExchange.getRequestMethod())){
                InputStream inputStream = httpExchange.getRequestBody();
                String requestBody = streamToString(inputStream);
                JSONObject jsonBody = null;


                try {
                    jsonBody = new JSONObject(requestBody);
                    if (!jsonBody.get("Phone").toString().isEmpty() && !jsonBody.get("Text").toString().isEmpty())
                    {
                        String goodRequest = "{ \n" +
                                "    \"StatusOnPhone\": \"Yes\", \n" +
                                "}";
                        String Phone = jsonBody.get("Phone").toString();
                        String Text = jsonBody.get("Text").toString();

                       // Telephony.SMS(Phone, Text);

                        sendResponse(httpExchange, goodRequest.toString());
                    } else {
                        String badRequest = "{ \n" +
                                "    \"StatusOnPhone\": \"No\", \n" +
                                "}";

                        sendResponse(httpExchange, badRequest.toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    sendResponse(httpExchange, e.getMessage());
                }

            }


        }

    }

    private void stopServer() {
        if(httpServer != null){
            try {
                httpServer.stop(0);
                serverTextView.setText("Server is down");
                serverButton.setText("Start Server");



                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
                String date = dateFormat.format(Calendar.getInstance().getTime());
                msgLog += date + " : Server Stop\n\n";




                viewScrollView(msgLog);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }


        }

    }
    private void viewScrollView(String data){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                writeData(data);
                msg.setText(data, TextView.BufferType.SPANNABLE);

            }
        });
    }

    private boolean isExternalStorageAvaibleForRW(){
        String exStorageState = Environment.getExternalStorageState();
        if(exStorageState.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        return false;
    }

    private void writeData(String data) {
        try {
            File myExtStor = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
            FileOutputStream fos = null;
            fos = new FileOutputStream(myExtStor);
            if(myExtStor.exists()){
                fos.
                fos.write(data.getBytes(StandardCharsets.UTF_8));
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}