package com.ufcg.psoft.commerce.services.ativo;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;

import java.util.List;

public interface AtivoService {

    AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso);

    AtivoResponseDTO ativarOuDesativar(Long id, String codigoAcesso);

    List<AtivoResponseDTO> listarTodos();

    List<AtivoResponseDTO> listarFiltrandoPorTipo(List<TipoAtivo> tiposParaFiltrar);

    AtivoResponseDTO recuperar(Long id);

    AtivoResponseDTO criar(AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso);

    void remover(Long id, String codigoAcesso);

    AtivoResponseDTO atualizarCotacao(Long id, Double novaCotacao, String codigoAcesso);
}