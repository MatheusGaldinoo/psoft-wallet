package com.ufcg.psoft.commerce.controllers;


import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.exceptions.ServicoNaoDisponivelParaPlanoException;
import com.ufcg.psoft.commerce.services.ativocliente.AtivoClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        value = "/usuarios",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AtivoClienteController {

    @Autowired
    private AtivoClienteService ativoClienteService;

    //US06 e US07 - Cliente marcar interesse em ativo disponível ou indisponível
    @PatchMapping("/{idCliente}/ativos/{idAtivo}/interesse")
    public ResponseEntity<Void> marcarInteresse(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo) throws ServicoNaoDisponivelParaPlanoException {

        ativoClienteService.adicionarInteressado(idCliente, idAtivo);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    //US05 - Cliente visualizar ativos disponpiveis para seu plano
    @GetMapping("/{idCliente}/ativos")
    public ResponseEntity<List<AtivoResponseDTO>> listarAtivosDisponiveis(
            @PathVariable Long idCliente) {
        return ResponseEntity.ok(ativoClienteService.listarAtivosDisponiveis(idCliente));
    }

    //US08 - Cliente visualizar ativo
    @GetMapping("/{idCliente}/ativos/{idAtivo}")
    public ResponseEntity<AtivoResponseDTO> visualizarAtivo(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo) {
        return ResponseEntity.ok(ativoClienteService.visualizarAtivo(idCliente, idAtivo));
    }

}