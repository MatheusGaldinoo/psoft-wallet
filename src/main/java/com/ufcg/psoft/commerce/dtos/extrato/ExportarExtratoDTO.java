package com.ufcg.psoft.commerce.dtos.extrato;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ExportarExtratoDTO {
    @JsonProperty("codigoAcesso")
    @NotNull
    private String codigoAcesso;

    @JsonProperty("transacoes")
    private List<TransacaoResponseDTO> transacoes;
}
