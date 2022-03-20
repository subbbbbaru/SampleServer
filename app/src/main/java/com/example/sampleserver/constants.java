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
    public static final String badRequest = "{ \n" + // 400
            "    \"StatusOnPhone\": \"No\", \n" +
            "}";
    public static final String badRequest_Token = "{ \n" + // 402
            "    \"StatusOnPhone\": \"Bad Token\", \n" +
            "}";
    public static final String badRequest_Host = "{ \n" + // 402
            "    \"StatusOnPhone\": \"Bad Host\", \n" +
            "}";



    // Login и Password для входа на сервер
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";
    public static final String BASIC_AUTH = "Basic YWRtaW46YWRtaW4="; // "Basic " + Base64(USERNAME:PASSWORD)

}
