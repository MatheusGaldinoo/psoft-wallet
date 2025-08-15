package com.ufcg.psoft.commerce.models.usuario;

import com.ufcg.psoft.commerce.base.Usuario;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {}