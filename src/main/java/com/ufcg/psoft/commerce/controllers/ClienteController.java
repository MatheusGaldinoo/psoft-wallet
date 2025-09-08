package com.ufcg.psoft.commerce.controllers;

import com.electronwill.nightconfig.core.conversion.Path;
import com.ufcg.psoft.commerce.dtos.CodigoAcessoDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoQueryDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import com.ufcg.psoft.commerce.services.transacao.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        value = "/clientes",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ClienteController {

    @Autowired
    ClienteService clienteService;

    @Autowired
    TransacaoService transacaoService;

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> recuperarCliente(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clienteService.recuperar(id));
    }

    @GetMapping("")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes(
            @RequestParam(required = false, defaultValue = "") String nome) {

        if (nome != null && !nome.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(clienteService.listarPorNome(nome));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clienteService.listar());
    }

    @PostMapping()
    public ResponseEntity<ClienteResponseDTO> criarCliente(
            @RequestBody @Valid ClientePostPutRequestDTO clientePostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clienteService.criar(clientePostPutRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid ClientePostPutRequestDTO clientePostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clienteService.alterar(id, clientePostPutRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCliente(
            @PathVariable Long id,
            @RequestBody @Valid CodigoAcessoDTO dto) {
        clienteService.remover(id, dto);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{id}/transacoes")
    public ResponseEntity<List<TransacaoResponseDTO>> getTransacoes(
            @PathVariable Long id,
            @RequestBody @Valid TransacaoQueryDTO dto ){
        // garante que a busca seja apenas do cliente
        dto.setClienteId(id);
        return ResponseEntity.ok(transacaoService.listarTransacoes(dto));
    }
}