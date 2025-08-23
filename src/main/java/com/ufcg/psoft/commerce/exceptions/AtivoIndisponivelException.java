package com.ufcg.psoft.commerce.exceptions;

public class AtivoIndisponivelException extends RuntimeException {
    public AtivoIndisponivelException() {
        super("Ativo nao disponivel!");
    }
}
