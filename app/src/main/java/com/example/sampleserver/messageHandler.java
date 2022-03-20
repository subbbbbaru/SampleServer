package com.example.sampleserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class messageHandler implements HttpHandler {

    String ERROR = "";
    String writedata = "";


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        // Проверка, что пришел POST запрос
        if ("POST".equals(httpExchange.getRequestMethod())) {

            InetSocketAddress inetSocketAddress = httpExchange.getRemoteAddress();
            String HOSTNAME = inetSocketAddress.getHostName();

            Headers headers = httpExchange.getRequestHeaders();

            String token_file = ReadData.readData(HOSTNAME + ".txt");

            if (token_file.isEmpty()){
                sendResponse(constants.badRequest_Host_int, httpExchange, constants.badRequest_Host);
            } else if(headers.containsKey("Token") && headers.get("Token").get(0).equals(token_file)){

                ReadData.deleteFile(HOSTNAME + ".txt");
                // Получение данных из запроса
                InputStream inputStream = httpExchange.getRequestBody();
                String requestBody = streamToString(inputStream);
                inputStream.close();
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
                                "\t\t\t" + "INN : " + INN + "\n" +
                                "\t\t\t" + "Phone : " + Phone + "\n" +
                                "\t\t\t" + "Text : " + Text + "\n";
                        WriteData.writeData(writedata);

                        // Отправка SMS
                        //Telephony.SMS(Phone, Text);

                        sendResponse(constants.goodRequest_int, httpExchange, constants.goodRequest.toString());
                    } else {
                        // Отправка ответа, если данных повреждены

                        sendResponse(constants.badRequest_int, httpExchange, constants.badRequest.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendResponse(httpExchange, e.getMessage());

                    // Запись сообщения об ошибке с распознаванием JSON
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
                    String date = dateFormat.format(Calendar.getInstance().getTime());
                    ERROR += "\n\n" + date + " : ERROR(messageHandler)  JSONException!!! " + e.getMessage() + "\n\n";
                    WriteData.writeData(ERROR);
                }
            } else {
                sendResponse(constants.badRequest_Token_int, httpExchange, constants.badRequest_Token.toString());
            }
        }
    }

    // Отправка ответа с сервера(телефона) на запрос
    public void sendResponse(int code, HttpExchange httpsExchange, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpsExchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = httpsExchange.getResponseBody();
        os.write(bytes);
        os.close();



        /*byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        os.close();*/
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
    public void sendResponse(HttpExchange httpsExchange, String responseText) throws IOException {
        sendResponse(constants.ERROR, httpsExchange, responseText);
    }
}
