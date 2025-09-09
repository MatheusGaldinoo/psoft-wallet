package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
        @Transactional
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
        @Transactional
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
            LocalDateTime dataSolicitacao = compraResponseDTOSolicitacao.getDataSolicitacao();

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

            String compraJson2 = objectMapper.writeValueAsString(compra2);

            String compraResponse2 = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson2))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao2 = objectMapper.readValue(compraResponse2, CompraResponseDTO.class);
            LocalDateTime dataSolicitacao2 = compraResponseDTOSolicitacao2.getDataSolicitacao();

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

            Compra compraAntiga = compraRepository.findById(compraResponseDTOSolicitacao2.getId()).orElse(null);

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/transacoes")
                                    .param("codigoAcesso", "123456")
                                    .param("dataInicio", dataSolicitacao2.toString())
                                    .param("dataFim", dataSolicitacao2.toString()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();


            List<TransacaoResponseDTO> transacoesRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<TransacaoResponseDTO>>() { }
            );

            assertEquals(0, transacoesRetornadas.size());
        }
    }

    @Nested
    @DisplayName("US20 - Exportação de extrato CSV")
    class ClienteExportaExtratoCSV {

        @Test
        @DisplayName("Cliente exporta extrato com compras e resgates")
        void clienteExportaExtratoComTransacoes() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Criar uma compra
            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(compraJson))
                    .andExpect(status().isCreated());

            // Criar um resgate
            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO resgate = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String resgateJson = objectMapper.writeValueAsString(resgate);

            driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(resgateJson))
                    .andExpect(status().isCreated());

            // Exportar CSV
            String csvResponse = driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            containsString("extrato_" + cliente.getId() + ".csv")))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertTrue(csvResponse.contains("Tipo,Ativo,Quantidade,Imposto,Valor,DataSolicitacao,DataFinalizacao"));
            assertTrue(csvResponse.contains("COMPRA"));
            assertTrue(csvResponse.contains("RESGATE"));
            assertTrue(csvResponse.contains(String.valueOf(ativo.getId())));
        }

        @Test
        @DisplayName("Cliente exporta extrato sem transações")
        void clienteExportaExtratoSemTransacoes() throws Exception {
            Cliente cliente = clientes.get(1);

            String csvResponse = driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Apenas o cabeçalho deve estar presente
            assertEquals("Tipo,Ativo,Quantidade,Imposto,Valor,DataSolicitacao,DataFinalizacao\n", csvResponse);
        }

        @Test
        @DisplayName("Cliente exporta extrato com código de acesso inválido")
        void clienteExportaExtratoCodigoAcessoInvalido() throws Exception {
            Cliente cliente = clientes.get(0);

            driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", CODIGO_ACESSO_INVALIDO))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Verifica conteúdo do CSV - valores e impostos")
        void clienteExportaExtratoConteudoCSV() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(3)
                    .build();

            String compraResponse = driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(compra)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            CompraResponseDTO compraDTO = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

            driver.perform(patch("/compras/" + compraDTO.getId() + "/aprovar")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk());

            driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraDTO.getId())
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk());

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO resgate = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();

            driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resgate)))
                    .andExpect(status().isCreated());

            String csvResponse = driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String[] linhas = csvResponse.split("\\r?\\n"); // Suporta \n ou \r\n

            assertEquals(3, linhas.length); // Cabeçalho + 2 transações

            boolean hasCompra = false;
            boolean hasResgate = false;

            for (int i = 1; i < linhas.length; i++) { // começa em 1 para pular cabeçalho
                String linha = linhas[i].trim();
                if (linha.startsWith("COMPRA")) {
                    hasCompra = true;
                    assertTrue(linha.contains(String.valueOf(ativo.getId())), "Compra deve conter ID do ativo");
                    assertTrue(linha.contains("0"), "Compra não deve ter imposto");
                }
                if (linha.startsWith("RESGATE")) {
                    hasResgate = true;
                    assertTrue(linha.contains(String.valueOf(ativo.getId())), "Resgate deve conter ID do ativo");
                    assertTrue(linha.contains("2.0") || linha.contains("2"), "Resgate deve conter quantidade correta");
                }
            }

            assertTrue(hasCompra, "CSV deve conter transação do tipo COMPRA");
            assertTrue(hasResgate, "CSV deve conter transação do tipo RESGATE");
        }

        @Test
        @DisplayName("Cliente exporta extrato com apenas compras")
        void clienteExportaExtratoApenasCompras() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();

            String compraResponse = driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(compra)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraDTO = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

            driver.perform(patch("/compras/" + compraDTO.getId() + "/aprovar")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk());

            driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraDTO.getId())
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk());

            String csvResponse = driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(2, linhas.length); // Cabeçalho + 1 compra
            assertTrue(linhas[1].startsWith("COMPRA"));
        }

        @Test
        @DisplayName("Cliente exporta extrato com apenas resgates")
        void clienteExportaExtratoApenasResgates() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);
            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO resgate = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(3)
                    .build();

            driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resgate)))
                    .andExpect(status().isCreated());

            String csvResponse = driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(2, linhas.length); // Cabeçalho + 1 resgate
            assertTrue(linhas[1].startsWith("RESGATE"));
        }

        @Test
        @DisplayName("Cliente exporta extrato com múltiplas transações do mesmo tipo")
        void clienteExportaExtratoMultiplasComprasResgates() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Inicializa carteira
            inicializarCarteiraComAtivo(cliente, ativo, 10);

            // Cria duas compras
            for (int i = 0; i < 2; i++) {
                CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                        .codigoAcesso(cliente.getCodigoAcesso())
                        .idAtivo(ativo.getId())
                        .quantidade(1 + i)
                        .build();

                String compraResponse = driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(compra)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse().getContentAsString();

                CompraResponseDTO compraDTO = objectMapper.readValue(compraResponse, CompraResponseDTO.class);

                driver.perform(patch("/compras/" + compraDTO.getId() + "/aprovar")
                                .param("codigoAcesso", cliente.getCodigoAcesso()))
                        .andExpect(status().isOk());

                driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraDTO.getId())
                                .param("codigoAcesso", cliente.getCodigoAcesso()))
                        .andExpect(status().isOk());
            }

            // Cria dois resgates
            for (int i = 0; i < 2; i++) {
                ResgatePostPutRequestDTO resgate = ResgatePostPutRequestDTO.builder()
                        .idAtivo(ativo.getId())
                        .quantidade(1 + i)
                        .build();

                driver.perform(post("/clientes/" + cliente.getId() + "/resgate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resgate)))
                        .andExpect(status().isCreated());
            }

            String csvResponse = driver.perform(get("/clientes/" + cliente.getId() + "/extrato")
                            .param("codigoAcesso", cliente.getCodigoAcesso()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(5, linhas.length); // Cabeçalho + 4 transações
        }
    }
}
