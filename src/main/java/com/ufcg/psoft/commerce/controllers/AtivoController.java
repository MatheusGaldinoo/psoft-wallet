package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/ativos",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AtivoController {

    @Autowired
    private AtivoService ativoService;

    @GetMapping("")
    public ResponseEntity<?> listarAtivos() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarAtivo(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoService.recuperar(id));
    }

    @PostMapping()
    public ResponseEntity<?> criarAtivo(
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ativoService.criar(ativoPostPutRequestDto, codigoAcesso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAtivo(
            @PathVariable Long id,
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoService.alterar(id, ativoPostPutRequestDto, codigoAcesso));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> ativarOuDesativarAtivo(
            @PathVariable Long id,
            @RequestParam String codigoAcesso) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoService.ativarOuDesativar(id, codigoAcesso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAtivo(
            @PathVariable Long id,
            @RequestParam String codigoAcesso) {
        ativoService.remover(id, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }
}