package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.exceptions.CompraNaoEncontradaException;
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

    // US09
    @PostMapping("/clientes/{idCliente}/solicitar")
    public ResponseEntity<CompraResponseDTO> solicitarCompra(
            @PathVariable Long idCliente,
            @RequestParam String codigoAcesso,
            @RequestParam Long idAtivo,
            @RequestParam double quantidade) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.solicitarCompra(idCliente, codigoAcesso, idAtivo, quantidade));
    }

    // US10
    @GetMapping("/clientes/{idCliente}/acompanhar-status")
    public ResponseEntity<List<CompraResponseDTO>> listarComprasDoCliente(@PathVariable Long idCliente) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.listarComprasDoCliente(idCliente));
    }

    // US11
    @PatchMapping("/compras/{idCompra}/aprovar")
    public ResponseEntity<CompraResponseDTO> aprovarCompra(@PathVariable Long idCompra, @RequestParam String codigoAcesso) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.aprovarCompra(idCompra, codigoAcesso));
    }

    // US11
    @PatchMapping("/compras/{idCompra}/recusar")
    public ResponseEntity<CompraResponseDTO> recusarCompra(@PathVariable Long idCompra, @RequestParam String codigoAcesso) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.recusarCompra(idCompra, codigoAcesso));
    }

    // US12
    @PatchMapping("/clientes/{idCliente}/finalizar/{idCompra}")
    public ResponseEntity<CompraResponseDTO> realizarCompra(@PathVariable Long idCliente, @PathVariable Long idCompra) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.executarCompra(idCliente, idCompra));
    }

    // US13
    @GetMapping("/clientes/{idCliente}/visualizar-carteira")
    public ResponseEntity<List<AtivoCarteiraResponseDTO>> visualizarCarteiraDoCliente(@PathVariable Long idCliente) {
        return ResponseEntity.status(HttpStatus.OK).body(carteiraService.visualizarCarteira(idCliente));
    }

    /*
    @GetMapping("/clientes/{idCliente}/compras/{idCompra}")
    public ResponseEntity<CompraResponseDTO> detalharCompra(@PathVariable Long idCliente, @PathVariable Long idCompra) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.detalharCompra(idCliente, idCompra));
    }

    @GetMapping("/compras")
    public ResponseEntity<List<CompraResponseDTO>> listarPorEstado(
            @RequestParam(required = false) String estado
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.listarPorEstado(estado));
    }
     */
}
