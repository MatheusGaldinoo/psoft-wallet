package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.dtos.AtualizarStatusTransacaoDTO;
import com.ufcg.psoft.commerce.services.carteira.CarteiraService;
import com.ufcg.psoft.commerce.services.compra.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompraController {

    @Autowired
    CarteiraService carteiraService;

    @Autowired
    CompraService compraService;

    @Autowired
    ModelMapper modelMapper;

    // US09 - Cliente solicita compra de ativo
    @PostMapping("/clientes/{idCliente}/compras")
    public ResponseEntity<CompraResponseDTO> solicitarCompra(@PathVariable Long idCliente, @Valid @RequestBody CompraPostPutRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.solicitarCompra(idCliente, dto));
    }

    // US10 - Cliente acompanha status de cada compra realizada
    @GetMapping("/clientes/{idCliente}/compras")
    public ResponseEntity<List<CompraResponseDTO>> listarComprasDoCliente(@PathVariable Long idCliente) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.listarComprasDoCliente(idCliente));
    }

    // US11 - Administrador confirma ou não a solicitação de uma compra
    @PatchMapping("/compras/{idCompra}")
    public ResponseEntity<CompraResponseDTO> atualizarStatusCompra(
            @PathVariable Long idCompra,
            @Valid @RequestBody AtualizarStatusTransacaoDTO dto) {
        return ResponseEntity.ok(compraService.atualizarStatusCompra(idCompra, dto));
    }

    // US12 - Cliente confirma compra disponível
    @PatchMapping("/clientes/{idCliente}/{idCompra}")
    public ResponseEntity<CompraResponseDTO> realizarCompra(@PathVariable Long idCliente, @PathVariable Long idCompra) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.executarCompra(idCliente, idCompra));
    }

    // US13 - Cliente visualiza carteira de investimentos
    @GetMapping("/clientes/{idCliente}/carteira")
    public ResponseEntity<List<AtivoCarteiraResponseDTO>> visualizarCarteiraDoCliente(@PathVariable Long idCliente) {
        return ResponseEntity.status(HttpStatus.OK).body(carteiraService.visualizarCarteira(idCliente));
    }

}
