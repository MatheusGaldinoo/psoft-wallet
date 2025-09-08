package com.ufcg.psoft.commerce.repositories;

import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.enums.EstadoResgate;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.transacao.Resgate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ResgateRepository extends JpaRepository<Resgate, Long> {
    List<Resgate> findByIdCliente(Long idCliente);

    @Query("SELECT r FROM Resgate r " +
            "WHERE (:clienteId IS NULL OR r.idCliente = :clienteId) " +
            "AND (:tipoAtivo IS NULL OR r.tipoAtivo = :tipoAtivo) " +
            "AND (:statusResgate IS NULL OR r.estado_atual = :statusResgate) " +
            "AND (:dataInicio IS NULL OR " +
            "(r.dataSolicitacao BETWEEN :dataInicio AND :dataFim) OR " +
            "(r.dataFinalizacao BETWEEN :dataInicio AND :dataFim)) "+
            "ORDER BY r.dataSolicitacao DESC")
    List<Resgate> findAllResgates(Long clienteId, TipoAtivo tipoAtivo, EstadoResgate statusResgate, LocalDateTime dataInicio, LocalDateTime dataFim);
}
