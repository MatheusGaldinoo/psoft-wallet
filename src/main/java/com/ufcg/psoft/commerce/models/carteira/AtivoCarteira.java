package com.ufcg.psoft.commerce.models.carteira;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class AtivoCarteira {

    private double quantidade;

    private double valorAcumulado;
}

