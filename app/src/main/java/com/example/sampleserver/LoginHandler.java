package com.example.sampleserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class LoginHandler implements HttpHandler {


    private static String HOSTNAME = "";


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        // Проверка, что пришел POST запрос
        if ("POST".equals(httpExchange.getRequestMethod())) {

            Headers input_Headers = httpExchange.getRequestHeaders();
            List<String> auth_header = input_Headers.get("Authorization");

            InetSocketAddress h = httpExchange.getRemoteAddress();
            HOSTNAME = h.getHostName();

            if (auth_header.get(0).equals(constants.BASIC_AUTH)) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    byte[] decode = Base64.getDecoder().decode(constants.BASIC_AUTH.substring(6));

                    String decode_string = new String(decode);
                    String LOGIN = decode_string.substring(0, constants.USERNAME.length());
                    String password = decode_string.substring(constants.USERNAME.length() + 1);


                    if(LOGIN.equals(constants.USERNAME) && password.equals(constants.PASSWORD)){
                        sendResponse_Headers(httpExchange, Token.getJWTToken(constants.USERNAME, HOSTNAME));
                    }
                }
            }

            httpExchange.close();
        }
    }

    public void sendResponse_Headers(HttpExchange httpsExchange, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        httpsExchange.getResponseHeaders().add("Token", responseText);
        httpsExchange.sendResponseHeaders(200, bytes.length);
    }
}
