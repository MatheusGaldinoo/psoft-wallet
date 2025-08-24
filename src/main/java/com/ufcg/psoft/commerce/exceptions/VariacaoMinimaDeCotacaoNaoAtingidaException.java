package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VariacaoMinimaDeCotacaoNaoAtingidaException extends CommerceException {
    public VariacaoMinimaDeCotacaoNaoAtingidaException() {
        super("A variação da cotação deve ser de no mínimo 1%.");
    }
}