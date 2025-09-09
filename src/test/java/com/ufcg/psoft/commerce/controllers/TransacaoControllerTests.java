package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.AtualizarStatusTransacaoDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.dtos.extrato.ExportarExtratoDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import com.ufcg.psoft.commerce.dtos.transacao.TransacaoResponseDTO;
import com.ufcg.psoft.commerce.enums.DecisaoAdministrador;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoPlano;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Transações")
public class TransacaoControllerTests {

    // Constantes
    private static final String URI_COMPRAS_CLIENTES = "/clientes";
    private static final String URI_TRANSACOES = "/transacoes";
    private static final String URI_COMPRAS = "/compras";
    private static final String URI_RESGATE = "/resgate";
    private static final String URI_EXTRATO = "/extrato";
    private static final String CODIGO_ACESSO_VALIDO = "123456";
    private static final String CODIGO_ACESSO_INVALIDO = "000000";
    private static final String ENDERECO_PADRAO = "Avenida Paris, 5987, Campina Grande - PB";
    private static final double BALANCO_INICIAL = 200.00;
    private static final double IMPOSTO_PADRAO = 10.0;
    private static final int QUANTIDADE_PADRAO = 1;
    private static final String CABECALHO_CSV = "Tipo,Ativo,Quantidade,Imposto,Valor,DataSolicitacao,DataFinalizacao";

