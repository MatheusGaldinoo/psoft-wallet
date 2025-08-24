package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CompraNaoDisponivelException extends RuntimeException {
    public CompraNaoDisponivelException() { super("Compra ainda nao aprovada pelo administrador!"); }
}
