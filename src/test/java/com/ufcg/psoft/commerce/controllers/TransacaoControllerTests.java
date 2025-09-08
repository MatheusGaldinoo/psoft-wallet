package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoCotacaoRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.exceptions.CustomErrorType;
import com.ufcg.psoft.commerce.exceptions.ErrorHandlingControllerAdvice;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Transações")
public class TransacaoControllerTests {

    final String URI_COMPRAS_CLIENTES = "/clientes";

    final String URI_COMPRAS = "/compras";
    final String URI_CLIENTES = "/usuarios";
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
    ResgateRepository resgateRepository;

    @Autowired
    ModelMapper modelMapper;

    ObjectMapper objectMapper = new ObjectMapper();


    List<Ativo> ativos;
    List<Cliente> clientes;
    List<AtivoPostPutRequestDTO> ativosPostPutRequestDTO;

    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;

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

    private void inicializarCarteiraComAtivo(Cliente cliente, Ativo ativo, double quantidade) {
        if (cliente.getCarteira().getAtivos() == null) {
            cliente.getCarteira().setAtivos(new HashMap<>());
        }

        AtivoCarteira ativoCarteira = AtivoCarteira.builder()
                .quantidade(quantidade)
                .valorAcumulado(ativo.getValor())
                .quantidadeAcumulada(quantidade)
                .build();

        cliente.getCarteira().getAtivos().put(ativo.getId(), ativoCarteira);
        clienteRepository.save(cliente);
    }

    @AfterEach
    void tearDown() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();
        compraRepository.deleteAll();
        resgateRepository.deleteAll();
    }

    @Nested
    @DisplayName("Cliente lista suas transações - operações")
    class ClienteListaTransacoes {
        @Test
        @DisplayName("Cliente lista todas transações")
        void clienteListaTodasTransacoesTest() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            String compraResponse = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTO = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/transacoes")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> transacoesRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<TransacaoResponseDTO>>() { }
            );

            assertEquals(1, transacoesRetornadas.size());
            assertEquals(transacoesRetornadas.get(0).getCompra().getId(), compraResponseDTO.getId());


        }

        @Test
        @DisplayName("Cliente lista transações por tipo (COMPRA OU RESGATE)")
        void clienteListaTransacoesPorTipoTest() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            String compraResponse = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            driver.perform(
                            patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraResponseDTOSolicitacao.getId())
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraPostPutRequestDTO compra2 = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson2 = objectMapper.writeValueAsString(compra);

            String compraResponse2 = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson2))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao2 = objectMapper.readValue(compraResponse2, CompraResponseDTO.class);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String jsonRequest = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andDo(print());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/transacoes")
                                    .param("codigoAcesso", "123456")
                                    .param("tipoOperacao", "COMPRA"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();


            List<TransacaoResponseDTO> transacoesRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<TransacaoResponseDTO>>() { }
            );

            assertEquals(2, transacoesRetornadas.size());
            assertEquals(transacoesRetornadas.get(0).getCompra().getId(), compraResponseDTOSolicitacao2.getId());
            assertEquals(transacoesRetornadas.get(1).getCompra().getId(), compraResponseDTOSolicitacao.getId());
        }

        @Test
        @DisplayName("Cliente lista transações por status (COMPRA - solicitado, disponivel, em_carteira)")
        void clienteListaTransacoesPorStatusCompraTest() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            String compraResponse = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            driver.perform(
                            patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraResponseDTOSolicitacao.getId())
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraPostPutRequestDTO compra2 = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson2 = objectMapper.writeValueAsString(compra);

            String compraResponse2 = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson2))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao2 = objectMapper.readValue(compraResponse2, CompraResponseDTO.class);

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/transacoes")
                                    .param("codigoAcesso", "123456")
                                    .param("statusCompra", "SOLICITADO")
                                    .param("tipoOperacao", "COMPRA"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String jsonRequest = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andDo(print());

            List<TransacaoResponseDTO> transacoesRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<TransacaoResponseDTO>>() { }
            );

            assertEquals(1, transacoesRetornadas.size());
            assertEquals(transacoesRetornadas.get(0).getCompra().getId(), compraResponseDTOSolicitacao2.getId());
        }

        @Test
        @DisplayName("Cliente lista transações por periodo")
        void clienteListaTransacoesPorPeriodoCompraTest() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            String compraResponse = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            driver.perform(
                            patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraResponseDTOSolicitacao.getId())
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraPostPutRequestDTO compra2 = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson2 = objectMapper.writeValueAsString(compra);

            String compraResponse2 = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson2))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao2 = objectMapper.readValue(compraResponse2, CompraResponseDTO.class);

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/transacoes")
                                    .param("codigoAcesso", "123456")
                                    .param("statusCompra", "SOLICITADO")
                                    .param("tipoOperacao", "COMPRA"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String jsonRequest = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andDo(print());

            List<TransacaoResponseDTO> transacoesRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<TransacaoResponseDTO>>() { }
            );

            assertEquals(1, transacoesRetornadas.size());
            assertEquals(transacoesRetornadas.get(0).getCompra().getId(), compraResponseDTOSolicitacao2.getId());
        }
    }
}
