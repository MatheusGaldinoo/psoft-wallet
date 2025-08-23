package com.ufcg.psoft.commerce.exceptions;

public class BalancoInsuficienteException extends RuntimeException {
    public BalancoInsuficienteException() {
        super("Balanco insuficiente!");
    }
}
