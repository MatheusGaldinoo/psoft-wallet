package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.transacao.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.transacao.CompraResponseDTO;
import com.ufcg.psoft.commerce.services.compra.TransacaoService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/usuario",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class TransacaoController {

    @Autowired
    TransacaoService transacaoService;

    @Autowired
    ModelMapper modelMapper;

    @PostMapping("/{idCliente}/comprar-ativo/{idAtivo}")
    public ResponseEntity<CompraResponseDTO> comprarAtivo(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo,
            @RequestBody @Valid CompraPostPutRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transacaoService.realizarCompra(request));
    };
}
