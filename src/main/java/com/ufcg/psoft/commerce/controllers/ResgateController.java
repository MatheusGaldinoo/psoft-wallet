package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import com.ufcg.psoft.commerce.dtos.AtualizarStatusTransacaoDTO;
import com.ufcg.psoft.commerce.services.resgate.ResgateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResgateController {

    @Autowired
    ResgateService resgateService;

    // US14 e US15 - Solicitar resgate e calcular imposto do resgate
    @PostMapping("clientes/{idCliente}/resgate")
    public ResponseEntity<ResgateResponseDTO> solicitarResgate(
            @PathVariable Long idCliente,
            @Valid @RequestBody ResgatePostPutRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resgateService.solicitarResgate(idCliente, dto));
    }

    // US16 - Cliente consulta status de um resgate específico
    @GetMapping("/resgates/{idResgate}")
    public ResponseEntity<ResgateResponseDTO> consultarResgate(
            @PathVariable Long idResgate) {
        return ResponseEntity.ok(resgateService.consultarResgate(idResgate));
    }

    // US16 / US18 - Cliente lista seus resgates (com filtros opcionais)
    @GetMapping("/clientes/{idCliente}/resgates")
    public ResponseEntity<List<ResgateResponseDTO>> listarResgatesDoCliente(
            @PathVariable Long idCliente,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String periodoInicio,
            @RequestParam(required = false) String periodoFim) {
        return ResponseEntity.ok(
                resgateService.listarResgatesDoCliente(idCliente, status, periodoInicio, periodoFim)
        );
    }

    //US16 - Executa resgate confirmado
    @PostMapping("/resgates/{idResgate}/executar")
    public ResponseEntity<ResgateResponseDTO> executarResgate(
            @PathVariable Long idResgate,
            @RequestParam Long idCliente
    ) {
        return ResponseEntity.ok(resgateService.executarResgate(idCliente, idResgate));
    }

    // US17 - Admin atualiza status de um resgate (APROVADO e RECUSADO)
    @PatchMapping("/resgates/{idResgate}")
    public ResponseEntity<ResgateResponseDTO> atualizarStatusResgate(
            @PathVariable Long idResgate,
            @Valid @RequestBody AtualizarStatusTransacaoDTO dto) {
        return ResponseEntity.ok(resgateService.atualizarStatusResgate(idResgate, dto));
    }
}
