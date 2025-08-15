package com.ufcg.psoft.commerce.services.compra;

import com.ufcg.psoft.commerce.dtos.transacao.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.transacao.CompraResponseDTO;
import com.ufcg.psoft.commerce.exceptions.BalancoInsuficienteException;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.ClienteRepository;
import com.ufcg.psoft.commerce.repositories.CompraRepository;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompraServiceImpl implements CompraService {

    @Autowired
    AtivoService ativoService;

    @Autowired
    ClienteService clienteService;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CompraResponseDTO comprarAtivo(CompraPostPutRequestDTO dto) {
        Optional<Cliente> cliente = clienteRepository.findById(dto.getClienteId());

        /* TODO - Validação da senha
        if (!cliente.getCodigoAcesso().equals(dto.getCodigoAcesso)) {
            throw new RuntimeException("Código de acesso inválido");
        } */

        Carteira carteira = cliente.get().getCarteira();

        double precoTotal = dto.getQuantidade() * dto.getPrecoUnitario();
        if (carteira.getBalanco() < precoTotal) {
            throw new BalancoInsuficienteException("Balanco insuficiente na carteira");
        }

        carteira.setBalanco(carteira.getBalanco() - precoTotal);

        // TODO - Atualizar o mapa 'quantidadeDeAtivo' em Carteira

        Compra compra = modelMapper.map(dto, Compra.class);

        carteira.getCompras().add(compra);

        compraRepository.save(compra);
        return modelMapper.map(compra, CompraResponseDTO.class);
    }
}
