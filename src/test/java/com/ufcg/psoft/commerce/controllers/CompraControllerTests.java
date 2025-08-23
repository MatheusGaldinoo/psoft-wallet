package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.carteira.CarteiraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.exceptions.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.carteira.AtivoCarteira;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.usuario.Administrador;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.*;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ufcg.psoft.commerce.enums.TipoPlano.NORMAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Compra")
public class CompraControllerTests {

    final String URI_CLIENTES = "/clientes";
    final String URI_COMPRAS = "/compras";
    final String CODIGO_ACESSO_VALIDO = "123456";

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
        compraRepository.deleteAll();
        clienteRepository.deleteAll();

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
                            .balanco(200.0)
                            .build();
        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco("Avenida Paris, 5987, Campina Grande - PB")
                        .codigoAcesso(CODIGO_ACESSO_VALIDO)
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
        compraRepository.deleteAll();
    }


    @Nested
    @Transactional
    class AprovarCompra {

        private final PrintStream originalOut = System.out;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        @DisplayName("Quando o admin aprova uma compra com sucesso.")
        void aprovarCompraComSucessoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();

            System.out.println(ativoRepository.findAll());
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                        post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            String responseJsonStringAprovacao = driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOAprovacao = objectMapper.readValue(responseJsonStringAprovacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.DISPONIVEL, compraResponseDTOAprovacao.getEstado());
            assertEquals(2.0, compraResponseDTOAprovacao.getValorTotal());
            String output = outContent.toString();
            assertTrue(output.contains(
                    String.format("User: %s\nAlerta: Sua compra do ativo '%s' foi aprovada",
                            cliente.getNome(),
                            ativo.getNome())));

        }

        @Test
        @DisplayName("Quando o admin com acesso invalido tenta aprovar uma compra.")
        void aprovarCompraComAcessoInvalidoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "000000"))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @DisplayName("Quando o admin tenta aprovar uma compra inexistente.")
        void aprovarCompraInexistente() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + 123L + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Quando o admin tenta aprovar uma compra que não tem estado SOLICITADO.")
        void aprovarCompraForaDoEstadoSolicitado() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isForbidden());

        }

        @Test
        @DisplayName("Quando o admin tenta aprovar uma compra de um cliente com balanço insuficiente.")
        void aprovarCompraComBalancoInsuficiente() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            cliente.getCarteira().setBalanco(0);
            clienteRepository.save(cliente);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isUnprocessableEntity());

        }

    }

    @Nested
    @Transactional
    class RecusarCompra {

        private final PrintStream originalOut = System.out;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        @DisplayName("Quando o admin recusa uma compra com sucesso.")
        void recusarCompraComSucessoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();

            System.out.println(ativoRepository.findAll());
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            String responseJsonStringAprovacao = driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/recusar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOAprovacao = objectMapper.readValue(responseJsonStringAprovacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOAprovacao.getEstado());
            assertEquals(2.0, compraResponseDTOAprovacao.getValorTotal());
            String output = outContent.toString();
            assertTrue(output.contains(
                    String.format("User: %s\nAlerta: Sua compra do ativo '%s' foi recusada",
                            cliente.getNome(),
                            ativo.getNome())));

        }

        @Test
        @DisplayName("Quando o admin com acesso invalido tenta recusar uma compra.")
        void recusarCompraComAcessoInvalidoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/recusar")
                                    .param("codigoAcesso", "000000"))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @DisplayName("Quando o admin tenta recusar uma compra inexistente.")
        void recusarCompraInexistente() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + 123L + "/recusar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isNotFound());

        }

    }

}
