package com.ufcg.psoft.commerce.interfaces.transacao;

import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import java.time.LocalDateTime;
import java.util.List;

public interface TransacaoStrategy {

    List<TransacaoResponseDTO> listarAllItens(Long clienteId, TipoAtivo tipoAtivo, String statusCompra, String statusResgate, LocalDateTime dataInicio, LocalDateTime dataFim);

}
