package com.ufcg.psoft.commerce.controllers;


import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.exceptions.ServicoNaoDisponivelParaPlanoException;
import com.ufcg.psoft.commerce.services.ativocliente.AtivoClienteService;
import jakarta.validation.Valid;
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

    @PutMapping("/{idUser}/atualizar-ativo/{idAtivo}")
    public ResponseEntity<?> atualizarAtivo(
            @PathVariable Long idUser,
            @PathVariable Long idAtivo,
            @RequestParam String codigoAcesso,
            @RequestBody @Valid AtivoPostPutRequestDTO ativoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.alterar(idAtivo, ativoPostPutRequestDto, codigoAcesso));
    }

    @PatchMapping("/{idUser}/ativar-desativar/{idAtivo}")
    public ResponseEntity<?> ativarOuDesativarAtivo(
            @PathVariable Long idUser,
            @PathVariable Long idAtivo,
            @RequestParam String codigoAcesso) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.ativarOuDesativar(idAtivo, codigoAcesso));
    }

    @PatchMapping("/{idUser}/atualizar-cotacao/{idAtivo}")
    public ResponseEntity<?> atualizarCotacao(
            @PathVariable Long idUser,
            @PathVariable Long idAtivo,
            @RequestParam String codigoAcesso,
            @RequestBody AtivoPostPutRequestDTO ativoPostPutRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.atualizarCotacao(idAtivo, ativoPostPutRequestDTO, codigoAcesso));
    }

    @DeleteMapping("/{idUser}/excluir-ativo/{idAtivo}")
    public ResponseEntity<?> excluirAtivo(
            @PathVariable Long idUser,
            @RequestParam String codigoAcesso,
            @PathVariable Long idAtivo){
        ativoClienteService.remover(idAtivo, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

    @PatchMapping("/{idCliente}/marcar-interesse/{idAtivo}")
    public ResponseEntity<?> marcarInteresseAtivo(
            @PathVariable Long idCliente,
            @PathVariable Long idAtivo) throws ServicoNaoDisponivelParaPlanoException {

        ativoClienteService.adicionarInteressado(idCliente, idAtivo);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("");
    }

    @GetMapping("/{idUser}/ativos-disponiveis")
    public ResponseEntity<?> recuperarAtivo(
            @PathVariable Long idUser) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.listarAtivosDisponiveis(idUser));
    }
}