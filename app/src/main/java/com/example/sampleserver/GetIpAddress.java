package com.example.sampleserver;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

public class GetIpAddress {

    // Получение локального IP адреса и порта
    public static String getIpAddress() {
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
            WriteData.writeData(ERROR);
        }
        return ip;
    }
}
