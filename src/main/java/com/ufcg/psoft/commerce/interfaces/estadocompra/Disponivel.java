package com.ufcg.psoft.commerce.interfaces.estadocompra;

import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Disponivel implements EstadoCompraState {

    private Compra compra;

    @Override
    public void modificarStatus(){

        compra.setEstado(new Comprado(this.compra));
        compra.setEstadoAtual(EstadoCompra.COMPRADO);

    }

}
