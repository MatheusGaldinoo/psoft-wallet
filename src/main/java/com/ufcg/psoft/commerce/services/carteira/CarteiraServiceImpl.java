package com.ufcg.psoft.commerce.services.carteira;

import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.exceptions.BalancoInsuficienteException;
import com.ufcg.psoft.commerce.exceptions.AtivoNaoExisteException;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static java.lang.Math.round;

@Service
public class CarteiraServiceImpl implements CarteiraService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AtivoRepository ativoRepository;

    @Override
    public void aplicarCompra(Long idCliente, Long idAtivo, double quantidade, double custoTotal) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();

        validarBalancoSuficiente(idCliente, custoTotal);

        carteira.aplicarCompra(idAtivo, quantidade, custoTotal);

        clienteService.salvar(cliente);
    }

    @Override
    public void validarBalancoSuficiente(Long idCliente, double valor) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        validarBalancoSuficiente(cliente, valor);
    }

    private void validarBalancoSuficiente(Cliente cliente, double valor) {
        Carteira carteira = cliente.getCarteira();
        if (carteira.getBalanco() < valor) {
            throw new BalancoInsuficienteException();
        }
    }

    @Override
    public void adicionarBalanco(Long idCliente, double valor) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        carteira.setBalanco(carteira.getBalanco() + valor);
        clienteService.salvar(cliente);
    }

    @Override
    public void debitarBalanco(Long idCliente, double valor) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        carteira.setBalanco(carteira.getBalanco() - valor);
        clienteService.salvar(cliente);
    }

    @Override
    public List<AtivoCarteiraResponseDTO> visualizarCarteira(Long idCliente) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();

        return carteira.getAtivos().entrySet().stream()
                .map(e -> {
                    Ativo ativo = ativoRepository.findById(e.getKey())
                            .orElseThrow(AtivoNaoExisteException::new);

                    double valorAquisicaoAcumulado = e.getValue().getValorAcumulado();
                    double valorAtualUnitario = ativo.getValor();
                    double quantidade = e.getValue().getQuantidade();

                    double valorAtualTotal = round(valorAtualUnitario * quantidade, 2);
                    double desempenho = round(valorAtualTotal - valorAquisicaoAcumulado, 2);

                    return AtivoCarteiraResponseDTO.builder()
                            .idAtivo(e.getKey())
                            .tipo(ativo.getTipo().getNomeTipo())
                            .quantidade(quantidade)
                            .valorAquisicaoAcumulado(valorAquisicaoAcumulado)
                            .valorAtualUnitario(valorAtualUnitario)
                            .desempenho(desempenho)
                            .build();
                })
                .toList();
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
