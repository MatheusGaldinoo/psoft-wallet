package com.ufcg.psoft.commerce.services.carteira;

import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CarteiraService {
    void aplicarCompra(Long idCliente, Long idAtivo, double quantidade, double precoUnitario);

    void aplicarResgate(Long idCliente, Long idAtivo, double quantidade);

    void validarBalancoSuficiente(Long idCliente, double valor);

    void adicionarBalanco(Long idCliente, double valor);

    void debitarBalanco(Long idCliente, double valor);

    List<AtivoCarteiraResponseDTO> visualizarCarteira(Long idCliente);

    void validarQuantidadeDisponivel(Long idCliente, Long idAtivo, double quantidade);
}
