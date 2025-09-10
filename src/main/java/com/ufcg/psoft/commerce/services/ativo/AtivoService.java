package com.ufcg.psoft.commerce.services.ativo;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoCotacaoRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import java.util.List;

public interface AtivoService {

    AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO ativoPostPutRequestDTO);

    AtivoResponseDTO ativarOuDesativar(Long id, String codigoAcesso);

    List<AtivoResponseDTO> listarTodos();

    List<AtivoResponseDTO> listarFiltrandoPorTipo(List<TipoAtivo> tiposParaFiltrar);

    AtivoResponseDTO recuperar(Long id);

    Ativo buscarPorId(Long id);

    AtivoResponseDTO criar(AtivoPostPutRequestDTO ativoPostPutRequestDTO);

    void remover(Long id, String codigoAcesso);

    List<Long> recuperarInteressadosCotacao(Long id);

    List<Long> recuperarInteressadosDisponibilidade(Long id);

    void limparInteressadosDisponibilidade(Long id);

    void adicionarInteressadoCotacao(Long idAtivo, Long idCliente);

    void adicionarInteressadoDisponibilidade(Long idAtivo, Long idCliente);

    void validarDisponibilidade(Long idAtivo);

    AtivoResponseDTO atualizarCotacao(Long id, AtivoCotacaoRequestDTO dto);
}