    @Autowired MockMvc driver;
    @Autowired AtivoRepository ativoRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired TipoDeAtivoRepository tipoDeAtivoRepository;
    @Autowired AdministradorRepository administradorRepository;
    @Autowired CompraRepository compraRepository;
    @Autowired ResgateRepository resgateRepository;
    @Autowired ModelMapper modelMapper;

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
        limparBancoDados();
        inicializarColecoes();
        criarAdministrador();
        criarTiposDeAtivo();
        criarAtivosEClientes();
    }

    @AfterEach
    void tearDown() {
        limparBancoDados();
    }

    // Métodos de configuração
    private void limparBancoDados() {
        resgateRepository.deleteAll();
        compraRepository.deleteAll();
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        clienteRepository.deleteAll();
        administradorRepository.deleteAll();
    }

    private void inicializarColecoes() {
        ativos = new ArrayList<>();
        clientes = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();
        compras = new ArrayList<>();
        resgates = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private void criarAdministrador() {
        administradorRepository.save(Administrador.builder()
                .nome("Admin")
                .codigoAcesso(CODIGO_ACESSO_VALIDO)
                .build());
    }

    private void criarTiposDeAtivo() {
        tesouro = tipoDeAtivoRepository.save(new TesouroDireto());
        cripto = tipoDeAtivoRepository.save(new CriptoMoeda());
        acao = tipoDeAtivoRepository.save(new Acao());
    }

    private void criarAtivosEClientes() {
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

    private void criarCliente(String nome, TipoPlano tipo) {
        Carteira carteira = Carteira.builder()
                .balanco(BALANCO_INICIAL)
                .build();

        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco(ENDERECO_PADRAO)
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

    // Métodos auxiliares para transações
    private void criarTransacoes() {
        for (int i = 0; i <= 9; i++) {
            Compra compra = Compra.builder()
                    .idCliente(clientes.get(i).getId())
                    .idAtivo(ativos.get(i).getId())
                    .quantidade(i + 1.0)
                    .precoUnitario(100.0 * (i + 1))
                    .tipoAtivo(ativos.get(i).getTipo().getNomeTipo())
                    .valorTotal((i + 1.0) * 100.0 * (i + 1))
                    .dataSolicitacao(LocalDateTime.of(2025, 9, 8, 1 + i, 0))
                    .dataFinalizacao(LocalDateTime.of(2025, 9, 8, 13 + i, 0))
                    .build();

            compras.add(compraRepository.save(compra));

            Resgate resgate = Resgate.builder()
                    .idCliente(clientes.get(i).getId())
                    .idAtivo(ativos.get(i).getId())
                    .quantidade((i + 1) * 2.0)
                    .precoUnitario(100.0 * (i + 1))
                    .tipoAtivo(ativos.get(i).getTipo().getNomeTipo())
                    .valorTotal((i + 1) * 200.0)
                    .dataSolicitacao(LocalDateTime.of(2025, 9, 8, 1 + i, 0))
                    .dataFinalizacao(LocalDateTime.of(2025, 9, 8, 13 + i, 0))
                    .imposto(IMPOSTO_PADRAO)
                    .build();

            resgates.add(resgateRepository.save(resgate));
        }
    }

    private List<TransacaoResponseDTO> mapTransacoesEsperadas(List<Compra> compras, List<Resgate> resgates) {
        List<TransacaoResponseDTO> todasTransacoes = new ArrayList<>();

        // Mapeia compras
        for (Compra c : compras) {
            todasTransacoes.add(
                    TransacaoResponseDTO.builder()
                            .compra(modelMapper.map(c, CompraResponseDTO.class))
                            .build()
            );
        }

        // Mapeia resgates
        for (Resgate r : resgates) {
            todasTransacoes.add(
                    TransacaoResponseDTO.builder()
                            .resgate(modelMapper.map(r, ResgateResponseDTO.class))
                            .build()
            );
        }

        // Ordena: primeiro resgates DESC por data, depois compras DESC por data
        todasTransacoes.sort((t1, t2) -> {
            boolean t1Resgate = t1.getResgate() != null;
            boolean t2Resgate = t2.getResgate() != null;

            if (t1Resgate && !t2Resgate) return -1;
            if (!t1Resgate && t2Resgate) return 1;

            LocalDateTime dt1 = t1Resgate ? t1.getResgate().getDataSolicitacao() : t1.getCompra().getDataSolicitacao();
            LocalDateTime dt2 = t2Resgate ? t2.getResgate().getDataSolicitacao() : t2.getCompra().getDataSolicitacao();

            return dt2.compareTo(dt1);
        });

        return todasTransacoes;
    }

    // Métodos auxiliares para operações HTTP
    private CompraResponseDTO criarCompra(Cliente cliente, Ativo ativo, int quantidade) throws Exception {
        CompraPostPutRequestDTO compraRequest = CompraPostPutRequestDTO.builder()
                .codigoAcesso(cliente.getCodigoAcesso())
                .idAtivo(ativo.getId())
                .quantidade(quantidade)
                .build();

        String response = driver.perform(
                        post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, CompraResponseDTO.class);
    }

    private CompraResponseDTO criarCompra(Cliente cliente, Ativo ativo) throws Exception {
        return criarCompra(cliente, ativo, QUANTIDADE_PADRAO);
    }

    private void aprovarCompra(Long compraId) throws Exception {
        AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                .estado(DecisaoAdministrador.APROVADO)
                .codigoAcesso(CODIGO_ACESSO_VALIDO)
                .build();

        String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

        driver.perform(
                        patch(URI_COMPRAS + "/" + compraId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(aprovacaoJson))
                .andExpect(status().isOk());
    }

    private void finalizarCompra(Long clienteId, Long compraId) throws Exception {
        driver.perform(
                        patch(URI_COMPRAS_CLIENTES + "/" + clienteId + "/" + compraId)
                                .param("codigoAcesso", CODIGO_ACESSO_VALIDO))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private CompraResponseDTO criarEProcessarCompraCompleta(Cliente cliente, Ativo ativo) throws Exception {
        CompraResponseDTO compra = criarCompra(cliente, ativo);
        aprovarCompra(compra.getId());
        finalizarCompra(cliente.getId(), compra.getId());
        return compra;
    }

    private void criarResgate(Cliente cliente, Ativo ativo, int quantidade) throws Exception {
        ResgatePostPutRequestDTO resgateRequest = ResgatePostPutRequestDTO.builder()
                .idAtivo(ativo.getId())
                .quantidade(quantidade)
                .build();

        driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + URI_RESGATE)
                        .content(objectMapper.writeValueAsString(resgateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    private void criarResgate(Cliente cliente, Ativo ativo) throws Exception {
        criarResgate(cliente, ativo, QUANTIDADE_PADRAO);
    }

    private List<TransacaoResponseDTO> listarTransacoes(Long clienteId) throws Exception {
        String response = driver.perform(
                        get(URI_COMPRAS_CLIENTES + "/" + clienteId + "/transacoes")
                                .param("codigoAcesso", CODIGO_ACESSO_VALIDO))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, new TypeReference<List<TransacaoResponseDTO>>() {});
    }

    private List<TransacaoResponseDTO> listarTransacoesPorTipo(Long clienteId, String tipoOperacao) throws Exception {
        String response = driver.perform(
                        get(URI_COMPRAS_CLIENTES + "/" + clienteId + "/transacoes")
                                .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                .param("tipoOperacao", tipoOperacao))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, new TypeReference<List<TransacaoResponseDTO>>() {});
    }

    private List<TransacaoResponseDTO> listarTransacoesPorStatusETipo(Long clienteId, String statusCompra, String tipoOperacao) throws Exception {
        String response = driver.perform(
                        get(URI_COMPRAS_CLIENTES + "/" + clienteId + "/transacoes")
                                .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                .param("statusCompra", statusCompra)
                                .param("tipoOperacao", tipoOperacao))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, new TypeReference<List<TransacaoResponseDTO>>() {});
    }

    private List<TransacaoResponseDTO> listarTransacoesPorPeriodo(Long clienteId, LocalDateTime dataInicio, LocalDateTime dataFim) throws Exception {
        String response = driver.perform(
                        get(URI_COMPRAS_CLIENTES + "/" + clienteId + "/transacoes")
                                .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                .param("dataInicio", dataInicio.toString())
                                .param("dataFim", dataFim.toString()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, new TypeReference<List<TransacaoResponseDTO>>() {});
    }

    private String exportarExtrato(Long clienteId, String codigoAcesso) throws Exception {
        ExportarExtratoDTO dto = ExportarExtratoDTO.builder()
                .codigoAcesso(codigoAcesso)
                .transacoes(listarTransacoes(clienteId))
                .build();
        String json = objectMapper.writeValueAsString(dto);
        return driver.perform(get(URI_COMPRAS_CLIENTES + "/" + clienteId + URI_EXTRATO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Nested
    @DisplayName("Cliente lista suas transações - operações")
    class ClienteListaTransacoes {

        @Test
        @DisplayName("Cliente lista todas transações")
        void clienteListaTodasTransacoesTest() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            CompraResponseDTO compraResponse = criarCompra(cliente, ativo);
            List<TransacaoResponseDTO> transacoes = listarTransacoes(cliente.getId());

            // Assert
            assertEquals(1, transacoes.size());
            assertEquals(compraResponse.getId(), transacoes.get(0).getCompra().getId());
        }

        @Test
        @Transactional
        @DisplayName("Cliente lista transações por tipo (COMPRA OU RESGATE)")
        void clienteListaTransacoesPorTipoTest() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            CompraResponseDTO compra1 = criarEProcessarCompraCompleta(cliente, ativo);
            CompraResponseDTO compra2 = criarCompra(cliente, ativo);
            criarResgate(cliente, ativo);

            List<TransacaoResponseDTO> transacoesCompra = listarTransacoesPorTipo(cliente.getId(), "COMPRA");

            // Assert
            assertEquals(2, transacoesCompra.size());
            assertEquals(compra2.getId(), transacoesCompra.get(0).getCompra().getId());
            assertEquals(compra1.getId(), transacoesCompra.get(1).getCompra().getId());
        }

        @Test
        @DisplayName("Cliente lista transações por status (COMPRA - solicitado, disponivel, em_carteira)")
        void clienteListaTransacoesPorStatusCompraTest() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            criarEProcessarCompraCompleta(cliente, ativo);
            CompraResponseDTO compra2 = criarCompra(cliente, ativo);
            criarResgate(cliente, ativo);

            List<TransacaoResponseDTO> transacoesSolicitadas = listarTransacoesPorStatusETipo(
                    cliente.getId(), "SOLICITADO", "COMPRA");

            // Assert
            assertEquals(1, transacoesSolicitadas.size());
            assertEquals(compra2.getId(), transacoesSolicitadas.get(0).getCompra().getId());
        }

        @Test
        @Transactional
        @DisplayName("Cliente lista transações por periodo")
        void clienteListaTransacoesPorPeriodoCompraTest() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            criarEProcessarCompraCompleta(cliente, ativo);
            CompraResponseDTO compra2 = criarCompra(cliente, ativo);
            LocalDateTime dataConsulta = compra2.getDataSolicitacao();
            criarResgate(cliente, ativo);

            List<TransacaoResponseDTO> transacoesPorPeriodo = listarTransacoesPorPeriodo(
                    cliente.getId(), dataConsulta, dataConsulta);

            // Assert
            assertEquals(0, transacoesPorPeriodo.size());
        }
    }

    @Nested
    @DisplayName("US20 - Exportação de extrato CSV")
    class ClienteExportaExtratoCSV {

        @Test
        @DisplayName("Cliente exporta extrato com compras e resgates")
        void clienteExportaExtratoComTransacoes() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            criarCompra(cliente, ativo, 2);
            inicializarCarteiraComAtivo(cliente, ativo, 5);
            criarResgate(cliente, ativo);

            String csvResponse = exportarExtrato(cliente.getId(), cliente.getCodigoAcesso());
            // Assert
            assertTrue(csvResponse.contains(CABECALHO_CSV));
            assertTrue(csvResponse.contains("COMPRA"));
            assertTrue(csvResponse.contains("RESGATE"));
            assertTrue(csvResponse.contains(String.valueOf(ativo.getId())));
        }

        @Test
        @DisplayName("Cliente exporta extrato sem transações")
        void clienteExportaExtratoSemTransacoes() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(1);

            // Act
            String csvResponse = exportarExtrato(cliente.getId(), cliente.getCodigoAcesso());

            // Assert
            assertEquals(CABECALHO_CSV + "\n", csvResponse);
        }

        @Test
        @DisplayName("Cliente exporta extrato com código de acesso inválido")
        void clienteExportaExtratoCodigoAcessoInvalido() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);

            // Act & Assert
            driver.perform(get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + URI_EXTRATO)
                            .param("codigoAcesso", CODIGO_ACESSO_INVALIDO))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Verifica conteúdo do CSV - valores e impostos")
        void clienteExportaExtratoConteudoCSV() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            CompraResponseDTO compraDTO = criarCompra(cliente, ativo, 3);
            aprovarCompra(compraDTO.getId());
            finalizarCompra(cliente.getId(), compraDTO.getId());

            inicializarCarteiraComAtivo(cliente, ativo, 5);
            criarResgate(cliente, ativo, 2);

            String csvResponse = exportarExtrato(cliente.getId(), cliente.getCodigoAcesso());

            // Assert
            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(3, linhas.length); // Cabeçalho + 2 transações

            boolean hasCompra = false;
            boolean hasResgate = false;

            for (int i = 1; i < linhas.length; i++) {
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
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            CompraResponseDTO compraDTO = criarCompra(cliente, ativo, 2);
            aprovarCompra(compraDTO.getId());
            finalizarCompra(cliente.getId(), compraDTO.getId());

            String csvResponse = exportarExtrato(cliente.getId(), cliente.getCodigoAcesso());

            // Assert
            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(2, linhas.length); // Cabeçalho + 1 compra
            assertTrue(linhas[1].startsWith("COMPRA"));
        }

        @Test
        @DisplayName("Cliente exporta extrato com apenas resgates")
        void clienteExportaExtratoApenasResgates() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            // Act
            inicializarCarteiraComAtivo(cliente, ativo, 5);
            criarResgate(cliente, ativo, 3);


            String csvResponse = exportarExtrato(cliente.getId(), cliente.getCodigoAcesso());

            // Assert
            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(2, linhas.length); // Cabeçalho + 1 resgate
            assertTrue(linhas[1].startsWith("RESGATE"));
        }

        @Test
        @DisplayName("Cliente exporta extrato com múltiplas transações do mesmo tipo")
        void clienteExportaExtratoMultiplasComprasResgates() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);
            inicializarCarteiraComAtivo(cliente, ativo, 10);

            // Act
            // Cria duas compras
            for (int i = 0; i < 2; i++) {
                CompraResponseDTO compraDTO = criarCompra(cliente, ativo, 1 + i);
                aprovarCompra(compraDTO.getId());
                finalizarCompra(cliente.getId(), compraDTO.getId());
            }

            // Cria dois resgates
            for (int i = 0; i < 2; i++) {
                criarResgate(cliente, ativo, 1 + i);
            }

            String csvResponse = exportarExtrato(cliente.getId(), cliente.getCodigoAcesso());

            // Assert
            String[] linhas = csvResponse.split("\\r?\\n");
            assertEquals(5, linhas.length); // Cabeçalho + 4 transações
        }
    }

    @Nested
    @DisplayName("GET /transacoes - Administrador pode consultar todas transações, filtrando ou não")
    class ListarTransacoes {

        @Test
        @DisplayName("Deve retornar exceção quando o código do admin for inválido")
        void quandoCodigoAcessoInvalido() throws Exception {
            // Arrange
            criarTransacoes();

            // Act & Assert
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
            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(compras, resgates);

            // Assert
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista com todas as transações de compra e resgate")
        void listarTodasTransacoes() throws Exception {
            // Arrange
            criarTransacoes();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(compras, resgates);

            // Assert
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoAtivo == TesouroDireto")
        void quandoFiltrarPorTesouroDireto() throws Exception {
            // Arrange
            criarTransacoes();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoAtivo", "TESOURO_DIRETO")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<Compra> comprasEsperadas = filtrarComprasPorTipoAtivo("TESOURO_DIRETO");
            List<Resgate> resgatesEsperados = filtrarResgatesPorTipoAtivo("TESOURO_DIRETO");
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoAtivo == Acao")
        void quandoFiltrarPorAcao() throws Exception {
            // Arrange
            criarTransacoes();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoAtivo", "ACAO")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<Compra> comprasEsperadas = filtrarComprasPorTipoAtivo("ACAO");
            List<Resgate> resgatesEsperados = filtrarResgatesPorTipoAtivo("ACAO");
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoAtivo == CriptoMoeda")
        void quandoFiltrarPorCriptomoeda() throws Exception {
            // Arrange
            criarTransacoes();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoAtivo", "CRIPTOMOEDA")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<Compra> comprasEsperadas = filtrarComprasPorTipoAtivo("CRIPTOMOEDA");
            List<Resgate> resgatesEsperados = filtrarResgatesPorTipoAtivo("CRIPTOMOEDA");
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas por período")
        void quandoFiltrarPorPeriodo() throws Exception {
            // Arrange
            criarTransacoes();
            LocalDateTime dataInicio = LocalDateTime.of(2025, 9, 7, 5, 0, 0);
            LocalDateTime dataFim = LocalDateTime.of(2025, 9, 7, 10, 0, 0);

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("dataInicio", String.valueOf(dataInicio))
                            .param("dataFim", String.valueOf(dataInicio))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<Compra> comprasEsperadas = filtrarComprasPorPeriodo(dataInicio, dataFim);
            List<Resgate> resgatesEsperados = filtrarResgatesPorPeriodo(dataInicio, dataFim);
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações feitas por um cliente")
        void quandoFiltrarPorCliente() throws Exception {
            // Arrange
            criarTransacoes();
            Long clientId = clientes.get(3).getId();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("clienteId", String.valueOf(clientId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<Compra> comprasEsperadas = filtrarComprasPorCliente(clientId);
            List<Resgate> resgatesEsperados = filtrarResgatesPorCliente(clientId);
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(comprasEsperadas, resgatesEsperados);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoOperação == compra")
        void quandoFiltrarPorCompra() throws Exception {
            // Arrange
            criarTransacoes();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoOperacao", "compra")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(compras, List.of());
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar uma lista de transações filtradas pelo tipoOperação == resgate")
        void quandoFiltrarPorResgate() throws Exception {
            // Arrange
            criarTransacoes();

            // Act
            String jsonResponse = driver.perform(get(URI_TRANSACOES)
                            .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                            .param("tipoOperacao", "resgate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<TransacaoResponseDTO> result = objectMapper.readValue(jsonResponse, new TypeReference<List<TransacaoResponseDTO>>() {});

            // Assert
            List<TransacaoResponseDTO> expected = mapTransacoesEsperadas(List.of(), resgates);
            assertEquals(expected, result);
        }

        // Métodos auxiliares para filtros
        private List<Compra> filtrarComprasPorTipoAtivo(String tipoAtivo) {
            return compras.stream()
                    .filter(c -> {
                        Ativo ativo = ativos.stream().filter(a -> a.getId().equals(c.getIdAtivo())).findFirst().orElse(null);
                        return ativo != null && ativo.getTipo().getNomeTipo().name().equals(tipoAtivo);
                    })
                    .toList();
        }

        private List<Resgate> filtrarResgatesPorTipoAtivo(String tipoAtivo) {
            return resgates.stream()
                    .filter(r -> {
                        Ativo ativo = ativos.stream().filter(a -> a.getId().equals(r.getIdAtivo())).findFirst().orElse(null);
                        return ativo != null && ativo.getTipo().getNomeTipo().name().equals(tipoAtivo);
                    })
                    .toList();
        }

        private List<Compra> filtrarComprasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
            return compras.stream()
                    .filter(c -> !c.getDataSolicitacao().isBefore(dataInicio) && !c.getDataSolicitacao().isAfter(dataFim))
                    .toList();
        }

        private List<Resgate> filtrarResgatesPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
            return resgates.stream()
                    .filter(r -> !r.getDataSolicitacao().isBefore(dataInicio) && !r.getDataSolicitacao().isAfter(dataFim))
                    .toList();
        }

        private List<Compra> filtrarComprasPorCliente(Long clienteId) {
            return compras.stream()
                    .filter(c -> c.getIdCliente().equals(clienteId))
                    .toList();
        }

        private List<Resgate> filtrarResgatesPorCliente(Long clienteId) {
            return resgates.stream()
                    .filter(r -> r.getIdCliente().equals(clienteId))
                    .toList();
        }
    }
}