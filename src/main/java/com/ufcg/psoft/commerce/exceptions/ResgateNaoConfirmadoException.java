package com.ufcg.psoft.commerce.exceptions;

public class ResgateNaoConfirmadoException extends RuntimeException {
    public ResgateNaoConfirmadoException() {
        super("Resgate nao foi julgado pelo administrador!");
    }
}
