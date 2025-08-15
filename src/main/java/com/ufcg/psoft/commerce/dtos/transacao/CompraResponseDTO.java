package com.ufcg.psoft.commerce.dtos.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.TipoTransacao;

import java.time.LocalDateTime;

public class CompraResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("clienteId")
    private Long clienteId;

    @JsonProperty("ativoId")
    private Long ativoId;

    @JsonProperty("quantidade")
    private Double quantidade;

    @JsonProperty("valorUnitario")
    private Double valorUnitario;

    @JsonProperty("tipo")
    private TipoTransacao tipo;

    //private EstadoTransacao estado;

    @JsonProperty("dataSolicitacao")
    private LocalDateTime dataSolicitacao;

    @JsonProperty("dataFinalizacao")
    private LocalDateTime dataFinalizacao;

}
