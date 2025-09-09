package com.ufcg.psoft.commerce.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;

@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    private CustomErrorType defaultCustomErrorTypeConstruct(String message) {
        return CustomErrorType.builder()
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .message(message)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        CustomErrorType customErrorType = defaultCustomErrorTypeConstruct(
                "Erros de validacao encontrados"
        );
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            customErrorType.getErrors().add(fieldError.getDefaultMessage());
        }
        return customErrorType;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onConstraintViolation(ConstraintViolationException e) {
        CustomErrorType customErrorType = defaultCustomErrorTypeConstruct(
                "Erros de validacao encontrados"
        );
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            customErrorType.getErrors().add(violation.getMessage());
        }
        return customErrorType;
    }

    @ExceptionHandler(CommerceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onCommerceException(CommerceException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(ServicoNaoDisponivelParaPlanoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public CustomErrorType onServicoNaoDisponivelParaPlanoException(ServicoNaoDisponivelParaPlanoException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(CompraNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onCompraNaoEncontrada(CompraNaoEncontradaException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(CompraNaoPendenteException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public CustomErrorType onCompraNaoPendente(CompraNaoPendenteException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(CodigoDeAcessoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onCodigoDeAcessoInvalido(CodigoDeAcessoInvalidoException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(QuantidadeInsuficienteException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public CustomErrorType onQuantidadeInsuficienteException(QuantidadeInsuficienteException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(ResgateNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onResgateNaoEncontradoException(ResgateNaoEncontradoException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(ResgateNaoPendenteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public CustomErrorType onResgateNaoPendenteException(ResgateNaoPendenteException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }

    @ExceptionHandler(ResgateRejeitadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public CustomErrorType onResgateRejeitadoException(ResgateRejeitadoException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }


    @ExceptionHandler(ClienteNaoExisteException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onClienteNaoExisteException(ClienteNaoExisteException e) {
        return defaultCustomErrorTypeConstruct(e.getMessage());
    }
//
//    @ExceptionHandler(AtivoNaoExisteException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    @ResponseBody
//    public CustomErrorType onAtivoNaoExisteException(AtivoNaoExisteException e) {
//        return defaultCustomErrorTypeConstruct(e.getMessage());
//    }
}