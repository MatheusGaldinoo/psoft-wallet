package com.ufcg.psoft.commerce.interfaces.estadocompra;

import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comprado implements EstadoCompraState {

    private Compra compra;

    @Override
    public void modificarStatus(){

        compra.setEstado(new EmCarteira(this.compra));
        compra.setEstadoAtual(EstadoCompra.EM_CARTEIRA);

    }

}
