package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CompraNaoPertenceAoClienteException extends RuntimeException {
    public CompraNaoPertenceAoClienteException() {
        super("Compra nao pertence a este cliente!");
    }
}
