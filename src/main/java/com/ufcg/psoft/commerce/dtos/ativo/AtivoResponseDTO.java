package com.ufcg.psoft.commerce.dtos.ativo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.models.Ativo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtivoResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("tipo")
    private TipoDeAtivo tipo;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("statusDisponibilidade")
    private StatusDisponibilidade statusDisponibilidade;

    @JsonProperty("valor")
    private Double valor;

    public AtivoResponseDTO(Ativo ativo) {
        this.id = ativo.getId();
        this.nome = ativo.getNome();
        this.tipo = ativo.getTipo();
        this.descricao = ativo.getDescricao();
        this.statusDisponibilidade = ativo.getStatusDisponibilidade();
        this.valor = ativo.getValor();
    }
}