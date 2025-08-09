package com.ufcg.psoft.commerce.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.Acao;
import com.ufcg.psoft.commerce.models.CriptoMoeda;
import com.ufcg.psoft.commerce.models.TesouroDireto;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "nomeTipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TesouroDireto.class, name = "TESOURO_DIRETO"),
        @JsonSubTypes.Type(value = CriptoMoeda.class, name = "CRIPTOMOEDA"),
        @JsonSubTypes.Type(value = Acao.class, name = "ACAO")
        // Add other concrete types here
})
public abstract class TipoDeAtivo {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("nomeTipo")
    private TipoAtivo nomeTipo;

    public abstract Double calcularDesconto();
    public abstract TipoAtivo getNomeTipo();
}