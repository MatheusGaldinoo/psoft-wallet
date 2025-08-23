package com.ufcg.psoft.commerce.dtos.compra;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("estado")
    private EstadoCompra estado;

    @JsonProperty("cliente_id")
    private Long idCliente;

    @JsonProperty("ativo_id")
    private Long idAtivo;

    @JsonProperty("quantidade")
    private double quantidade;

    @JsonProperty("preco_unitario")
    private double precoUnitario;

    @JsonProperty("valor_total")
    private double valorTotal;

    @JsonProperty("data_solicitacao")
    private LocalDateTime dataSolicitacao;

    @JsonProperty("data_finalizacao")
    private LocalDateTime dataFinalizacao;
}
