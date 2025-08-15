package com.ufcg.psoft.commerce.models.ativo;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;


@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ativo {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("tipo")
    @ManyToOne
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoDeAtivo tipo;

    @JsonProperty("descricao")
    @Column(nullable = false)
    private String descricao;

    @JsonProperty("valor")
    @Column(nullable = false)
    private Double valor;

    @JsonProperty("statusDisponibilidade")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDisponibilidade statusDisponibilidade;

    // Interessados na variação da cotação
    @JsonProperty("interessadosCotacao")
    @ElementCollection
    @CollectionTable(
            name = "ativo_interessados_cotacao",
            joinColumns = @JoinColumn(name = "ativo_id")
    )
    @Column(name = "cliente_id")
    private List<Long> interessadosCotacao;

    // Interessados quando o ativo ficar disponível
    @JsonProperty("interessadosDisponibilidade")
    @ElementCollection
    @CollectionTable(
            name = "ativo_interessados_disponibilidade",
            joinColumns = @JoinColumn(name = "ativo_id")
    )
    @Column(name = "cliente_id")
    private List<Long> interessadosDisponibilidade;

    public void addInteressadoCotacao(Long idCliente) {
        this.interessadosCotacao.add(idCliente);
    }

    public void addInteressadoDisponibilidade(Long idCliente) {
        this.interessadosDisponibilidade.add(idCliente);
    }
}