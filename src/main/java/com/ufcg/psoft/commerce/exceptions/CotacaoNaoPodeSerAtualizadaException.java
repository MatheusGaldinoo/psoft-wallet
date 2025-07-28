package com.ufcg.psoft.commerce.exceptions;

public class CotacaoNaoPodeSerAtualizadaException extends CommerceException {
    public CotacaoNaoPodeSerAtualizadaException() {
        super("Somente ativos do tipo Ação ou Criptomoeda podem ter a cotação atualizada!");
    }
}