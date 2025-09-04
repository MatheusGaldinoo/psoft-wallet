package com.ufcg.psoft.commerce.exceptions;

public class QuantidadeInsuficienteException extends RuntimeException {
    // TODO - Adicionar no ErrorHandler para retornar o erro correto
    public QuantidadeInsuficienteException() {
        super("Quantidade do ativo insuficiente para o resgate!");
    }
}
