package com.ufcg.psoft.commerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.Usuario;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {

    @JsonProperty("endereco")
    @Column(nullable = true)
    private String endereco;

    @JsonProperty("plano")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlano plano;
}