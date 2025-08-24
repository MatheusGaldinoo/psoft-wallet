package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompraNaoEncontradaException extends RuntimeException {
    public CompraNaoEncontradaException() {
        super("Compra nao encontrada!");
    }
}
