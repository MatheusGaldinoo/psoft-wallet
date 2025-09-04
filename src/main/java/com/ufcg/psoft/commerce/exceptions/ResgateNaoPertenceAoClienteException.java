package com.ufcg.psoft.commerce.exceptions;

public class ResgateNaoPertenceAoClienteException extends RuntimeException {
    public ResgateNaoPertenceAoClienteException() {
        super("Resgate nao pertence ao cliente!");
    }
}
