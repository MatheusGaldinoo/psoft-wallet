package com.ufcg.psoft.commerce.repositories;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TipoDeAtivoRepository extends JpaRepository<TipoDeAtivo, Long>{
}