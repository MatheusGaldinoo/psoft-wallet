package com.ufcg.psoft.commerce.services.compra;

import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface CompraService {

    CompraResponseDTO solicitarCompra(Long idCliente, CompraPostPutRequestDTO dto);

    CompraResponseDTO executarCompra(Long idCliente, Long idCompra);

    CompraResponseDTO aprovarCompra(Long idCompra, String adminCodigoAcesso);

    CompraResponseDTO recusarCompra(Long idCompra, String adminCodigoAcesso);

    List<CompraResponseDTO> listarComprasDoCliente(Long idCliente);

    List<TransacaoResponseDTO> listarAllItens(Long clienteId, TipoAtivo tipoAtivo, String statusCompra, String statusResgate, LocalDateTime dataInicio, LocalDateTime dataFim);
}
