package com.ufcg.psoft.commerce.models.carteira;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    @JsonProperty("balanco")
    private double balanco;

    @ElementCollection
    @CollectionTable(name = "carteira_quantidade", joinColumns = @JoinColumn(name = "carteira_id"))
    @MapKeyColumn(name = "ativo_id")
    @Column(name = "quantidade")
    private Map<Long, Double> quantidadeDeAtivo;

    @OneToMany(mappedBy = "carteira", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty("compras")
    private List<Compra> compras;
}