package com.ufcg.psoft.commerce.exceptions;

public class AtivoIndisponivelException extends CommerceException {
    public AtivoIndisponivelException() {
        super("Ativo nao disponivel!");
    }
}
