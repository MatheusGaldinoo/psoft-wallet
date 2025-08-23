package com.ufcg.psoft.commerce.models.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.Transacao;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Compra extends Transacao {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("estado")
    private EstadoCompra estado;
}
