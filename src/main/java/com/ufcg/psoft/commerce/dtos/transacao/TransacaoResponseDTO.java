package com.ufcg.psoft.commerce.dtos.transacao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransacaoResponseDTO {

    @JsonProperty("compras")
    private CompraResponseDTO compra;

    @JsonProperty("resgates")
    private ResgateResponseDTO resgate;

}
