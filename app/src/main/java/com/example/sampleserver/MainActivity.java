package com.example.sampleserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private boolean serverUp = false;
    private HttpServer httpServer;

    TextView serverTextView;
    Button serverButton;


    // https://www.youtube.com/watch?v=1JgV3eNs-_4
    // https://docs.oracle.com/javase/7/docs/api/java/net/ServerSocket.html
    // https://dev-gang.ru/article/prostoi-http-server-na-java-8mjy9xrxmt/
    // https://medium.com/hacktive-devs/creating-a-local-http-server-on-android-49831fbad9ca

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int PORT = 5055;

        serverTextView = findViewById(R.id.serverTextView);
        serverButton = findViewById(R.id.serverButton);

        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!serverUp){
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

    public static String streamToString(InputStream inputStream){

        Scanner s = (new Scanner(inputStream)).useDelimiter("\\A");
        if(s.hasNext()){
            return s.next();
        }
        else {
            return "";
        }
    }

    public static void sendResponse(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.sendResponseHeaders(200, responseText.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(responseText.getBytes(Charset.defaultCharset()));
        os.flush();
        os.close();
    }



    private void startServer(int port) {

       try {
           httpServer = HttpServer.create(new InetSocketAddress(port), 0);
           httpServer.setExecutor(Executors.newCachedThreadPool());

           httpServer.createContext("/", new HttpHandler() {
               @Override
               public void handle(HttpExchange httpExchange) throws IOException {
                   if("GET".equals(httpExchange.getRequestMethod())){
                       sendResponse(httpExchange,"Welcome to my server");
                   }
               }
           });

           //httpServer.createContext("/", new rootHandler());
           //httpServer.createContext("/index", new rootHandler());

           //httpServer.createContext("/messages", new messageHandler());
           serverTextView.setText("Server is running...");
           serverButton.setText("Stop Server");


       } catch (IOException e) {
           e.printStackTrace();
       }

    }

    public class rootHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if("GET".equals(httpExchange.getRequestMethod())){
                sendResponse(httpExchange,"Welcome to my server");
            }
        }


    }

    /*public class messageHandler implements HttpHandler{
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendResponse(httpExchange, jsonBody.toString());
            }


        }

    }*/

    private void stopServer() {
        if(httpServer != null){
            httpServer.stop(0);
            serverTextView.setText("Server is down");
            serverButton.setText("Start Server");
        }

    }


}