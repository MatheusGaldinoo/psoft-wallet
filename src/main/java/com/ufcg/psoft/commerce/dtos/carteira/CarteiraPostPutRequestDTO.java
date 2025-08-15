package com.ufcg.psoft.commerce.dtos.carteira;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private double balanco;

}
