package com.ufcg.psoft.commerce.dtos.transacao;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Data
public class TransacaoQueryDTO {

    private Long clienteId;

    private TipoAtivo tipoAtivo;

    private LocalDate data;

    private String tipoOperacao;

    private String statusCompra;

    private String statusResgate;

    @NotBlank(message = "Código de acesso é obrigatório")
    private String codigoAcesso;


}
