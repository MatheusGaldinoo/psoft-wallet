package com.ufcg.psoft.commerce.services.transacao;

import com.ufcg.psoft.commerce.dtos.transacao.TransacaoQueryDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.interfaces.transacao.TransacaoStrategy;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.compra.CompraServiceImpl;
import com.ufcg.psoft.commerce.services.resgate.ResgateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransacaoServiceImpl implements TransacaoService{

    @Autowired
    private AdministradorService administradorService;

    private Map<String, TransacaoStrategy> strategies;;

    public TransacaoServiceImpl(CompraServiceImpl compraService, ResgateServiceImpl resgateService){
        this.strategies = new HashMap<>();
        this.strategies.put("COMPRA", compraService);
        this.strategies.put("RESGATE", resgateService);
    }

    @Override
    public List<TransacaoResponseDTO> listarTransacoes(TransacaoQueryDTO transacaoQueryDTO){
        administradorService.validarCodigoAcesso(transacaoQueryDTO.getCodigoAcesso());

        LocalDateTime dataInicio = (transacaoQueryDTO.getData() != null) ? transacaoQueryDTO.getData().atStartOfDay() : null;
        LocalDateTime dataFim = (transacaoQueryDTO.getData() != null) ? transacaoQueryDTO.getData().plusDays(1).atStartOfDay() : null;

        if (transacaoQueryDTO.getTipoOperacao() == null) {
            return strategies.values().stream()
                    .flatMap(s -> s.listarAllItens(transacaoQueryDTO.getClienteId(), transacaoQueryDTO.getTipoAtivo(), dataInicio, dataFim).stream())
                    .collect(Collectors.toList());
        }

        TransacaoStrategy strategy = strategies.get(transacaoQueryDTO.getTipoOperacao().toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Operação não suportada: " + transacaoQueryDTO.getTipoOperacao());
        }

        return strategy.listarAllItens(transacaoQueryDTO.getClienteId(), transacaoQueryDTO.getTipoAtivo(), dataInicio, dataFim);
    }

}
