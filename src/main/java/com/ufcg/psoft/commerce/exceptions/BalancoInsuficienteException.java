package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BalancoInsuficienteException extends RuntimeException {
    public BalancoInsuficienteException() {
        super("Balanco insuficiente!");
    }
}
