package com.ufcg.psoft.commerce.services.ativo;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoCotacaoRequestDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.exceptions.AtivoIndisponivelException;
import com.ufcg.psoft.commerce.exceptions.AtivoNaoExisteException;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.exceptions.CotacaoNaoPodeSerAtualizadaException;
import com.ufcg.psoft.commerce.exceptions.VariacaoMinimaDeCotacaoNaoAtingidaException;
import com.ufcg.psoft.commerce.loggers.Logger;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;

import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AtivoServiceImpl implements AtivoService {

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;
    @Autowired
    AtivoRepository ativoRepository;
    @Autowired
    ClienteService clienteService;
    @Autowired
    AdministradorService administradorService;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO dto) {
        administradorService.validarCodigoAcesso(dto.getCodigoAcesso());

        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        modelMapper.map(dto, ativo);
        ativoRepository.save(ativo);
        return modelMapper.map(ativo, AtivoResponseDTO.class);
    }

    @Override
    public AtivoResponseDTO criar(AtivoPostPutRequestDTO dto) {
        administradorService.validarCodigoAcesso(dto.getCodigoAcesso());

        List<TipoDeAtivo> tiposDeAtivo = tipoDeAtivoRepository.findAll();
        TipoDeAtivo tipo = tiposDeAtivo.stream()
                .filter(t -> t.getNomeTipo() == dto.getTipo())
                .toList().get(0);
        Ativo ativo = modelMapper.map(dto, Ativo.class);
        ativo.setInteressadosCotacao(new ArrayList<>());
        ativo.setInteressadosDisponibilidade(new ArrayList<>());
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
    public AtivoResponseDTO ativarOuDesativar(Long idAtivo, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);

        AtivoResponseDTO ativoAtualizado = ativarOuDesativarSwitch(idAtivo);

        if (ativoAtualizado.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL) {

            List<Long> interessados = recuperarInteressadosDisponibilidade(idAtivo);

            String mensagem = String.format("O ativo '%s' agora está disponível para compra!", ativoAtualizado.getNome());
            notificarInteressados(mensagem, interessados);
            limparInteressadosDisponibilidade(idAtivo);
        }

        return ativoAtualizado;
    }
    private AtivoResponseDTO ativarOuDesativarSwitch(Long id) {

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
        return ativos.stream().map(AtivoResponseDTO::new).toList();
    }

    @Override
    public List<AtivoResponseDTO> listarFiltrandoPorTipo(List<TipoAtivo> tiposParaFiltrar) {
        List<Ativo> ativos = ativoRepository.findAll();
        return ativos.stream()
                .filter(ativo -> !tiposParaFiltrar.contains(ativo.getTipo().getNomeTipo()))
                .map(AtivoResponseDTO::new).toList();
    }

    @Override
    public AtivoResponseDTO recuperar(Long id) {
        return ativoRepository.findById(id)
                .map(AtivoResponseDTO::new)
                .orElseThrow(AtivoNaoExisteException::new);
    }

    @Override
    public Ativo buscarPorId(Long id){
        return ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
    }

    @Override
    public void adicionarInteressadoCotacao(Long idAtivo, Long idCliente) {
        Ativo ativo = ativoRepository.findById(idAtivo).orElseThrow(AtivoNaoExisteException::new);
        ativo.addInteressadoCotacao(idCliente);
        ativoRepository.save(ativo);
    }

    @Override
    public void adicionarInteressadoDisponibilidade(Long idAtivo, Long idCliente) {
        Ativo ativo = ativoRepository.findById(idAtivo).orElseThrow(AtivoNaoExisteException::new);
        ativo.addInteressadoDisponibilidade(idCliente);
        ativoRepository.save(ativo);
    }

    @Override
    public List<Long> recuperarInteressadosCotacao(Long id) {
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        return ativo.getInteressadosCotacao();
    }

    @Override
    public List<Long> recuperarInteressadosDisponibilidade(Long id) {
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        return ativo.getInteressadosDisponibilidade();
    }

    @Override
    public void limparInteressadosDisponibilidade(Long id) {
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        ativo.getInteressadosDisponibilidade().clear();
        ativoRepository.save(ativo);
    }


    @Override
    public void validarDisponibilidade(Long idAtivo) {
        AtivoResponseDTO ativo = this.recuperar(idAtivo);
        if (ativo.getStatusDisponibilidade() != StatusDisponibilidade.DISPONIVEL) {
            throw new AtivoIndisponivelException();
        }
    }

    @Override
    public AtivoResponseDTO atualizarCotacao(Long id, AtivoCotacaoRequestDTO dto) {
        administradorService.validarCodigoAcesso(dto.getCodigoAcesso());

        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        if (ativo.getTipo().getNomeTipo() != TipoAtivo.ACAO && ativo.getTipo().getNomeTipo() != TipoAtivo.CRIPTOMOEDA) {
            throw new CotacaoNaoPodeSerAtualizadaException();
        }

        double cotacaoAtual = ativo.getValor();
        double novaCotacao = dto.getValor();
        double variacaoPercentual = Math.abs((cotacaoAtual - novaCotacao) / cotacaoAtual);

        if (variacaoPercentual < 0.01) {
            throw new VariacaoMinimaDeCotacaoNaoAtingidaException();
        }

        if (variacaoPercentual >= 0.10) {
            List<Long> interessados = recuperarInteressadosCotacao(id);
            String mensagem = String.format("Ativo %s variou de cotação em %.2f%%",
                    ativo.getNome(),
                    variacaoPercentual * 100);
            notificarInteressados(mensagem, interessados);
        }

        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, ativo);

        ativoRepository.save(ativo);

        return modelMapper.map(ativo, AtivoResponseDTO.class);
    }

    private void notificarInteressados(String mensagem, List<Long> interessados) {

        if (interessados == null || interessados.isEmpty()) {
            return;
        }

        for (Long idInteressado : interessados) {
            ClienteResponseDTO cliente = clienteService.recuperar(idInteressado);
            Logger.alertUser(cliente.getNome(), mensagem);
        }
    }
}