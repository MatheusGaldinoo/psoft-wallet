package com.ufcg.psoft.commerce.services.transacao;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoQueryDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.interfaces.transacao.TransacaoStrategy;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import com.ufcg.psoft.commerce.services.compra.CompraServiceImpl;
import com.ufcg.psoft.commerce.services.resgate.ResgateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransacaoServiceImpl implements TransacaoService{

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AtivoService ativoService;

    private Map<String, TransacaoStrategy> strategies;

    public TransacaoServiceImpl(CompraServiceImpl compraService, ResgateServiceImpl resgateService){
        this.strategies = new HashMap<>();
        this.strategies.put("COMPRA", compraService);
        this.strategies.put("RESGATE", resgateService);
    }

    @Override
    public List<TransacaoResponseDTO> listarTransacoes(TransacaoQueryDTO transacaoQueryDTO){

        if (transacaoQueryDTO.getTipoOperacao() == null) {
            return strategies.values().stream()
                    .flatMap(s -> s.listarAllItens(
                            transacaoQueryDTO.getClienteId(),
                            transacaoQueryDTO.getTipoAtivo(),
                            transacaoQueryDTO.getStatusCompra(),
                            transacaoQueryDTO.getStatusResgate(),
                            transacaoQueryDTO.getDataInicio(),
                            transacaoQueryDTO.getDataFim())
                    .stream()).collect(Collectors.toList());
        }

        TransacaoStrategy strategy = strategies.get(transacaoQueryDTO.getTipoOperacao().toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Operação não suportada: " + transacaoQueryDTO.getTipoOperacao());
        }

        return strategy.listarAllItens(
                transacaoQueryDTO.getClienteId(),
                transacaoQueryDTO.getTipoAtivo(),
                transacaoQueryDTO.getStatusCompra(),
                transacaoQueryDTO.getStatusResgate(),
                transacaoQueryDTO.getDataInicio(),
                transacaoQueryDTO.getDataFim()
        );
    }


    @Override
    public String gerarExtratoCSV(Long clienteId, String codigoAcesso) {
        clienteService.validarCodigoAcesso(clienteId, codigoAcesso);

        TransacaoQueryDTO query = new TransacaoQueryDTO();
        query.setClienteId(clienteId);
        query.setCodigoAcesso(codigoAcesso);

        List<TransacaoResponseDTO> transacoes = listarTransacoes(query);

        StringBuilder sb = new StringBuilder();
        sb.append("Tipo,Ativo,Quantidade,Imposto,Valor,DataSolicitacao,DataFinalizacao\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (TransacaoResponseDTO t : transacoes) {
            if (t.getCompra() != null) {
                double valorTotal = 0.0;
                try {
                    valorTotal = ativoService.recuperar(t.getCompra().getIdAtivo()).getValor()
                            * t.getCompra().getQuantidade();
                } catch (Exception e) {
                    // Mantém valorTotal = 0 caso haja erro
                }

                sb.append("COMPRA,")
                        .append(t.getCompra().getIdAtivo()).append(",")
                        .append(t.getCompra().getQuantidade()).append(",")
                        .append("0,") // Compras não têm imposto
                        .append(valorTotal).append(",")
                        .append(t.getCompra().getDataSolicitacao().format(formatter)).append(",")
                        .append(t.getCompra().getDataFinalizacao() != null
                                ? t.getCompra().getDataFinalizacao().format(formatter)
                                : "").append("\n");
            }

            if (t.getResgate() != null) {
                double valorTotal = 0.0;
                try {
                    valorTotal = ativoService.recuperar(t.getResgate().getIdAtivo()).getValor()
                            * t.getResgate().getQuantidade();
                } catch (Exception e) { }

                sb.append("RESGATE,")
                        .append(t.getResgate().getIdAtivo()).append(",")
                        .append(t.getResgate().getQuantidade()).append(",")
                        .append(t.getResgate().getImposto()).append(",")
                        .append(valorTotal).append(",")
                        .append(t.getResgate().getDataSolicitacao().format(formatter)).append(",")
                        .append(t.getResgate().getDataFinalizacao() != null
                                ? t.getResgate().getDataFinalizacao().format(formatter)
                                : "").append("\n");
            }
        }

        return sb.toString();
    }
}