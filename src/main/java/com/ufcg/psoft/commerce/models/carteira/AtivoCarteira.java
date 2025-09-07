package com.ufcg.psoft.commerce.models.carteira;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AtivoCarteira {

    private double quantidade;

    private double valorAcumulado;

    private double quantidadeAcumulada;
}

