package com.ufcg.psoft.commerce.dtos.transacao;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoQueryDTO {

    private Long clienteId;

    private TipoAtivo tipoAtivo;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private String tipoOperacao;

    private String statusCompra;

    private String statusResgate;

    @NotBlank(message = "Código de acesso é obrigatório")
    private String codigoAcesso;


}
