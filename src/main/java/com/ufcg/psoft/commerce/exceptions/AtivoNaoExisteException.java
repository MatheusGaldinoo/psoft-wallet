package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AtivoNaoExisteException extends CommerceException {
    public AtivoNaoExisteException() {
        super("O ativo consultado nao existe!");
    }
}