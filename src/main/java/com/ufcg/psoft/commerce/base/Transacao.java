package com.ufcg.psoft.commerce.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ufcg.psoft.commerce.models.transacao.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Data
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Compra.class, name = "COMPRA"),
})
public abstract class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @JsonProperty("id")
    private Long id;

    @Positive
    @JsonProperty("quantidade")
    private double quantidade;

    @Positive
    @JsonProperty("preco_unitario")
    private double precoUnitario;

    @JsonProperty("data_solicitacao")
    private LocalDateTime dataSolicitacao;

    @JsonProperty("data_finalizacao")
    private LocalDateTime dataFinalizacao;

    protected Transacao() {};
}