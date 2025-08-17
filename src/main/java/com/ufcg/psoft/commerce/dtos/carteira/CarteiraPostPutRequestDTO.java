package com.ufcg.psoft.commerce.dtos.carteira;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteiraPostPutRequestDTO {

    @JsonProperty("balanco")
    @PositiveOrZero(message = "Balanco nao pode ser negativo")
    private double balanco;
}
