package com.ufcg.psoft.commerce.loggers;

public class Logger {

    public static void alertUser(String userName, String message) {

        String alertMessage = String.format("User: %s\nAlerta: %s", userName, message);
        System.out.println(alertMessage);

    }

}
