package com.example.sampleserver;

public class constants {
    // Пароли ключей
    public static final char[] kspass = "abcdefg".toCharArray();
    public static final char[] ctpass = "aabbcc".toCharArray();
    public static final int PORT = 5055;
    public static final String fileName = "testFile.txt"; // Имя файла куда будут сохраняться логи и данные из запросов

    // Ответы на запросы
    public static final String goodRequest = "{ \n" + // 200
            "    \"StatusOnPhone\": \"Yes\", \n" +
            "}";
    public static final int goodRequest_int = 200;

    public static final String badRequest = "{ \n" + // 400
            "    \"StatusOnPhone\": \"No\", \n" +
            "}";
    public static final int badRequest_int = 400;

    public static final String badRequest_Token = "{ \n" + // 401
            "    \"StatusOnPhone\": \"Bad Token\", \n" +
            "}";
    public static final int badRequest_Token_int = 401;

    public static final String badRequest_Host = "{ \n" + // 401
            "    \"StatusOnPhone\": \"Bad Host\", \n" +
            "}";
    public static final int badRequest_Host_int = 401;

    public static final int ERROR = 404;



    // Login и Password для входа на сервер
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";
    public static final String BASIC_AUTH = "Basic YWRtaW46YWRtaW4="; // "Basic " + Base64(USERNAME:PASSWORD)

}
