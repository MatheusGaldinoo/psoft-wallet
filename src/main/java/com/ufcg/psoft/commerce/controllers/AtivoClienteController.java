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
        value = "/usuario",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AtivoClienteController {

    @Autowired
    private AtivoClienteService ativoClienteService;

    @PostMapping("/{idUser}/criar-ativo")
    public ResponseEntity<AtivoResponseDTO> criarAtivo(
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ativoClienteService.criar(ativoPostPutRequestDto, codigoAcesso));
    }

    @PutMapping("/{idUser}/atualizar-ativo/{idAtivo}")
    public ResponseEntity<AtivoResponseDTO> atualizarAtivo(
            @PathVariable Long idAtivo,
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.alterar(idAtivo, ativoPostPutRequestDto, codigoAcesso));
    }

    @PatchMapping("/{idUser}/ativar-desativar/{idAtivo}")
    public ResponseEntity<AtivoResponseDTO> ativarOuDesativarAtivo(
            @PathVariable Long idAtivo,
            @RequestParam String codigoAcesso) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.ativarOuDesativar(idAtivo, codigoAcesso));
    }

    @PatchMapping("/{idUser}/atualizar-cotacao/{idAtivo}")
    public ResponseEntity<AtivoResponseDTO> atualizarCotacao(
            @PathVariable Long idAtivo,
            @RequestParam String codigoAcesso,
            @RequestBody AtivoPostPutRequestDTO ativoPostPutRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.atualizarCotacao(idAtivo, ativoPostPutRequestDTO, codigoAcesso));
    }

    @DeleteMapping("/{idUser}/excluir-ativo/{idAtivo}")
    public ResponseEntity<Void> excluirAtivo(
            @RequestParam String codigoAcesso,
            @PathVariable Long idAtivo){
        ativoClienteService.remover(idAtivo, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PatchMapping("/{idCliente}/marcar-interesse/{idAtivo}")
    public ResponseEntity<Void> marcarInteresseAtivo(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo) throws ServicoNaoDisponivelParaPlanoException {

        ativoClienteService.adicionarInteressado(idCliente, idAtivo);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{idUser}/ativos-disponiveis")
    public ResponseEntity<List<AtivoResponseDTO>> recuperarAtivo(
            @PathVariable Long idUser) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.listarAtivosDisponiveis(idUser));
    }

    @GetMapping("/{idCliente}/visualizar-ativo/{idAtivo}")
    public ResponseEntity<AtivoResponseDTO> visualizarAtivo(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.visualizarAtivo(idCliente, idAtivo));
    }
    // TODO - Visualizar um ativo fora do meu plano fala do 'marcar-interesse'?

}