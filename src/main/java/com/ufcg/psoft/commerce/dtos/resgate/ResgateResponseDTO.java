package com.ufcg.psoft.commerce.dtos.resgate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.EstadoResgate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResgateResponseDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("cliente_id")
    private Long idCliente;

    @JsonProperty("estado_atual")
    private EstadoResgate estado;

    @JsonProperty("ativo_id")
    private Long idAtivo;

    @JsonProperty("quantidade")
    private double quantidade;

    @JsonProperty("imposto")
    private double imposto;

    @JsonProperty("data_solicitacao")
    private LocalDateTime dataSolicitacao;

    @JsonProperty("data_finalizacao")
    private LocalDateTime dataFinalizacao;
}
