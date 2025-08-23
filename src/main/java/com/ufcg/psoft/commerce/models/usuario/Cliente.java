package com.ufcg.psoft.commerce.models.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.Usuario;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @JsonProperty("endereco")
    private String endereco;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("plano")
    private TipoPlano plano;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "carteira_id", referencedColumnName = "id")
    @JsonProperty("carteira")
    private Carteira carteira;

    @PrePersist
    private void prePersist() {
        if (this.carteira == null) {
            this.carteira = Carteira.builder().balanco(0.0).build();
        }
    }
}