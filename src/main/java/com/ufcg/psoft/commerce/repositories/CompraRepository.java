package com.ufcg.psoft.commerce.repositories;

import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.enums.EstadoResgate;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import com.ufcg.psoft.commerce.models.transacao.Resgate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByIdCliente(Long idCliente);

    @Query("SELECT c FROM Compra c " +
            "WHERE (:clienteId IS NULL OR c.idCliente = :clienteId) " +
            "AND (:tipoAtivo IS NULL OR c.tipoAtivo= :tipoAtivo) " +
            "AND (:statusCompra IS NULL OR c.estadoAtual = :statusCompra) " +
            "AND (:dataInicio IS NULL OR " +
            "(c.dataSolicitacao BETWEEN :dataInicio AND :dataFim)) " +
            //"(c.dataFinalizacao BETWEEN :dataInicio AND :dataFim)) "+
            "ORDER BY c.dataSolicitacao DESC")
    List<Compra> findAllCompras(Long clienteId, TipoAtivo tipoAtivo, EstadoCompra statusCompra, LocalDateTime dataInicio, LocalDateTime dataFim);
}
