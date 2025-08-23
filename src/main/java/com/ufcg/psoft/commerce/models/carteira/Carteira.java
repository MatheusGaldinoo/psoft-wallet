package com.ufcg.psoft.commerce.models.carteira;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    @JsonProperty("balanco")
    private double balanco;

    @ElementCollection
    @CollectionTable(name = "carteira_ativos", joinColumns = @JoinColumn(name = "carteira_id"))
    @MapKeyColumn(name = "ativo_id")
    @Builder.Default
    private Map<Long, AtivoCarteira> ativos = new HashMap<>();

    public void aplicarCompra(Long idAtivo, double quantidade, double custoTotal) {
        if (ativos.containsKey(idAtivo)) {
            AtivoCarteira ativoCarteira = ativos.get(idAtivo);
            ativoCarteira.setQuantidade(ativoCarteira.getQuantidade() + quantidade);
            ativoCarteira.setValorAcumulado(ativoCarteira.getValorAcumulado() + custoTotal);
        } else {
            ativos.put(idAtivo, new AtivoCarteira(quantidade, custoTotal));
        }
        balanco -= custoTotal;
    }
}