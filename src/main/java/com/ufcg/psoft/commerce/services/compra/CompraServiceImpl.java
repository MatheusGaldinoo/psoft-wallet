package com.ufcg.psoft.commerce.services.compra;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.exceptions.*;
import com.ufcg.psoft.commerce.loggers.Logger;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import com.ufcg.psoft.commerce.repositories.CompraRepository;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.ativocliente.AtivoClienteService;
import com.ufcg.psoft.commerce.services.carteira.CarteiraService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {

    /* TODO - Compra tem muitos services.
    *  O que pode ajudar é:
    * (1) passar a lógica dos estados pra classe Compra; e
    * (2) criar um package de utils contendo uma classe de validação dos dados,
    * nem que exista uma classe de validação exclusiva de compra.
    */

    @Autowired
    AdministradorService administradorService;

    @Autowired
    ClienteService clienteService;

    @Autowired
    AtivoService ativoService;

    @Autowired
    AtivoClienteService ativoClienteService;

    @Autowired
    CarteiraService carteiraService;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CompraResponseDTO solicitarCompra(Long idCliente, String codigoAcesso, Long idAtivo, double quantidade) {
        clienteService.validarCodigoAcesso(idCliente, codigoAcesso);

        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        // 403 status code if forbidden
        ativoClienteService.validarPermissaoCompra(idCliente, ativo.getId());

        // 500 status code if unavailable
        ativoService.validarDisponibilidade(ativo.getId());


        double precoUnitario = ativo.getValor();
        double custoTotalCompra = precoUnitario * quantidade;

        // 422 status code if not enough credit
        carteiraService.validarBalancoSuficiente(idCliente, custoTotalCompra);

        Compra compra = Compra.builder()
                .idCliente(idCliente)
                .idAtivo(idAtivo)
                .quantidade(quantidade)
                .precoUnitario(precoUnitario)
                .valorTotal(custoTotalCompra)
                .dataSolicitacao(LocalDateTime.now())
                .build();

        compraRepository.save(compra);

        return modelMapper.map(compra, CompraResponseDTO.class);
    }

    @Transactional
    @Override
    public CompraResponseDTO executarCompra(Long idCliente, Long idCompra) {
        Compra compra = compraRepository.findById(idCompra).orElseThrow(CompraNaoEncontradaException::new);

        if (!compra.getIdCliente().equals(idCliente)) {
            throw new CompraNaoPertenceAoClienteException();
        }

        if (compra.getEstadoAtual() != EstadoCompra.DISPONIVEL) {
            throw new CompraNaoDisponivelException();
        }

        carteiraService.validarBalancoSuficiente(idCliente, compra.getValorTotal());
        carteiraService.aplicarCompra(idCliente, compra.getIdAtivo(), compra.getQuantidade(), compra.getValorTotal());

        compra.modificarEstadoCompra();

        compra.modificarEstadoCompra();

        compra.setDataFinalizacao(LocalDateTime.now());
        compraRepository.save(compra);

        return modelMapper.map(compra, CompraResponseDTO.class);
    }

    @Override
    public CompraResponseDTO aprovarCompra(Long idCompra, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);

        // 404 status code if Compra not found
        Compra compra = compraRepository.findById(idCompra).orElseThrow(CompraNaoEncontradaException::new);

        // 403 status code if Compra is not in SOLICITADO state
        if (compra.getEstadoAtual() != EstadoCompra.SOLICITADO) { throw new CompraNaoPendenteException(); }

        ClienteResponseDTO clienteDto = clienteService.recuperar(compra.getIdCliente());

        // 422 status code if credit is not enough to buy asset
        carteiraService.validarBalancoSuficiente(clienteDto.getId(), compra.getValorTotal());

        compra.modificarEstadoCompra();
        compra.setDataFinalizacao(LocalDateTime.now());

        compraRepository.save(compra);

        AtivoResponseDTO ativoDto = ativoService.recuperar(compra.getIdAtivo());

        Logger.alertUser(clienteDto.getNome(), String.format("Sua compra do ativo '%s' foi aprovada!", ativoDto.getNome()));

        return modelMapper.map(compra, CompraResponseDTO.class);
    }

    @Override
    public CompraResponseDTO recusarCompra(Long idCompra, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        Compra compra = compraRepository.findById(idCompra).orElseThrow(CompraNaoEncontradaException::new);
        compraRepository.deleteById(idCompra);
        ClienteResponseDTO clienteDto = clienteService.recuperar(compra.getIdCliente());
        AtivoResponseDTO ativoDto = ativoService.recuperar(compra.getIdAtivo());
        Logger.alertUser(clienteDto.getNome(), String.format("Sua compra do ativo '%s' foi recusada!", ativoDto.getNome()));
        return modelMapper.map(compra, CompraResponseDTO.class);
    }

    @Override
    public List<CompraResponseDTO> listarComprasDoCliente(Long idCliente) {
        List<Compra> compras = compraRepository.findByIdCliente(idCliente);
        return compras.stream().map(compra -> modelMapper.map(compra, CompraResponseDTO.class)).toList();
    }

}
