package com.ufcg.psoft.commerce.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class QuantidadeInsuficienteException extends CommerceException {
    public QuantidadeInsuficienteException() {
        super("Quantidade do ativo insuficiente para o resgate!");
    }
}
