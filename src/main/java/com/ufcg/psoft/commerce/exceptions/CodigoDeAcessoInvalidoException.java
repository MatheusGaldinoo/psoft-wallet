package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CodigoDeAcessoInvalidoException extends CommerceException {
    public CodigoDeAcessoInvalidoException() {
        super("Codigo de acesso invalido!");
    }
}