package com.ufcg.psoft.commerce.exceptions;

public class AtivoNaoExisteException extends CommerceException {
    public AtivoNaoExisteException() {
        super("O ativo consultado nao existe!");
    }
}