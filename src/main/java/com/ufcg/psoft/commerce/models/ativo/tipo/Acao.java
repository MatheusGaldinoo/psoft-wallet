package com.ufcg.psoft.commerce.models.ativo.tipo;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;

import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("ACAO")
public class Acao extends TipoDeAtivo{

    public Acao() {
        super.setNomeTipo(TipoAtivo.ACAO);
    }

    @Override
    public TipoAtivo getNomeTipo() {
        return TipoAtivo.ACAO;
    }

    @Override
    public double calcularImposto(double lucro){
        return lucro * 0.15;
    }

}