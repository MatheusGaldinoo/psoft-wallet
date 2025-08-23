package com.ufcg.psoft.commerce.dtos.cliente;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.dtos.carteira.CarteiraResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    @JsonProperty("id")
    @Id
    @NotBlank(message = "Id obrigatorio")
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("endereco")
    @NotBlank(message = "Endereco obrigatorio")
    private String endereco;

    @JsonProperty("plano")
    @NotNull(message = "Plano obrigatorio")
    private TipoPlano plano;

    @JsonProperty("carteira")
    @NotNull(message = "Carteira obrigatoria")
    private CarteiraResponseDTO carteira;

    public ClienteResponseDTO(Cliente cliente) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
        this.endereco = cliente.getEndereco();
        this.plano = cliente.getPlano();
        if (cliente.getCarteira() != null) {
            this.carteira = new CarteiraResponseDTO(cliente.getCarteira());
        }
    }
}