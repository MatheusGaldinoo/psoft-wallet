package com.ufcg.psoft.commerce.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ufcg.psoft.commerce.models.transacao.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
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
        @JsonSubTypes.Type(value = Compra.class, name = "RESGATE")
})
public abstract class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("cliente_id")
    private Long idCliente;

    @NotNull
    @JsonProperty("ativo_id")
    private Long idAtivo;

    @Positive
    @JsonProperty("quantidade")
    private double quantidade;

    @Positive
    @JsonProperty("preco_unitario")
    private double precoUnitario;

    @Positive
    @JsonProperty("valor_total")
    private double valorTotal;

    @JsonProperty("data_solicitacao")
    private LocalDateTime dataSolicitacao;

    @JsonProperty("data_finalizacao")
    private LocalDateTime dataFinalizacao;

}