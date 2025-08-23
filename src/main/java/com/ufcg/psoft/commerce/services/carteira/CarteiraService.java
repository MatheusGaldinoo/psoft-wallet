package com.ufcg.psoft.commerce.services.carteira;

import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CarteiraService {
    void aplicarCompra(Long idCliente, Long idAtivo, double quantidade, double precoUnitario);

    void validarBalancoSuficiente(Long idCliente, double valor);

    void adicionarBalanco(Long idCliente, double valor);

    void debitarBalanco(Long idCliente, double valor);

    List<AtivoCarteiraResponseDTO> visualizarCarteira(Long idCliente);
}
