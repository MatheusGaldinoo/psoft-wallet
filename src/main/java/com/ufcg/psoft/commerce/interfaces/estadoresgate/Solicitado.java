package com.ufcg.psoft.commerce.interfaces.estadoresgate;

import com.ufcg.psoft.commerce.enums.EstadoResgate;
import com.ufcg.psoft.commerce.models.transacao.Resgate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Solicitado implements EstadoResgateState {

    private Resgate resgate;

    @Override
    public void modificarStatus(){
        resgate.setEstado(new Confirmado(this.resgate));
        resgate.setEstadoAtual(EstadoResgate.CONFIRMADO);
    }

}
