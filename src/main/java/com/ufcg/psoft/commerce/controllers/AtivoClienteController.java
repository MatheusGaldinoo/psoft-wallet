package com.ufcg.psoft.commerce.controllers;


import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.services.ativocliente.AtivoClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/clientes",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AtivoClienteController {

    @Autowired
    private AtivoClienteService ativoClienteService;

    @PostMapping("/{idUser}/criar-ativo")
    public ResponseEntity<?> criarAtivo(
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ativoClienteService.criar(ativoPostPutRequestDto, codigoAcesso));
    }

    @PutMapping("/{id}/atualizar-ativo")
    public ResponseEntity<?> atualizarAtivo(
            @PathVariable Long id,
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.alterar(id, ativoPostPutRequestDto, codigoAcesso));
    }

    @GetMapping("/{id}/ativos-disponiveis")
    public ResponseEntity<?> recuperarAtivo(
            @PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.listarAtivosDisponiveis(id));
    }

    @GetMapping("/{idCliente}/marcar-interesse/{idAtivo}")
    public ResponseEntity<?> recuperarAtivo(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.marcarInteresseAtivo(idCliente, idAtivo));
    }

}