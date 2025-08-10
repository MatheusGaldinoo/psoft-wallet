package com.ufcg.psoft.commerce.services.ativo;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;

import java.util.List;

public interface AtivoService {

    AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO ativoPostPutRequestDTO);

    AtivoResponseDTO ativarOuDesativar(Long id);

    List<AtivoResponseDTO> listarTodos();

    List<AtivoResponseDTO> listarFiltrandoPorTipo(List<TipoAtivo> tiposParaFiltrar);

    AtivoResponseDTO recuperar(Long id);

    AtivoResponseDTO criar(AtivoPostPutRequestDTO ativoPostPutRequestDTO);

    void remover(Long id);

    List<Long> recuperarInteressadosCotacao(Long id);

    List<Long> recuperarInteressadosDisponibilidade(Long id);

    void limparInteressadosDisponibilidade(Long id);

    AtivoResponseDTO atualizarCotacao(Long id, Double novaCotacao);

    void adicionarInteressado(Long idAtivo, Long idCliente);
}