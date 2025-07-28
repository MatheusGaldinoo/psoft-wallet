package com.ufcg.psoft.commerce.exceptions;

public class CommerceException extends RuntimeException {
    public CommerceException() {
        super("Erro inesperado no Pits A!");
    }

    public CommerceException(String message) {
        super(message);
    }
}
