package com.ufcg.psoft.commerce.exceptions;

public class CompraNaoPendenteException extends RuntimeException {
  public CompraNaoPendenteException() {
    super("Compra nao pendente!");
  }
}
