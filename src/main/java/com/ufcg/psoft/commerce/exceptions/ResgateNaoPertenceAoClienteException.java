package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResgateNaoPertenceAoClienteException extends RuntimeException {
    public ResgateNaoPertenceAoClienteException() {
        super("Resgate nao pertence ao cliente!");
    }
}
