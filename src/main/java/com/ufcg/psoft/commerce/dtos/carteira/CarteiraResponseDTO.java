package com.ufcg.psoft.commerce.dtos.carteira;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteiraResponseDTO {

    @JsonProperty("balanco")
    private double balanco;

    @JsonProperty("ativos")
    private Map<Long, AtivoCarteiraDTO> ativos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtivoCarteiraDTO {
        @JsonProperty("quantidade")
        private double quantidade;

        @JsonProperty("valorAcumulado")
        private double valorAcumulado;
    }

    public CarteiraResponseDTO(Carteira carteira) {
        this.balanco = carteira.getBalanco();
        if (carteira.getAtivos() != null) {
            this.ativos = carteira.getAtivos().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            e -> new AtivoCarteiraDTO(e.getValue().getQuantidade(), e.getValue().getValorAcumulado())));
        }
    }
}
