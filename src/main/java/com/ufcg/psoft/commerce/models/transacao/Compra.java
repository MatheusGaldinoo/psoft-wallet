package com.ufcg.psoft.commerce.models.transacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.Transacao;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.interfaces.estadocompra.*;
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
public class Compra extends Transacao {

    @Transient
    private EstadoCompraState estado;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JsonProperty("estadoAtual")
    private EstadoCompra estadoAtual = EstadoCompra.SOLICITADO;

    @PostLoad
    private void initEstado() {
        switch (this.estadoAtual) {
            case SOLICITADO -> this.estado = new Solicitado(this);
            case DISPONIVEL -> this.estado = new Disponivel(this);
            case COMPRADO   -> this.estado = new Comprado(this);
            case EM_CARTEIRA -> this.estado = new EmCarteira(this);
        }
    }

    public void modificarEstadoCompra(){
        this.initEstado();
        this.estado.modificarStatus();
    }

}
