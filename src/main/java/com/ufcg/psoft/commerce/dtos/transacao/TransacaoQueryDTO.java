package com.ufcg.psoft.commerce.dtos.transacao;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
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
