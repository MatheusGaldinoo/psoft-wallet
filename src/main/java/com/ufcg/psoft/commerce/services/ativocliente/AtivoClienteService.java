package com.ufcg.psoft.commerce.services.ativocliente;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AtivoClienteService {

    List<AtivoResponseDTO> listarAtivosDisponiveis(Long idCliente);

}