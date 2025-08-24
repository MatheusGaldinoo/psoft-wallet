package com.ufcg.psoft.commerce.dtos.compra;

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
// excluir
    @JsonProperty("cliente_codigo_acesso")
    @NotNull(message = "Codigo de acesso obrigatorio")
    @Pattern(regexp = "^\\d{6}$", message = "Codigo de acesso deve ter exatamente 6 digitos numericos")
    private String codigoAcesso;

    @JsonProperty("ativo_id")
    @NotNull(message = "Ativo obrigatorio")
    private Long idAtivo;

    @JsonProperty("quantidade")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private double quantidade;
}

