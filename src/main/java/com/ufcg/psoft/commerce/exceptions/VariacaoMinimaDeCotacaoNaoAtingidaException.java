package com.ufcg.psoft.commerce.exceptions;

public class VariacaoMinimaDeCotacaoNaoAtingidaException extends CommerceException {
    public VariacaoMinimaDeCotacaoNaoAtingidaException() {
        super("A variação da cotação deve ser de no mínimo 1%.");
    }
}