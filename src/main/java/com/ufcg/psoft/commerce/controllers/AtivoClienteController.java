package com.ufcg.psoft.commerce.controllers;


import com.ufcg.psoft.commerce.services.ativocliente.AtivoClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/clientes",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AtivoClienteController {

    @Autowired
    private AtivoClienteService ativoClienteService;

    @GetMapping("/{id}/ativos-disponiveis")
    public ResponseEntity<?> recuperarAtivo(
            @PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ativoClienteService.listarAtivosDisponiveis(id));
    }

}