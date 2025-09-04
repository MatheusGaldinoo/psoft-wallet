package com.ufcg.psoft.commerce.services.resgate;

import com.ufcg.psoft.commerce.dtos.resgate.AtualizarStatusResgateDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ResgateService {

    ResgateResponseDTO solicitarResgate(Long idCliente, ResgatePostPutRequestDTO dto);

    @Transactional
    ResgateResponseDTO executarResgate(Long idCliente, Long idResgate);

    List<ResgateResponseDTO> listarResgatesDoCliente(Long idCliente, String status, String periodoInicio, String periodoFim);

    ResgateResponseDTO consultarResgate(Long idResgate);

    List<ResgateResponseDTO> listarResgates(Long clienteId, String status, String periodoInicio, String periodoFim);

    ResgateResponseDTO atualizarStatusResgate(Long idResgate, AtualizarStatusResgateDTO dto);
}
