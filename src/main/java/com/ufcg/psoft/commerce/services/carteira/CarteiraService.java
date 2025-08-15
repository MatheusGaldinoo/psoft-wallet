package com.ufcg.psoft.commerce.services.carteira;

import org.springframework.stereotype.Service;

@Service
public interface CarteiraService {
    // TODO - Controle do saldo (adicionar, debitar e visualizar)
    CarteiraResponseDTO adicionarBalanco(Long idCarteira, double valor);
    CarteiraResponseDTO debitarBalanco(Long idCarteira, double valor);
    // TODO - Controle do ativo (adição e remoção da 'quantidadeDeAtivo')
    CarteiraResponseDTO adicionarQuantidadeAtivo(Long idCarteira, Long idAtivo);
    // TODO - Controle de compra (adição e remoção de 'compras')
    CarteiraResponseDTO adicionarCompra(Long idCarteira, Long idCompra);
    CarteiraResponseDTO removerCompra(Long idCarteira, Long idCompra);
    // TODO - Controle de resgate (adição e remoção de 'resgates')
}
