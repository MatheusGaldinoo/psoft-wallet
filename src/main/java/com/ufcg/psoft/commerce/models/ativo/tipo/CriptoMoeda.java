package com.ufcg.psoft.commerce.models.ativo.tipo;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("CRIPTOMOEDA")
public class CriptoMoeda extends TipoDeAtivo{

    public CriptoMoeda() {
        super.setNomeTipo(TipoAtivo.CRIPTOMOEDA);
    }

    @Override
    public TipoAtivo getNomeTipo() {
        return TipoAtivo.CRIPTOMOEDA;
    }
}