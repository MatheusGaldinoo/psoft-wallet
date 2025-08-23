package com.ufcg.psoft.commerce.interfaces.estadocompra;

import com.ufcg.psoft.commerce.models.transacao.Compra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmCarteira implements EstadoCompraState {

    private Compra compra;

    @Override
    public void modificarStatus(){
        //Does nothing
    }

}
