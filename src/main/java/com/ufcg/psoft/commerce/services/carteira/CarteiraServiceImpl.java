package com.ufcg.psoft.commerce.services.carteira;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.exceptions.BalancoInsuficienteException;
import com.ufcg.psoft.commerce.exceptions.QuantidadeInsuficienteException;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.carteira.AtivoCarteira;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

@Service
public class CarteiraServiceImpl implements CarteiraService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AtivoService ativoService;

    @Autowired
    private ModelMapper modelMapper;

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
    public void aplicarResgate(Long idCliente, double impostoResgate, Long idAtivo, double quantidade) {
        validarQuantidadeDisponivel(idCliente, idAtivo, quantidade);
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        AtivoCarteira ativoCarteira = carteira.getAtivos().get(idAtivo);
        AtivoResponseDTO ativo = ativoService.recuperar(idAtivo);

        double valorVenda = ativo.getValor() * quantidade;

        double valorLiquido = valorVenda - impostoResgate;

        ativoCarteira.setQuantidade(ativoCarteira.getQuantidade() - quantidade);

        if (ativoCarteira.getQuantidade() <= 0) {
            carteira.getAtivos().remove(idAtivo);
        }

        adicionarBalanco(idCliente, valorLiquido);

        clienteService.salvar(cliente);
    }

    @Override
    public double calcularImpostoDevido(Long idCliente, Long idAtivo, double quantidade){
        Cliente cliente = clienteService.buscarPorId(idCliente);
        Carteira carteira = cliente.getCarteira();
        AtivoCarteira ativoCarteira = carteira.getAtivos().get(idAtivo);
        Ativo ativo = ativoService.buscarPorId(idAtivo);

        // Estamos comparando o preço médio do valor acumulado de todas as compras daquele ativo,
        // com quanto seria a mesma quantidade sob o preço atual para decidir desempenho e imposto.
        double precoMedio = ativoCarteira.getValorAcumulado() / ativoCarteira.getQuantidadeAcumulada();
        double valorVenda = ativo.getValor() * quantidade;
        double custo = precoMedio * quantidade;
        double lucro = valorVenda - custo;

        if (lucro <= 0) return 0.0;

        return calcularImpostoTipoAtivo(ativo, lucro);
    }


    private double calcularImpostoTipoAtivo(Ativo ativo, double lucro) {
        if (lucro <= 0) return 0.0;

        return ativo.getTipo().calcularImposto(lucro);
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
