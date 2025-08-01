package com.ufcg.psoft.commerce.services.ativocliente;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AtivoClienteService {

    List<AtivoResponseDTO> listarAtivosDisponiveis(Long idCliente);

    List<AtivoResponseDTO> marcarInteresseAtivo(Long idCliente, Long idAtivo);

    AtivoResponseDTO alterar(Long id, AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso);

    AtivoResponseDTO criar(AtivoPostPutRequestDTO ativoPostPutRequestDTO, String codigoAcesso);

    void remover(Long id, String codigoAcesso);

    AtivoResponseDTO atualizarCotacao(Long id, Double novaCotacao, String codigoAcesso);

    void adicionarInteressado(Long idAtivo, Long idCliente);

    AtivoResponseDTO ativarOuDesativar(Long id, String codigoAcesso);
}