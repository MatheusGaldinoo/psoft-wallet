package com.ufcg.psoft.commerce.loggers;

public class Logger {

    private Logger(){
        throw new UnsupportedOperationException("Classe utilitária, não deve ser instanciada.");
    }

    public static void alertUser(String userName, String message) {

        String alertMessage = String.format("User: %s%nAlerta: %s", userName, message);
        System.out.println(alertMessage);

    }

}
