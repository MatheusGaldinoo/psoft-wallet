package com.ufcg.psoft.commerce.models;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("TESOURO_DIRETO")
public class TesouroDireto extends TipoDeAtivo {

    public TesouroDireto() {}

    public Double calcularDesconto(){
        return 0.0;
    }

    @Override
    public TipoAtivo getNomeTipo() {
        return TipoAtivo.TESOURO_DIRETO;
    }
}