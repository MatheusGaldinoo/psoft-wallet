package com.ufcg.psoft.commerce.exceptions;

public class CompraNaoPertenceAoClienteException extends RuntimeException {
    public CompraNaoPertenceAoClienteException() {
        super("Compra nao pertence a este cliente!");
    }
}
