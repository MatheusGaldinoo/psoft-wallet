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
import com.ufcg.psoft.commerce.loggers.Logger;
import com.ufcg.psoft.commerce.models.Ativo;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
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
    AtivoRepository ativoRepository;

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
    public void adicionarInteressado(Long idCliente, Long idAtivo) throws ServicoNaoDisponivelParaPlanoException {

       ClienteResponseDTO cliente = clienteService.recuperar(idCliente);
       AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

       if (ativo.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL) {
           if (cliente.getPlano() == TipoPlano.NORMAL) {
               throw new ServicoNaoDisponivelParaPlanoException("Plano do cliente nao permite marcar interesse!");
           }
           ativoService.adicionarInteressadoCotacao(idAtivo, idCliente);
       } else {
           ativoService.adicionarInteressadoDisponibilidade(idAtivo, idCliente);
       }
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
    public AtivoResponseDTO atualizarCotacao(Long idAtivo, AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso) {

        administradorService.validarCodigoAcesso(codigoAcesso);

        Ativo ativo = ativoRepository.findById(idAtivo).orElseThrow(AtivoNaoExisteException::new);
        if (ativo.getTipo().getNomeTipo() != TipoAtivo.ACAO && ativo.getTipo().getNomeTipo() != TipoAtivo.CRIPTOMOEDA) {
            throw new CotacaoNaoPodeSerAtualizadaException();
        }

        Double cotacaoAtual = ativo.getValor();
        Double novaCotacao = ativoPostPutRequestDTO.getValor();
        Double variacaoPercentual = Math.abs((cotacaoAtual - novaCotacao) / cotacaoAtual);

        if (variacaoPercentual < 0.01) {
            throw new VariacaoMinimaDeCotacaoNaoAtingidaException();
        }

        if (variacaoPercentual >= 0.10) {
            String mensagem = String.format("Ativo %s variou de cotação em %.2f%%",
                    ativoPostPutRequestDTO.getNome(),
                    variacaoPercentual * 100);
            notificarInteressados(idAtivo, mensagem);
        }

        return ativoService.atualizarCotacao(idAtivo, novaCotacao);
    }

    @Override
    public AtivoResponseDTO ativarOuDesativar(Long idAtivo, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);

        AtivoResponseDTO ativoAtualizado = ativoService.ativarOuDesativar(idAtivo);

        if (ativoAtualizado.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL) {
            String mensagem = String.format("O ativo '%s' agora está disponível para compra!", ativoAtualizado.getNome());
            notificarInteressados(idAtivo, mensagem);
            ativoService.limparInteressadosDisponibilidade(idAtivo);
        }

        return ativoAtualizado;
    }

    private void notificarInteressados(Long idAtivo, String mensagem) {
        List<Long> interessados = ativoService.recuperarInteressadosDisponibilidade(idAtivo);

        if (interessados == null || interessados.isEmpty()) {
            return;
        }

        for (Long idInteressado : interessados) {
            ClienteResponseDTO cliente = clienteService.recuperar(idInteressado);
            Logger.alertUser(cliente.getNome(), mensagem);
        }
    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        administradorService.validarCodigoAcesso(codigoAcesso);
        ativoService.remover(id);
    }

    @Override
    public AtivoResponseDTO visualizarAtivo(Long idCliente, Long idAtivo) {

        ClienteResponseDTO cliente = clienteService.recuperar(idCliente);

        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        if((cliente.getPlano() == TipoPlano.NORMAL) && (ativo.getTipo() != TipoAtivo.TESOURO_DIRETO)){
            throw new ServicoNaoDisponivelParaPlanoException("Plano do cliente nao permite marcar interesse!");
        }

        return ativo;
    }
}