package com.ufcg.psoft.commerce.exceptions;

public class CompraNaoEncontradaException extends RuntimeException {
    public CompraNaoEncontradaException() {
        super("Compra nao encontrada!");
    }
}
