package com.ufcg.psoft.commerce.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;



@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TesouroDireto.class, name = "TESOURO_DIRETO"),
        @JsonSubTypes.Type(value = CriptoMoeda.class, name = "CRIPTOMOEDA"),
        @JsonSubTypes.Type(value = Acao.class, name = "ACAO")
})
public abstract class TipoDeAtivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("nomeTipo")
    private TipoAtivo nomeTipo;

}