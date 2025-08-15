package com.ufcg.psoft.commerce.dtos.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraPostPutRequestDTO {

    @JsonProperty("clienteId")
    @NotNull(message = "Cliente obrigatorio")
    private Long clienteId;

    @JsonProperty("codigoAcesso")
    @NotNull(message = "Codigo de acesso obrigatorio")
    @Pattern(regexp = "^\\d{6}$", message = "Codigo de acesso deve ter exatamente 6 digitos numericos")
    private String codigoAcesso;

    @JsonProperty("ativoId")
    @NotNull(message = "Ativo obrigatorio")
    private Long ativoId;

    @JsonProperty("quantidade")
    @NotNull(message = "Quantidade obrigatoria")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Double quantidade;

    @NotNull
    @JsonProperty("preco_unitario")
    private Double precoUnitario;
}

