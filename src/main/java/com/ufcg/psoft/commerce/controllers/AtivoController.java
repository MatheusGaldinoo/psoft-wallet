package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        List<AtivoResponseDTO> ativos = ativoService.listarTodos();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> recuperarAtivo(@PathVariable Long id) {
        return ResponseEntity.ok(ativoService.recuperar(id));
    }

}