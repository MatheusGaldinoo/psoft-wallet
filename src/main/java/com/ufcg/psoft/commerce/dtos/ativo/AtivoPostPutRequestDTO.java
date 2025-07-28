package com.ufcg.psoft.commerce.dtos.ativo;

import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtivoPostPutRequestDTO {

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("tipo")
    @NotNull(message = "Tipo obrigatorio")
    private TipoAtivo tipo;

    @JsonProperty("descricao")
    @NotBlank(message = "Descrição obrigatoria")
    private String descricao;

    @JsonProperty("statusDisponibilidade")
    @NotNull(message = "Status de disponibilidade obrigatorio")
    private StatusDisponibilidade statusDisponibilidade;

    @JsonProperty("valor")
    @NotNull(message = "Valor obrigatorio")
    @Positive(message = "Valor deve ser positivo")
    private Double valor;
}