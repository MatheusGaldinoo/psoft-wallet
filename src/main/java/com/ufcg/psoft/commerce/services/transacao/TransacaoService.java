package com.ufcg.psoft.commerce.services.transacao;

import com.ufcg.psoft.commerce.dtos.CodigoAcessoDTO;
import com.ufcg.psoft.commerce.dtos.extrato.ExportarExtratoDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoQueryDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransacaoService {


    public List<TransacaoResponseDTO> listarTransacoes(TransacaoQueryDTO transacaoQueryDTO);
    public String gerarExtratoCSV(Long clienteId, ExportarExtratoDTO dto);

}
