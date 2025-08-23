package com.ufcg.psoft.commerce.services.cliente;

import com.ufcg.psoft.commerce.dtos.cliente.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO alterar(Long id, String codigoAcesso, ClientePostPutRequestDTO clientePostPutRequestDTO);

    List<ClienteResponseDTO> listar();

    ClienteResponseDTO recuperar(Long id);

    ClienteResponseDTO criar(ClientePostPutRequestDTO clientePostPutRequestDTO);

    void remover(Long id, String codigoAcesso);

    List<ClienteResponseDTO> listarPorNome(String nome);

    Cliente buscarPorId(Long idCliente);

    void salvar(Cliente cliente);

    void validarCodigoAcesso(Long idCliente, String codigoAcesso);
}