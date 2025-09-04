package com.ufcg.psoft.commerce.exceptions;

public class ResgateNaoEncontradoException extends RuntimeException {
    public ResgateNaoEncontradoException() {
        super("Resgate nao encontrado!");
    }
}
