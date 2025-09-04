package com.ufcg.psoft.commerce.dtos.resgate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.DecisaoAdministrador;
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
public class AtualizarStatusResgateDTO {

    @JsonProperty("estado_atual")
    @NotNull
    private DecisaoAdministrador estado;

    @JsonProperty("admin_codigo_acesso")
    @NotNull(message = "Codigo de acesso obrigatorio")
    @Pattern(regexp = "^\\d{6}$", message = "Codigo de acesso deve ter exatamente 6 digitos numericos")
    private String codigoAcesso;
}
