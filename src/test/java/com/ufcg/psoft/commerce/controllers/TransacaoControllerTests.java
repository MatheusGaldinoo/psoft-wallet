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
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
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
import com.ufcg.psoft.commerce.models.transacao.Resgate;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    final String URI_TRANSACOES = "/transacoes";
    final String URI_COMPRAS = "/compras";
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

    List<Compra> compras;
    List<Resgate> resgates;

    List<Ativo> ativos;
    List<Cliente> clientes;
    List<AtivoPostPutRequestDTO> ativosPostPutRequestDTO;

    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;

    @BeforeEach
    void setup() {
        // limpa o banco
        resgateRepository.deleteAll();
        compraRepository.deleteAll();
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        clienteRepository.deleteAll();
        administradorRepository.deleteAll();

        ativos = new ArrayList<>();
        clientes = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());

        compras = new ArrayList<>();
        resgates = new ArrayList<>();

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
            criarCliente("Cliente" + i, (i % 2 == 0 ? TipoPlano.NORMAL : TipoPlano.PREMIUM));
        }
        for (int i = 8; i <= 10; i++) {
            criarAtivo("Ativo" + i, acao, 1.0 * i, StatusDisponibilidade.DISPONIVEL);
            criarCliente("Cliente" + i, (i % 2 == 0 ? TipoPlano.NORMAL : TipoPlano.PREMIUM));
        }

    }

    // cria e salva exemplos de compras e resgates
    private void criarTransacoes() {
        for (int i = 0; i <= 9; i++) {
            Compra compra = Compra.builder()
                    .idCliente(clientes.get(i).getId())
                    .idAtivo(ativos.get(i).getId())
                    .quantidade(i + 1.0)
                    .precoUnitario(100.0 * (i + 1))
                    .valorTotal((i + 1.0) * 100.0 * (i + 1))
                    .dataSolicitacao(LocalDateTime.of(2025, 9, 8, 10 + i, 0))
                    .build();

            compras.add(compraRepository.save(compra));

            Resgate resgate = Resgate.builder()
                    .idCliente(clientes.get(i).getId())
                    .idAtivo(ativos.get(i).getId())
                    .quantidade((i + 1) * 2.0)
                    .precoUnitario(100.0 * (i + 1))
                    .valorTotal((i + 1) * 200.0)
                    .dataSolicitacao(LocalDateTime.of(2025, 9, 8, 13 + i, 0))
                    .imposto(10.0)
                    .build();

            resgates.add(resgateRepository.save(resgate));
        }
    }

    private List<TransacaoResponseDTO> mapTransacoesEsperadas(List<Compra> compras, List<Resgate> resgates) {
        List<TransacaoResponseDTO> list = new ArrayList<>();

        // Mapeia compras
        for (Compra c : compras) {
            list.add(TransacaoResponseDTO.builder()
                    .compra(modelMapper.map(c, CompraResponseDTO.class))
                    .build());
        }

        // Mapeia resgates
        for (Resgate r : resgates) {
            list.add(TransacaoResponseDTO.builder()
                    .resgate(modelMapper.map(r, ResgateResponseDTO.class))
                    .build());
        }

        // Ordena DESC por data de solicitação
        // Caso datas sejam iguais, compras aparecem antes de resgates
        list.sort(Comparator.comparing(
                (TransacaoResponseDTO t) -> t.getCompra() != null ? t.getCompra().getDataSolicitacao() : t.getResgate().getDataSolicitacao(),
                Comparator.reverseOrder()
        ).thenComparing(t -> t.getCompra() != null ? 0 : 1));

        return list;
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
        clienteRepository.deleteAll();
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
    @DisplayName("GET /transacoes - Administrador pode consultar todas transações, filtrando ou não")
    class listarTransacoes {

        @Test
        @DisplayName("Deve retornar exceção quando o código do admin for inválido")
        void quandoCodigoAcessoInvalido() throws Exception {
            criarTransacoes();
            driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_INVALIDO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Codigo de acesso invalido!")));

        }

        @Test
        @DisplayName("Deve retornar uma lista vazia quando não tiver sido negociado nada ainda")
        void listarZeroTransacoes() throws Exception {
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            List<Compra> comprasEsperadas = compras;
            List<Resgate> resgatesEsperados = resgates;

            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista com todas as transações de compra e resgate")
        void listarTodasTransacoes() throws Exception {
//            criarTransacoes();
//            String jsonResponse = driver.perform(get(URI_TRANSACOES)
//                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andDo(print())
//                    .andReturn().getResponse().getContentAsString();
//
//            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});
//
//            List<Compra> comprasEsperadas = compras;
//            List<Resgate> resgatesEsperados = resgates;
//
//            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);
//
//            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoAtivo == TesouroDireto")
        void quandoFiltrarPorTesouroDireto() throws Exception {
//            criarTransacoes();
//
//            String jsonResponse = driver.perform(get(URI_TRANSACOES)
//                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
//                            .param("tipoAtivo", "TESOURO_DIRETO")
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andDo(print())
//                    .andReturn().getResponse().getContentAsString();
//
//            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});
//
//            // Filtra compras/resgates que são do tipo TesouroDireto
//            List<Compra> comprasEsperadas = compras.stream()
//                    .filter(c -> {
//                        Ativo ativo = ativos.stream().filter(a -> a.getId().equals(c.getIdAtivo())).findFirst().orElse(null);
//                        return ativo != null && ativo.getTipo() instanceof TesouroDireto;
//                    })
//                    .toList();
//
//            List<Resgate> resgatesEsperados = resgates.stream()
//                    .filter(r -> {
//                        Ativo ativo = ativos.stream().filter(a -> a.getId().equals(r.getIdAtivo())).findFirst().orElse(null);
//                        return ativo != null && ativo.getTipo() instanceof TesouroDireto;
//                    })
//                    .toList();
//
//            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);
//
//            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoAtivo == Acao")
        void quandoFiltrarPorAcao() throws Exception {

        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoAtivo == CriptoMoeda")
        void quandoFiltrarPorCriptomoeda() throws Exception {

        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas por TipoAtivo")
        void quandoFiltrarDataTime() throws Exception {

        }

        @Test
        @DisplayName("Deve retornar uma lista de transações feitas por um cliente")
        void quandoFiltrarCliente() throws Exception {
//            criarTransacoes();
//            Integer indexClient = 1;
//
//            Cliente clienteFiltrado = clientes.get(indexClient);
//
//            String jsonResponse = driver.perform(get(URI_TRANSACOES)
//                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
//                            .param("clientId", String.valueOf(clienteFiltrado.getId()))
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andDo(print())
//                    .andReturn().getResponse().getContentAsString();
//
//            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});
//
//            List<Compra> comprasEsperadas = List.of(compras.get(indexClient-1));
//            List<Resgate> resgatesEsperados = List.of(resgates.get(indexClient-1));
//
//            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);
//            //List<TransacaoResponseDTO> result = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);
//            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoOperação == compra")
        void quandoFiltrarPorCompra() throws Exception {
            criarTransacoes();

            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoOperacao", "compra")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            List<Compra> comprasEsperadas = compras;
            List<Resgate> resgatesEsperados = List.of();

            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoOperação == resgate")
        void quandoFiltrarPorResgate() throws Exception {
            criarTransacoes();

            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoOperacao", "resgate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            List<Compra> comprasEsperadas = List.of();
            List<Resgate> resgatesEsperados = resgates;

            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

    }

}

