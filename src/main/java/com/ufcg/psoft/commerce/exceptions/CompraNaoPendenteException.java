package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CompraNaoPendenteException extends RuntimeException {
  public CompraNaoPendenteException() {
    super("Compra nao pendente!");
  }
}
