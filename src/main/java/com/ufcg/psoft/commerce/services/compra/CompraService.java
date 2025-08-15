package com.ufcg.psoft.commerce.services.compra;

import com.ufcg.psoft.commerce.dtos.transacao.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.transacao.CompraResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface CompraService {

    CompraResponseDTO comprarAtivo(CompraPostPutRequestDTO compraPostPutRequestDto);
}
