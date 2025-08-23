package com.ufcg.psoft.commerce.exceptions;

public class CompraNaoDisponivelException extends RuntimeException {
    public CompraNaoDisponivelException() { super("Compra ainda nao aprovada pelo administrador!"); }
}
