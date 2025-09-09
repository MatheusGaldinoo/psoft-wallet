package com.ufcg.psoft.commerce.services.ativocliente;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.exceptions.ServicoNaoDisponivelParaPlanoException;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
            return Collections.emptyList();
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
    public AtivoResponseDTO visualizarAtivo(Long idCliente, Long idAtivo) {

        ClienteResponseDTO cliente = clienteService.recuperar(idCliente);

        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        if((cliente.getPlano() == TipoPlano.NORMAL) && (ativo.getTipo() != TipoAtivo.TESOURO_DIRETO)){
            throw new ServicoNaoDisponivelParaPlanoException("Plano do cliente nao permite visualizar ativo!");
        }

        return ativo;
    }

    @Override
    public void validarPermissaoCompra(Long idCliente, Long idAtivo) {
        ClienteResponseDTO cliente = clienteService.recuperar(idCliente);

        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        if((cliente.getPlano() == TipoPlano.NORMAL) && (ativo.getTipo() != TipoAtivo.TESOURO_DIRETO)){
            throw new ServicoNaoDisponivelParaPlanoException("Plano do cliente nao permite marcar interesse!");
        }
    }
}