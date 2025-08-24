package com.ufcg.psoft.commerce.services.compra;

import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CompraService {

    CompraResponseDTO solicitarCompra(Long idCliente, CompraPostPutRequestDTO dto);

    CompraResponseDTO executarCompra(Long idCliente, Long idCompra);

    CompraResponseDTO aprovarCompra(Long idCompra, String adminCodigoAcesso);

    CompraResponseDTO recusarCompra(Long idCompra, String adminCodigoAcesso);

    List<CompraResponseDTO> listarComprasDoCliente(Long idCliente);
}
