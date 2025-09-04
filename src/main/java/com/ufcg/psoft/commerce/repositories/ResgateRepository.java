package com.ufcg.psoft.commerce.repositories;

import com.ufcg.psoft.commerce.models.transacao.Resgate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResgateRepository extends JpaRepository<Resgate, Long> {
    List<Resgate> findByIdCliente(Long idCliente);
}
