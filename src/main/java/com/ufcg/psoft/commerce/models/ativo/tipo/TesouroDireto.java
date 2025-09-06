package com.ufcg.psoft.commerce.models.ativo.tipo;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("TESOURO_DIRETO")
public class TesouroDireto extends TipoDeAtivo {

    public TesouroDireto() {
        super.setNomeTipo(TipoAtivo.TESOURO_DIRETO);
    }

    @Override
    public TipoAtivo getNomeTipo() {
        return TipoAtivo.TESOURO_DIRETO;
    }

    @Override
    public double calcularImposto(double lucro){
        return lucro * 0.1;
    }

}