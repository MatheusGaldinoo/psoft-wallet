package com.ufcg.psoft.commerce.controllers;

import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
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

    @PostMapping("/clientes/{idCliente}/solicitar")
    public ResponseEntity<CompraResponseDTO> solicitarCompra(@PathVariable Long idCliente, @Valid @RequestBody CompraPostPutRequestDTO dto) {
        // TODO - o que colocar para receber dado que preciso do idAtivo, clienteCodigoAcesso e quantidade? Por agora, está no DTO.
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.solicitarCompra(idCliente, dto));
    }

    @PatchMapping("/clientes/{idCliente}/finalizar/{idCompra}")
    public ResponseEntity<CompraResponseDTO> realizarCompra(@PathVariable Long idCliente, @PathVariable Long idCompra) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.executarCompra(idCliente, idCompra));
    }

    @GetMapping("/clientes/{idCliente}/listar")
    public ResponseEntity<List<CompraResponseDTO>> listarComprasDoCliente(@PathVariable Long idCliente) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.listarComprasDoCliente(idCliente));
    }

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

    @PatchMapping("/compras/{idCompra}/aprovar")
    public ResponseEntity<CompraResponseDTO> aprovarCompra(@PathVariable Long idCompra, @RequestParam String codigoAcesso) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.aprovarCompra(idCompra, codigoAcesso));
    }

    @PatchMapping("/compras/{idCompra}/recusar")
    public ResponseEntity<CompraResponseDTO> recusarCompra(@PathVariable Long idCompra, @RequestParam String codigoAcesso) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.recusarCompra(idCompra, codigoAcesso));
    }
}
