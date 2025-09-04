package com.ufcg.psoft.commerce.exceptions;

public class ResgateNaoPendenteException extends RuntimeException {
    public ResgateNaoPendenteException() {
        super("Resgate nao esta pendente!");
    }
}
