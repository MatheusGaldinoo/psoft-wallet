package com.ufcg.psoft.commerce.services.carteira;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

@Service
public class CarteiraServiceImpl implements CarteiraService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AtivoService ativoService;

    @Override
    public void aplicarCompra(Long idCliente, Long idAtivo, double quantidade, double custoTotal) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();

        validarBalancoSuficiente(idCliente, custoTotal);

        Map<Long, AtivoCarteira> ativos = carteira.getAtivos();

        if (ativos.containsKey(idAtivo)) {
            AtivoCarteira ativoCarteira = ativos.get(idAtivo);
            ativoCarteira.setQuantidade(ativoCarteira.getQuantidade() + quantidade);
            ativoCarteira.setValorAcumulado(ativoCarteira.getValorAcumulado() + custoTotal);
            ativoCarteira.setQuantidadeAcumulada(ativoCarteira.getQuantidadeAcumulada() + quantidade);
        } else {
            ativos.put(idAtivo, new AtivoCarteira(quantidade, custoTotal, quantidade));
        }

        carteira.setBalanco(carteira.getBalanco() - custoTotal);

        clienteService.salvar(cliente);
    }

    @Override
    public void aplicarResgate(Long idCliente, Long idAtivo, double quantidade) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        AtivoCarteira ativoCarteira = carteira.getAtivos().get(idAtivo);
        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        validarQuantidadeDisponivel(idCliente, idAtivo, quantidade);

        // Estamos comparando o preço médio do valor acumulado de todas as compras daquele ativo,
        // com quanto seria a mesma quantidade sob o preço atual para decidir desempenho e imposto.
        double precoMedio = ativoCarteira.getValorAcumulado() / ativoCarteira.getQuantidadeAcumulada();
        double valorVenda = ativo.getValor() * quantidade;
        double custo = precoMedio * quantidade;
        double lucro = valorVenda - custo;

        double imposto = calcularImposto(ativo.getTipo(), lucro);
        double valorLiquido = valorVenda - imposto;

        ativoCarteira.setQuantidade(ativoCarteira.getQuantidade() - quantidade);

        if (ativoCarteira.getQuantidade() <= 0) {
            carteira.getAtivos().remove(idAtivo);
        }

        adicionarBalanco(idCliente, valorLiquido);

        clienteService.salvar(cliente);
    }

    private double calcularImposto(TipoAtivo tipoAtivo, double lucro) {
        // TODO - Adicionar a quantidadeAcumulada no AtivoCarteiraResponseDTO e imposto no ResgateResponseDTO.
        if (lucro <= 0) return 0.0;

        return switch (tipoAtivo) {
            case TESOURO_DIRETO -> lucro * 0.10;
            case ACAO -> lucro * 0.15;
            case CRIPTOMOEDA -> {
                if (lucro <= 5000) yield lucro * 0.15;
                else yield lucro * 0.225;
            }
        };
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
                    AtivoResponseDTO ativo = ativoService.recuperar(e.getKey());

                    double valorAquisicaoAcumulado = e.getValue().getValorAcumulado();
                    double valorAtualUnitario = ativo.getValor();
                    double quantidadeAtual = e.getValue().getQuantidade();
                    double quantidadeAcumulada = e.getValue().getQuantidadeAcumulada();

                    double valorAtualTotal = round(valorAtualUnitario * quantidadeAcumulada, 2);
                    double desempenho = round(valorAtualTotal - valorAquisicaoAcumulado, 2);

                    return AtivoCarteiraResponseDTO.builder()
                            .idAtivo(e.getKey())
                            .tipo(ativo.getTipo())
                            .quantidade(quantidadeAtual)
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
