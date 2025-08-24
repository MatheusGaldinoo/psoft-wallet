package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.carteira.CarteiraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.carteira.AtivoCarteira;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import com.ufcg.psoft.commerce.models.usuario.Administrador;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.*;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.carteira.CarteiraService;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ufcg.psoft.commerce.enums.TipoPlano.NORMAL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Compra")
public class CompraControllerTests {

    final String URI_CLIENTES = "/usuario";
    final String CODIGO_ACESSO_VALIDO = "123456";
    final String CODIGO_ACESSO_INVALIDO = "000000";

    @Autowired
    MockMvc driver;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;

    @Autowired
    AdministradorRepository administradorRepository;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    CarteiraService carteiraService;

    @Autowired
    ModelMapper modelMapper;

    ObjectMapper objectMapper = new ObjectMapper();


    List<Ativo> ativos;
    List<Cliente> clientes;
    List<AtivoPostPutRequestDTO> ativosPostPutRequestDTO;

    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;
    private AtivoService ativoService;

    @BeforeEach
    void setup() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();

        ativos = new ArrayList<>();
        clientes = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());

        administradorRepository.save(Administrador.builder()
                .nome("Admin")
                .codigoAcesso("123456")
                .build());


        tesouro = tipoDeAtivoRepository.save(new TesouroDireto());
        cripto = tipoDeAtivoRepository.save(new CriptoMoeda());
        acao = tipoDeAtivoRepository.save(new Acao());

        for (int i = 1; i <= 3; i++) {
            criarAtivo("Ativo" + i, tesouro, 1.0 * i, StatusDisponibilidade.DISPONIVEL);
            criarCliente("Cliente" + i, (i % 2 == 0 ? TipoPlano.NORMAL : TipoPlano.PREMIUM));
        }
        for (int i = 4; i <= 7; i++) {
            criarAtivo("Ativo" + i, cripto, 1.0 * i, StatusDisponibilidade.DISPONIVEL);
        }
        for (int i = 8; i <= 10; i++) {
            criarAtivo("Ativo" + i, acao, 1.0 * i, StatusDisponibilidade.INDISPONIVEL);
        }

    }

    private void criarCliente(String nome, TipoPlano tipo) {
        Carteira carteira = Carteira.builder()
                .balanco(200.00)
                .build();

        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco("Avenida Paris, 5987, Campina Grande - PB")
                        .codigoAcesso("123456")
                        .plano(tipo)
                        .carteira(carteira)
                        .build());
        clientes.add(cliente);
    }

    private void criarAtivo(String nome, TipoDeAtivo tipo, double valor, StatusDisponibilidade status) {
        Ativo ativo = ativoRepository.save(Ativo.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao(nome)
                .valor(valor)
                .interessadosCotacao(new ArrayList<>())
                .interessadosDisponibilidade(new ArrayList<>())
                .statusDisponibilidade(status)
                .build()
        );
        ativos.add(ativo);

        AtivoPostPutRequestDTO dto = AtivoPostPutRequestDTO.builder()
                .nome(ativo.getNome())
                .descricao(ativo.getDescricao())
                .valor(ativo.getValor())
                .tipo(ativo.getTipo().getNomeTipo())
                .statusDisponibilidade(ativo.getStatusDisponibilidade())
                .build();
        ativosPostPutRequestDTO.add(dto);
    }

    @AfterEach
    void tearDown() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();
        clienteRepository.deleteAll();
    }


//    @Nested
//    class SolicitarCompra {
//
//        @Test
//        @DisplayName("Quando o cliente solicita uma compra com sucesso.")
//        void solicitacarCompraComSucessoTest() throws Exception {
//
//            Cliente cliente = clienteRepository.getReferenceById(1L);
//            String responseJsonString = driver.perform(post(URI_CLIENTES + "/" + cliente.getId()))
//                    .andExpect(status().isOk())
//                    .andDo(print())
//                    .andReturn().getResponse().getContentAsString();
//        }
//
//    }

    @Nested
    @DisplayName("US12 - Confirmar execução de compra")
    class ConfirmarCompra {

        @Test
        @DisplayName("Deve confirmar compra com sucesso e mover estado de DISPONIVEL → COMPRADO → EM_CARTEIRA")
        void confirmarCompraComSucesso() throws Exception {
            Cliente cliente = clientes.get(0);
            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(2.0)
                            .precoUnitario(10.0)
                            .valorTotal(20.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/finalizar/" + compra.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.estadoAtual").value("EM_CARTEIRA"));
        }

        @Test
        @DisplayName("Não deve confirmar compra inexistente")
        void confirmarCompraInexistente() throws Exception {
            Cliente cliente = clientes.get(0);

            driver.perform(patch("/clientes/" + cliente.getId() + "/finalizar/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Não deve confirmar compra que não pertence ao cliente")
        void confirmarCompraOutroCliente() throws Exception {
            Cliente cliente1 = clientes.get(0);
            Cliente cliente2 = clientes.get(1);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente2.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(1.0)
                            .precoUnitario(5.0)
                            .valorTotal(5.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente1.getId() + "/finalizar/" + compra.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não deve confirmar compra em estado inválido (ex.: já comprada)")
        void confirmarCompraEstadoInvalido() throws Exception {
            Cliente cliente = clientes.get(0);

            // Garantir saldo suficiente para a compra
            carteiraService.adicionarBalanco(cliente.getId(), 100.0); // ou o suficiente para cobrir a compra

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(1.0)
                            .precoUnitario(5.0)
                            .valorTotal(5.0)
                            .estadoAtual(EstadoCompra.COMPRADO)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/finalizar/" + compra.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve confirmar compra se o saldo for exatamente igual ao valor da compra")
        void confirmarCompraSaldoExato() throws Exception {
            Cliente cliente = clientes.get(0);
            cliente.getCarteira().setBalanco(20.0);
            clienteRepository.save(cliente);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(2.0)
                            .precoUnitario(10.0)
                            .valorTotal(20.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/finalizar/" + compra.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estadoAtual").value("EM_CARTEIRA"));
        }

        @Test
        @DisplayName("Não deve confirmar compra se o cliente não tiver saldo suficiente")
        void confirmarCompraSaldoInsuficiente() throws Exception {
            Cliente cliente = clientes.get(0);
            cliente.getCarteira().setBalanco(1.0);
            clienteRepository.save(cliente);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(10.0)
                            .precoUnitario(100.0)
                            .valorTotal(1000.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/finalizar/" + compra.getId()))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Test
    @DisplayName("Não deve confirmar compra em estado SOLICITADO")
    void confirmarCompraSolicitado() throws Exception {
        Cliente cliente = clientes.get(0);

        Compra compra = compraRepository.save(
                Compra.builder()
                        .idCliente(cliente.getId())
                        .idAtivo(ativos.get(0).getId())
                        .quantidade(1.0)
                        .precoUnitario(5.0)
                        .valorTotal(5.0)
                        .estadoAtual(EstadoCompra.SOLICITADO)
                        .build()
        );

        driver.perform(patch("/clientes/" + cliente.getId() + "/finalizar/" + compra.getId()))
                .andExpect(status().isBadRequest());
    }
}