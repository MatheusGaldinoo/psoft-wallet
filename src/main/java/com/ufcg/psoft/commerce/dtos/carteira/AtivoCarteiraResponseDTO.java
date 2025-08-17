package com.ufcg.psoft.commerce.dtos.carteira;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtivoCarteiraResponseDTO {
    @JsonProperty("id_ativo")
    private Long idAtivo;

    @JsonProperty("tipo_ativo")
    private TipoAtivo tipo;

    @JsonProperty("quantidade")
    private double quantidade;

    @JsonProperty("valor_aquisicao_acumulado")
    private double valorAquisicaoAcumulado;

    @JsonProperty("valor_atual_unitario")
    private double valorAtualUnitario;

    @JsonProperty("desempenho")
    private double desempenho;
}
