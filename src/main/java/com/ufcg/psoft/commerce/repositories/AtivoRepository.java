package com.ufcg.psoft.commerce.repositories;

import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.models.Ativo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtivoRepository extends JpaRepository<Ativo, Long>{

    List<Ativo> findByNomeContaining(String nome);
    List<Ativo> findByStatusDisponibilidade(StatusDisponibilidade status);
}