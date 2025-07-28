package com.ufcg.psoft.commerce.services.ativo;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.exceptions.AtivoNaoExisteException;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.models.Ativo;
import com.ufcg.psoft.commerce.models.Cliente;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;

import main.java.com.ufcg.psoft.commerce.exceptions.CotacaoNaoPodeSerAtualizadaException;
import main.java.com.ufcg.psoft.commerce.exceptions.VariacaoMinimaDeCotacaoNaoAtingidaException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtivoServiceImpl implements AtivoService {

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;
    @Autowired
    AtivoRepository ativoRepository;
    @Autowired
    AdministradorService administradorService;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        modelMapper.map(ativoPostPutRequestDTO, ativo);
        return modelMapper.map(ativo, AtivoResponseDTO.class);
    }

    @Override
    public AtivoResponseDTO criar(AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso) {

        administradorService.validarCodigoAcesso(codigoAcesso);
        List<TipoDeAtivo> tiposDeAtivo = tipoDeAtivoRepository.findAll();
        TipoDeAtivo tipo = tiposDeAtivo.stream()
                .filter((t) -> t.getNomeTipo() == ativoPostPutRequestDTO.getTipo())
                .collect(Collectors.toList()).get(0);
        Ativo ativo = modelMapper.map(ativoPostPutRequestDTO, Ativo.class);
        ativo.setInteressados(new ArrayList<>());
        ativo.setTipo(tipo);
        ativoRepository.save(ativo);
        return modelMapper.map(ativo, AtivoResponseDTO.class);

    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        ativoRepository.delete(ativo);
    }

    @Override
    public AtivoResponseDTO ativarOuDesativar(Long id, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        if (ativo.getStatusDisponibilidade() == StatusDisponibilidade.INDISPONIVEL) {
            ativo.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
        } else {
            ativo.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);
        }
        return modelMapper.map(ativo, AtivoResponseDTO.class);
    }

    @Override
    public List<AtivoResponseDTO> listarTodos() {
        List<Ativo> ativos = ativoRepository.findAll();
        return ativos.stream().map(AtivoResponseDTO::new).collect(Collectors.toList());
    }

    @Override
    public List<AtivoResponseDTO> listarFiltrandoPorTipo(List<TipoAtivo> tiposParaFiltrar) {
        List<Ativo> ativos = ativoRepository.findByStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
        return ativos.stream()
                .filter((ativo) -> !tiposParaFiltrar.contains(ativo.getTipo().getNomeTipo()))
                .map(AtivoResponseDTO::new).collect(Collectors.toList());
    }

    private List<AtivoResponseDTO> listarPorNome(String nome) {
        List<Ativo> ativos = ativoRepository.findByNomeContaining(nome);
        return ativos.stream().map(AtivoResponseDTO::new).collect(Collectors.toList());
    }

    @Override
    public AtivoResponseDTO recuperar(Long id) {
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        return new AtivoResponseDTO(ativo);
    }

    @Override
    public AtivoResponseDTO atualizarCotacao(Long id, Double novaCotacao, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);

        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);

        if (ativo.getTipo().getNomeTipo() != TipoAtivo.ACAO && ativo.getTipo().getNomeTipo() != TipoAtivo.CRIPTOMOEDA) {
            throw new CotacaoNaoPodeSerAtualizadaException();
        }

        Double cotacaoAtual = ativo.getValor();
        double variacaoPercentual = Math.abs((novaCotacao - cotacaoAtual) / cotacaoAtual);

        if (variacaoPercentual < 0.01) {
            throw new VariacaoMinimaDeCotacaoNaoAtingidaException();
        }

        ativo.setValor(novaCotacao);
        ativoRepository.save(ativo);

        return modelMapper.map(ativo, AtivoResponseDTO.class);
    }
}