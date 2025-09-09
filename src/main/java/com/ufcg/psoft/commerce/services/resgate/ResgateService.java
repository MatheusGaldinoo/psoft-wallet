package com.ufcg.psoft.commerce.services.resgate;

import com.ufcg.psoft.commerce.dtos.AtualizarStatusTransacaoDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.transacao.Resgate;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface ResgateService {

    ResgateResponseDTO solicitarResgate(Long idCliente, ResgatePostPutRequestDTO dto);

    @Transactional
    ResgateResponseDTO executarResgate(Long idCliente, Long idResgate);

    List<ResgateResponseDTO> listarResgatesDoCliente(Long idCliente, String status, String periodoInicio, String periodoFim);

    ResgateResponseDTO consultarResgate(Long idResgate);

    ResgateResponseDTO atualizarStatusResgate(Long idResgate, AtualizarStatusTransacaoDTO dto);

    List<TransacaoResponseDTO> listarAllItens(Long clienteId, TipoAtivo tipoAtivo, String statusCompra, String statusResgate, LocalDateTime dataInicio, LocalDateTime dataFim);

    Resgate buscarPorId(Long idResgate);
}
