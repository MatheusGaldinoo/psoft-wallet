package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.CodigoAcessoDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoCotacaoRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping(
        value = "/ativos",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AtivoController {

    @Autowired
    private AtivoService ativoService;

    @GetMapping()
    public ResponseEntity<List<AtivoResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(ativoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> recuperarAtivo(@PathVariable Long id) {
        return ResponseEntity.ok(ativoService.recuperar(id));
    }

    @PostMapping
    public ResponseEntity<AtivoResponseDTO> criarAtivo(
            @RequestBody @Valid AtivoPostPutRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ativoService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> alterarAtivo(
            @PathVariable Long id,
            @RequestBody @Valid AtivoPostPutRequestDTO dto) {
        return ResponseEntity
                .ok(ativoService.alterar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAtivo(
            @PathVariable Long id,
            @RequestBody @Valid CodigoAcessoDTO dto) {

        ativoService.remover(id, dto.getCodigoAcesso());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AtivoResponseDTO> ativarOuDesativar(
            @PathVariable Long id,
            @RequestBody @Valid CodigoAcessoDTO dto) {
        return ResponseEntity.ok(ativoService.ativarOuDesativar(id, dto.getCodigoAcesso()));
    }

    @PatchMapping("/{id}/cotacao")
    public ResponseEntity<AtivoResponseDTO> atualizarCotacao(
            @PathVariable Long id,
            @RequestBody @Valid AtivoCotacaoRequestDTO dto) {
        return ResponseEntity.ok(ativoService.atualizarCotacao(id, dto));
    }
}