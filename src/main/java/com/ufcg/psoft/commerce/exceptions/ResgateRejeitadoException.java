package com.ufcg.psoft.commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResgateRejeitadoException extends RuntimeException {
  public ResgateRejeitadoException() {super("Resgate rejeitado!");
  }
}
