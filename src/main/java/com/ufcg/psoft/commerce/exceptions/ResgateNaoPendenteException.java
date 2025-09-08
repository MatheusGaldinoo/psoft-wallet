package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResgateNaoPendenteException extends RuntimeException {
    public ResgateNaoPendenteException() {
        super("Resgate nao esta pendente!");
    }
}
