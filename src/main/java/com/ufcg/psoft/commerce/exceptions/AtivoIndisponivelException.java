package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AtivoIndisponivelException extends RuntimeException {
    public AtivoIndisponivelException() {
        super("Ativo nao disponivel!");
    }
}
