package com.ufcg.psoft.commerce.models.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.Transacao;
import com.ufcg.psoft.commerce.enums.EstadoResgate;
import com.ufcg.psoft.commerce.interfaces.estadoresgate.Confirmado;
import com.ufcg.psoft.commerce.interfaces.estadoresgate.EmConta;
import com.ufcg.psoft.commerce.interfaces.estadoresgate.EstadoResgateState;
import com.ufcg.psoft.commerce.interfaces.estadoresgate.Solicitado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Resgate extends Transacao {

    @Transient
    private EstadoResgateState estado;

    @NotNull
    @JsonProperty("imposto")
    private double imposto;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JsonProperty("estado_atual")
    private EstadoResgate estadoAtual = EstadoResgate.SOLICITADO;

    @PostLoad
    private void initEstado() {
        switch (this.estadoAtual) {
            case SOLICITADO -> this.estado = new Solicitado(this);
            case CONFIRMADO -> this.estado = new Confirmado(this);
            case EM_CONTA   -> this.estado = new EmConta(this);
        }
    }

    public void modificarEstadoResgate(){
        this.initEstado();
        this.estado.modificarStatus();
    }
}