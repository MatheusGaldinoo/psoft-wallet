package com.ufcg.psoft.commerce.dtos.cliente;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.dtos.carteira.CarteiraPostPutRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ufcg.psoft.commerce.enums.TipoPlano;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientePostPutRequestDTO {

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("endereco")
    @NotBlank(message = "Endereco obrigatorio")
    private String endereco;

    @JsonProperty("codigoAcesso")
    @NotNull(message = "Codigo de acesso obrigatorio")
    @Pattern(regexp = "^\\d{6}$", message = "Codigo de acesso deve ter exatamente 6 digitos numericos")
    private String codigoAcesso;

    @JsonProperty("plano")
    @NotNull(message = "Plano obrigatorio")
    private TipoPlano plano;

    @JsonProperty("carteira")
    @NotNull(message = "Carteira obrigatoria")
    private CarteiraPostPutRequestDTO carteira;
}