package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.transacao.TransacaoQueryDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.transacao.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(
        value = "/transacoes",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private AdministradorService administradorService;

    // US19 - Admin lista todos as operações do sistema (com filtros)
    @GetMapping("")
    public ResponseEntity<List<TransacaoResponseDTO>> listarTransacoes(
            @Valid @ModelAttribute TransacaoQueryDTO queryDTO
    ) {

        administradorService.validarCodigoAcesso(queryDTO.getCodigoAcesso());

        return ResponseEntity.ok(
                transacaoService.listarTransacoes(queryDTO)
        );
    }


}
