package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ServicoNaoDisponivelParaPlanoException extends CommerceException {

    public ServicoNaoDisponivelParaPlanoException(String s) {
        super(s);

    }
}
