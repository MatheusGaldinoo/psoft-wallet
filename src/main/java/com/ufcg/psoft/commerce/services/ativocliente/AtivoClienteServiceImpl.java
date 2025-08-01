package com.ufcg.psoft.commerce.services.ativocliente;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.exceptions.AtivoNaoExisteException;
import com.ufcg.psoft.commerce.exceptions.CotacaoNaoPodeSerAtualizadaException;
import com.ufcg.psoft.commerce.exceptions.ServicoNaoDisponivelParaPlanoException;
import com.ufcg.psoft.commerce.exceptions.VariacaoMinimaDeCotacaoNaoAtingidaException;
import com.ufcg.psoft.commerce.models.Ativo;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AtivoClienteServiceImpl implements AtivoClienteService {

    @Autowired
    AtivoService ativoService;

    @Autowired
    ClienteService clienteService;

    @Autowired
    AdministradorService administradorService;

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<AtivoResponseDTO> listarAtivosDisponiveis(Long id) {

        ClienteResponseDTO cliente = clienteService.recuperar(id);
        TipoPlano tipoPlano = cliente.getPlano();
        if (tipoPlano == TipoPlano.PREMIUM) {
            return ativoService.listarTodos();
        } else if (tipoPlano == TipoPlano.NORMAL) {
            List<TipoAtivo> tiposParaFiltrar = new ArrayList<>();
            tiposParaFiltrar.add(TipoAtivo.ACAO);
            tiposParaFiltrar.add(TipoAtivo.CRIPTOMOEDA);
            return ativoService.listarFiltrandoPorTipo(tiposParaFiltrar);
        } else {
            return null;
        }

    }

    @Override
    public List<AtivoResponseDTO> marcarInteresseAtivo(Long idCliente, Long idAtivo) throws ServicoNaoDisponivelParaPlanoException {

       ClienteResponseDTO cliente = clienteService.recuperar(idCliente);

       if (cliente.getPlano() == TipoPlano.NORMAL) {
           throw new ServicoNaoDisponivelParaPlanoException("Plano do cliente não permite marcar interesse!");
       }

       ativoService.adicionarInteressado(idAtivo, idCliente);

    }

    @Override
    public AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);

        return ativoService.alterar(id, ativoPostPutRequestDTO);
    }

    @Override
    public AtivoResponseDTO criar(AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso) {

        administradorService.validarCodigoAcesso(codigoAcesso);

        AtivoResponseDTO ativo = ativoService.criar(ativoPostPutRequestDTO);

        return ativo;

    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        Ativo ativo = ativoRepository.findById(id).orElseThrow(AtivoNaoExisteException::new);
        ativoRepository.delete(ativo);
    }

    @Override
    public AtivoResponseDTO atualizarCotacao(Long id, Double novaCotacao, String codigoAcesso) {

        AtivoResponseDTO ativo = ativoService.recuperar(id);

        if (ativo.getTipo().getNomeTipo() != TipoAtivo.ACAO && ativo.getTipo().getNomeTipo() != TipoAtivo.CRIPTOMOEDA) {
            throw new CotacaoNaoPodeSerAtualizadaException();
        }

        Double cotacaoAtual = ativo.getValor();
        double variacaoPercentual = Math.abs((novaCotacao - cotacaoAtual) / cotacaoAtual);

        if (variacaoPercentual < 0.01) {
            throw new VariacaoMinimaDeCotacaoNaoAtingidaException();
        }

        AtivoPostPutRequestDTO ativoModificado = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);
        ativoModificado.setValor(novaCotacao);

        return ativoService.alterar(id, ativoModificado);
    }

    @Override
    public void adicionarInteressado(Long idAtivo, Long idCliente)  {
        ativoService.adicionarInteressado(idAtivo, idCliente);
    }

    @Override
    public AtivoResponseDTO ativarOuDesativar(Long id, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        return ativoService.ativarOuDesativar(id);
    };

    @Override
    public void remover(Long id, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        ativoService.remover(id);
    }
}