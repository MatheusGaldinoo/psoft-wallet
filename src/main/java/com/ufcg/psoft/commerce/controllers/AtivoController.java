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

}