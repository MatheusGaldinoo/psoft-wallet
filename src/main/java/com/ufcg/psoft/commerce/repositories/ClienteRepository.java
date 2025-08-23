package com.ufcg.psoft.commerce.repositories;

import com.ufcg.psoft.commerce.models.usuario.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNomeContaining(String nome);
}
