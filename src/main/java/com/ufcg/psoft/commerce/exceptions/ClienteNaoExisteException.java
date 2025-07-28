package com.ufcg.psoft.commerce.exceptions;

public class ClienteNaoExisteException extends CommerceException {
    public ClienteNaoExisteException() {
        super("O cliente consultado nao existe!");
    }
}
