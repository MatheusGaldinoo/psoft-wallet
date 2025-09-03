package com.ufcg.psoft.commerce.services.ativocliente;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.exceptions.ServicoNaoDisponivelParaPlanoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AtivoClienteService {

    List<AtivoResponseDTO> listarAtivosDisponiveis(Long idCliente);

    void adicionarInteressado(Long idAtivo, Long idCliente) throws ServicoNaoDisponivelParaPlanoException;

    AtivoResponseDTO visualizarAtivo(Long idCliente, Long idAtivo);

    void validarPermissaoCompra(Long idCliente, Long idAtivo);
}