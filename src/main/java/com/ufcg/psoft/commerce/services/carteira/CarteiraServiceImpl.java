package com.ufcg.psoft.commerce.services.carteira;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.exceptions.BalancoInsuficienteException;
import com.ufcg.psoft.commerce.exceptions.AtivoNaoExisteException;
import com.ufcg.psoft.commerce.exceptions.QuantidadeInsuficienteException;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.carteira.AtivoCarteira;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
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

    @Autowired
    private AtivoService ativoService;

    @Override
    public void aplicarCompra(Long idCliente, Long idAtivo, double quantidade, double custoTotal) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();

        validarBalancoSuficiente(idCliente, custoTotal);

        carteira.aplicarCompra(idAtivo, quantidade, custoTotal);

        clienteService.salvar(cliente);
    }

    @Override
    public void aplicarResgate(Long idCliente, Long idAtivo, double quantidade) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        AtivoCarteira ativoCarteira = carteira.getAtivos().get(idAtivo);
        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        validarQuantidadeDisponivel(idCliente, idAtivo, quantidade);

        double precoMedio = ativoCarteira.getValorAcumulado() / ativoCarteira.getQuantidade();

        ativoCarteira.setQuantidade(ativoCarteira.getQuantidade() - quantidade);
        // valor acumulado é o total das compras para calcular lucro em relação ao preço atual
        // estamos diminuindo para manter o equilíbrio pro desempenho
        ativoCarteira.setValorAcumulado(ativoCarteira.getValorAcumulado() - (precoMedio * quantidade));

        if (ativoCarteira.getQuantidade() <= 0) {
            carteira.getAtivos().remove(idAtivo);
        }

        adicionarBalanco(idCliente, (ativo.getValor() * quantidade));

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

    @Override
    public void validarQuantidadeDisponivel(Long idCliente, Long idAtivo, double quantidadeSolicitada) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        AtivoCarteira ativoCarteira = carteira.getAtivos().get(idAtivo);

        if (ativoCarteira == null || ativoCarteira.getQuantidade() < quantidadeSolicitada) {
            throw new QuantidadeInsuficienteException();
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
